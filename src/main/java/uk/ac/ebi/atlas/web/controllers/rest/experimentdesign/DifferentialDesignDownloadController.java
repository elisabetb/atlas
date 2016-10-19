
package uk.ac.ebi.atlas.web.controllers.rest.experimentdesign;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.atlas.commons.readers.FileTsvReaderBuilder;
import uk.ac.ebi.atlas.model.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Controller
@Scope("request")
public class DifferentialDesignDownloadController extends ExperimentDesignDownloadController<DifferentialExperiment> {

    @Inject
    public DifferentialDesignDownloadController(FileTsvReaderBuilder fileTsvReaderBuilder,ExperimentTrader experimentTrader) {
        super(fileTsvReaderBuilder,experimentTrader);
    }

    @RequestMapping(value = "/experiments/{experimentAccession}/experiment-design.tsv", params = {"type=RNASEQ_MRNA_DIFFERENTIAL"})
    public void downloadRnaSeqExperimentDesign(@PathVariable String experimentAccession,
                                               @RequestParam(value = "accessKey",required = false) String accessKey, HttpServletResponse response) throws IOException {

        extractExperimentDesign(experimentAccession, response,accessKey);

    }

    @RequestMapping(value = "/experiments/{experimentAccession}/experiment-design.tsv", params = {"type=MICROARRAY_ANY"})
    public void downloadMicroarrayExperimentDesign(@PathVariable String experimentAccession,
                                                   @RequestParam(value = "accessKey",required = false) String accessKey, HttpServletResponse response) throws IOException {

        extractExperimentDesign(experimentAccession, response, accessKey);

    }

    @Override
    protected Set<String> getAnalysedRowsAccessions(DifferentialExperiment experiment) {
        return experiment.getAssayAccessions();
    }
}















