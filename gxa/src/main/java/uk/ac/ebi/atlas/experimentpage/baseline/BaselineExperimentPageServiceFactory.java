package uk.ac.ebi.atlas.experimentpage.baseline;

import uk.ac.ebi.atlas.experimentpage.baseline.coexpression.CoexpressedGenesDao;
import uk.ac.ebi.atlas.experimentpage.baseline.coexpression.CoexpressedGenesService;
import uk.ac.ebi.atlas.experimentpage.baseline.grouping.FactorGroupingService;
import uk.ac.ebi.atlas.profiles.baseline.RankBaselineProfilesFactory;
import uk.ac.ebi.atlas.profiles.baseline.viewmodel.BaselineProfilesViewModelBuilder;
import uk.ac.ebi.atlas.resource.AtlasResourceHub;
import uk.ac.ebi.atlas.solr.query.SolrQueryService;
import uk.ac.ebi.atlas.utils.HeatmapDataToJsonService;
import uk.ac.ebi.atlas.web.ApplicationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.atlas.profiles.baseline.BaselineProfileInputStreamFactory;
import uk.ac.ebi.atlas.tracks.TracksUtil;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class BaselineExperimentPageServiceFactory {

    private final TracksUtil tracksUtil;
    private final ApplicationProperties applicationProperties;
    private final BaselineProfilesViewModelBuilder baselineProfilesViewModelBuilder;
    private final CoexpressedGenesService coexpressedGenesService;
    private final SolrQueryService solrQueryService;
    private final RankBaselineProfilesFactory rankProfilesFactory;
    private final FactorGroupingService factorGroupingService;
    private final AtlasResourceHub atlasResourceHub;
    private final HeatmapDataToJsonService heatmapDataToJsonService;

    @Inject
    public BaselineExperimentPageServiceFactory(TracksUtil tracksUtil, ApplicationProperties applicationProperties,
                                                BaselineProfilesViewModelBuilder baselineProfilesViewModelBuilder,
                                                JdbcTemplate jdbcTemplate,
                                                SolrQueryService solrQueryService, RankBaselineProfilesFactory rankProfilesFactory,
                                                FactorGroupingService factorGroupingService, AtlasResourceHub
                                                            atlasResourceHub,HeatmapDataToJsonService heatmapDataToJsonService) {
        this.tracksUtil = tracksUtil;
        this.applicationProperties = applicationProperties;
        this.baselineProfilesViewModelBuilder = baselineProfilesViewModelBuilder;
        this.coexpressedGenesService = new CoexpressedGenesService(new CoexpressedGenesDao(jdbcTemplate));
        this.solrQueryService = solrQueryService;
        this.rankProfilesFactory = rankProfilesFactory;
        this.factorGroupingService = factorGroupingService;
        this.atlasResourceHub = atlasResourceHub;
        this.heatmapDataToJsonService = heatmapDataToJsonService;
    }

    public BaselineExperimentPageService create(BaselineProfileInputStreamFactory inputStreamFactory){
        return new BaselineExperimentPageService(new BaselineProfilesHeatMapWranglerFactory(rankProfilesFactory,
                inputStreamFactory,baselineProfilesViewModelBuilder, solrQueryService, coexpressedGenesService),
                applicationProperties,atlasResourceHub,
                tracksUtil,factorGroupingService, heatmapDataToJsonService
        );
    }
}
