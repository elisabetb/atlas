package uk.ac.ebi.atlas.experimentimport.admin;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.ac.ebi.atlas.experimentimport.ExperimentCrud;
import uk.ac.ebi.atlas.experimentimport.ExperimentDAO;
import uk.ac.ebi.atlas.experimentimport.ExpressionAtlasExperimentChecker;
import uk.ac.ebi.atlas.experimentimport.analytics.AnalyticsLoaderFactory;
import uk.ac.ebi.atlas.experimentimport.analyticsindex.AnalyticsIndexerManager;
import uk.ac.ebi.atlas.experimentimport.coexpression.BaselineCoexpressionProfileLoader;
import uk.ac.ebi.atlas.experimentimport.condensedSdrf.CondensedSdrfParser;
import uk.ac.ebi.atlas.experimentimport.experimentdesign.ExperimentDesignFileWriterService;
import uk.ac.ebi.atlas.experimentimport.expressiondataserializer.ExpressionSerializerService;
import uk.ac.ebi.atlas.resource.DataFileHub;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import javax.inject.Inject;

@Controller
@Scope("request")
@RequestMapping("/admin/experiments")
public class ExpressionAtlasExperimentAdminController extends ExperimentAdminController {

    @Inject
    public ExpressionAtlasExperimentAdminController(DataFileHub dataFileHub,
                                                    CondensedSdrfParser condensedSdrfParser,
                                                    ExperimentDesignFileWriterService experimentDesignFileWriterService,
                                                    ExperimentDAO experimentDAO,
                                                    ExpressionAtlasExperimentChecker experimentChecker,
                                                    AnalyticsLoaderFactory analyticsLoaderFactory,
                                                    ConfigurationTrader configurationTrader,
                                                    BaselineCoexpressionProfileLoader baselineCoexpressionProfileLoader,
                                                    AnalyticsIndexerManager analyticsIndexerManager,
                                                    ExpressionSerializerService expressionSerializerService,
                                                    ExperimentTrader experimentTrader
    ) {
        super(
            new ExperimentOps(
                new ExperimentOpLogWriter(
                    dataFileHub
                ),
                new ExpressionAtlasExperimentOpsExecutionService(
                    new ExperimentCrud(
                        condensedSdrfParser,
                        experimentDesignFileWriterService,
                        experimentDAO,
                        experimentChecker,
                        analyticsLoaderFactory,
                        configurationTrader),
                    baselineCoexpressionProfileLoader,
                    analyticsIndexerManager,
                    expressionSerializerService,
                    experimentTrader
                )
            )
        );
    }
}
