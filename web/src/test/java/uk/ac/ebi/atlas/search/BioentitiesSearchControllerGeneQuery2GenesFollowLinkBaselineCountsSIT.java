/*
 * Copyright 2008-2013 Microarray Informatics Team, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the Gene Expression Atlas project, including source code,
 * downloads and documentation, please see:
 *
 * http://gxa.github.com/gxa
 */

package uk.ac.ebi.atlas.search;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.atlas.acceptance.selenium.fixture.SinglePageSeleniumFixture;
import uk.ac.ebi.atlas.acceptance.selenium.pages.BaselineBioEntitiesSearchResult;
import uk.ac.ebi.atlas.acceptance.selenium.pages.BioEntitiesPage;
import uk.ac.ebi.atlas.acceptance.selenium.pages.HeatmapTablePage;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

public class BioentitiesSearchControllerGeneQuery2GenesFollowLinkBaselineCountsSIT extends SinglePageSeleniumFixture {

    private BioEntitiesPage subject;

    @Override
    protected void getStartingPage() {
        subject = BioEntitiesPage.search(driver, "geneQuery=TERF2%09GFI1");
        subject.get();
    }

    @Test
    public void multipleGenesQueryInMultipleSpecies_followingBaselineCountsLinkShouldGoToExperimentPageWithResults() {
        List<BaselineBioEntitiesSearchResult> baselineCounts = subject.getBaselineResults();

        assertThat(baselineCounts, hasSize(4));

        //click link to go to experiment page for 27 tissues
        subject.getBaselineCountElements().get(1).click();

        HeatmapTablePage experimentPage = new HeatmapTablePage(driver, null);

        List<String> geneNames = experimentPage.getGeneNames();
        Assert.assertThat(geneNames, contains("TERF2", "GFI1"));
    }

}
