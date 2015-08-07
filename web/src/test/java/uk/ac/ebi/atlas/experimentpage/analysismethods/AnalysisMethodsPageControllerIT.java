package uk.ac.ebi.atlas.experimentpage.analysismethods;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;
import uk.ac.ebi.atlas.commons.readers.TsvReader;
import uk.ac.ebi.atlas.commons.readers.FileTsvReaderBuilder;
import uk.ac.ebi.atlas.model.baseline.ProteomicsBaselineExperiment;
import uk.ac.ebi.atlas.trader.cache.ProteomicsBaselineExperimentsCache;
import uk.ac.ebi.atlas.web.controllers.ExperimentDispatcher;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:solrContextIT.xml", "classpath:oracleContext.xml"})
public class AnalysisMethodsPageControllerIT {

    private static final String EXPERIMENT_ACCESSION = "E-PROT-1";

    @Value("#{configuration['experiment.analysis-method.path.template']}")
    private String analysisMethodsTemplate;
    @Inject
    private AnalysisMethodsPageController subject;

    @Inject
    private ProteomicsBaselineExperimentsCache proteomicsBaselineExperimentsCache;

    private HttpServletRequest requestMock;

    @Inject
    private FileTsvReaderBuilder fileTsvReaderBuilder;

    Model model = new BindingAwareModelMap();

    @Before
    public void initSubject() throws Exception {
        requestMock = mock(HttpServletRequest.class);
        ProteomicsBaselineExperiment proteomicsBaselineExperiment = proteomicsBaselineExperimentsCache.getExperiment(EXPERIMENT_ACCESSION);
        when(requestMock.getAttribute(ExperimentDispatcher.EXPERIMENT_ATTRIBUTE)).thenReturn(proteomicsBaselineExperiment);
        when(requestMock.getRequestURI()).thenReturn("/gxa/experiments/" + EXPERIMENT_ACCESSION + "/analysis-methods");
    }

    @Test
    public void testExtractProteomicsAnalysisMethods() throws IOException {

        //given
        subject.proteomicsAnalysisMethods(EXPERIMENT_ACCESSION, model, requestMock);

        TsvReader tsvReader = fileTsvReaderBuilder.forTsvFilePathTemplate(analysisMethodsTemplate).withExperimentAccession(EXPERIMENT_ACCESSION).build();

        List<String[]> result = tsvReader.readAll();
        String[] firstLine = result.get(0);
        String[] lastLine = result.get(result.size() - 1);

        assertThat(result.size(), is(10));
        assertThat(firstLine, arrayContaining("Pipeline ", "Please see <a href=\"experiments/E-PROT-1/analysis-methods/HumanDataAnalysisFigure.pdf\">Workflow</a> and <a href=\"experiments/E-PROT-1/analysis-methods/HumanDataAnalysisMethods.pdf\">Methods</a> for more information."));
        assertThat(lastLine, arrayContaining("Normalized Counts per Gene", "Obtained from the <a href=\"http://cufflinks.cbcb.umd.edu/manual.html#fpkm_tracking_format\">genes.fpkm_tracking files</a>, then averaged for all biological replicates (if any)"));

    }

    @Test
    public void testExtractPdfURL() throws IOException {
        assertThat(subject.generatePDFPath(EXPERIMENT_ACCESSION, "HumanDataAnalysisFigure"), is("/expdata/E-PROT-1/HumanDataAnalysisFigure.pdf"));
    }

}