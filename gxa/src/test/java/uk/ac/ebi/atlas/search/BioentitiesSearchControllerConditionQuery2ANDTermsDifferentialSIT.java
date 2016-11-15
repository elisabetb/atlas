
package uk.ac.ebi.atlas.search;

import uk.ac.ebi.atlas.acceptance.selenium.fixture.SinglePageSeleniumFixture;
import uk.ac.ebi.atlas.acceptance.selenium.pages.BioEntitiesPage;
import org.junit.Test;
import uk.ac.ebi.atlas.acceptance.selenium.pages.BaselineBioEntitiesSearchResult;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BioentitiesSearchControllerConditionQuery2ANDTermsDifferentialSIT extends SinglePageSeleniumFixture {

    private static final String GLOBAL_SEARCH_TERM = "%22Mus+musculus%22+AND+%22wild+type%22";
    private BioEntitiesPage subject;

    @Override
    protected void getStartingPage() {
        subject = BioEntitiesPage.search(driver, "condition=Mus+musculus%09AND%09wild+type");
        subject.get();
    }

    @Test
    public void checkDifferentialProfilesCount() {
        subject.clickDifferentialPane();
        assertThat(subject.diffExpressionResultCount(), is("Showing 50 of 316 results"));
    }

    @Test
    public void globalSearchTermIsIdentifiersSeparatedByAND() {
        assertThat(subject.getGlobalSearchTerm(), is(GLOBAL_SEARCH_TERM));
    }

    @Test
    public void checkBaselineExperimentCounts() {
        List<BaselineBioEntitiesSearchResult> baselineCounts = subject.getAllBaselineResults();

        assertThat(baselineCounts, hasSize(2));
        assertThat(baselineCounts.get(0).getExperimentAccession(), is("E-MTAB-599"));
        assertThat(baselineCounts.get(0).getExperimentName(), is("Tissues - 6"));
        assertThat(baselineCounts.get(0).getSpecies(), is("Mus musculus"));
        assertThat(baselineCounts.get(0).getHref(), endsWith("E-MTAB-599?_specific=on&queryFactorType=ORGANISM_PART&queryFactorValues=&geneQuery="));

        assertThat(baselineCounts.get(1).getExperimentAccession(), is("E-GEOD-30352"));
        assertThat(baselineCounts.get(1).getExperimentName(), is("Vertebrate tissues"));
        assertThat(baselineCounts.get(1).getSpecies(), is("Mus musculus"));
        assertThat(baselineCounts.get(1).getHref(), endsWith("E-GEOD-30352?_specific=on&queryFactorType=ORGANISM_PART&queryFactorValues=brain,cerebellum,heart,kidney,liver,testis&geneQuery=&serializedFilterFactors=ORGANISM:Mus%20musculus"));
    }

}