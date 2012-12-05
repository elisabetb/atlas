package uk.ac.ebi.atlas.acceptance.selenium;

import org.junit.Test;
import uk.ac.ebi.atlas.acceptance.selenium.pages.HeatmapTablePage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ExperimentTitleIT extends SeleniumFixture {

    private HeatmapTablePage subject;

    public void getStartingPage() {
        subject = new HeatmapTablePage(firefoxDriver);
        subject.get();
    }

    @Test
    public void testTitle() {
        assertThat(subject.getExperimentDescription(), is("RNA-Seq of human individual tissues and mixture of 16 " +
                "tissues (Illumina Body " +
                "Map)"));
    }


}
