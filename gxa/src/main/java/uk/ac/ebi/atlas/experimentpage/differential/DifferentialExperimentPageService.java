package uk.ac.ebi.atlas.experimentpage.differential;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import uk.ac.ebi.atlas.controllers.DownloadURLBuilder;
import uk.ac.ebi.atlas.experimentpage.ExperimentPageService;
import uk.ac.ebi.atlas.experimentpage.context.DifferentialRequestContext;
import uk.ac.ebi.atlas.experimentpage.context.DifferentialRequestContextBuilder;
import uk.ac.ebi.atlas.experimentpage.tooltip.ContrastSummaryBuilder;
import uk.ac.ebi.atlas.model.differential.Contrast;
import uk.ac.ebi.atlas.model.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.differential.DifferentialProfile;
import uk.ac.ebi.atlas.model.differential.DifferentialProfilesList;
import uk.ac.ebi.atlas.profiles.differential.viewmodel.DifferentialProfilesViewModelBuilder;
import uk.ac.ebi.atlas.resource.AtlasResourceHub;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.tracks.TracksUtil;
import uk.ac.ebi.atlas.utils.HeatmapDataToJsonService;
import uk.ac.ebi.atlas.web.ApplicationProperties;
import uk.ac.ebi.atlas.web.DifferentialRequestPreferences;
import uk.ac.ebi.atlas.web.GenesNotFoundException;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

public class DifferentialExperimentPageService<T extends DifferentialExperiment, K extends
        DifferentialRequestPreferences, P extends DifferentialProfile<?>> extends ExperimentPageService {

    private final DifferentialProfilesViewModelBuilder differentialProfilesViewModelBuilder;
    private final AtlasResourceHub atlasResourceHub;
    private DifferentialRequestContextBuilder differentialRequestContextBuilder;
    private DifferentialProfilesHeatMap<P, DifferentialRequestContext<?>> profilesHeatMap;
    private TracksUtil tracksUtil;
    private final ApplicationProperties applicationProperties;

    @SuppressWarnings("unchecked")
    protected DifferentialExperimentPageService(DifferentialRequestContextBuilder
                                                           differentialRequestContextBuilder,
                                                DifferentialProfilesHeatMap<P, DifferentialRequestContext<?>> profilesHeatMap,
                                                DifferentialProfilesViewModelBuilder differentialProfilesViewModelBuilder,
                                                TracksUtil tracksUtil,
                                                AtlasResourceHub atlasResourceHub, ApplicationProperties applicationProperties) {
        super(atlasResourceHub, new HeatmapDataToJsonService(applicationProperties));
        this.differentialRequestContextBuilder = differentialRequestContextBuilder;
        this.profilesHeatMap = profilesHeatMap;
        this.differentialProfilesViewModelBuilder = differentialProfilesViewModelBuilder;
        this.tracksUtil = tracksUtil;
        this.atlasResourceHub = atlasResourceHub;
        this.applicationProperties = applicationProperties;
    }


    // called from sub classes
    public void prepareRequestPreferencesAndHeaderData(T experiment, K preferences, Model model,HttpServletRequest request) {

        initRequestPreferences(preferences, experiment);
        model.addAttribute("atlasHost", applicationProperties.buildAtlasHostURL(request));
        model.addAttribute("queryFactorName", "Comparison");
        model.addAttribute("allQueryFactors", experiment.getContrasts());
        model.addAllAttributes(experiment.getAttributes());
        model.addAllAttributes(experiment.getDifferentialAttributes());
        model.addAllAttributes(new DownloadURLBuilder(experiment.getAccession()).dataDownloadUrls(request.getRequestURI()));
        model.addAllAttributes(headerAttributes(experiment, preferences));
    }

    public JsonObject populateModelWithHeatmapData(HttpServletRequest request,T experiment, K preferences,
                                                   BindingResult result, Model model) {
        DifferentialRequestContext requestContext = initRequestContext(experiment, preferences);
        Set<Contrast> contrasts = experiment.getContrasts();
        model.addAttribute("queryFactorName", "Comparison");
        model.addAttribute("geneQuery", preferences.getGeneQuery());
        model.addAllAttributes(experiment.getAttributes());
        model.addAllAttributes(experiment.getDifferentialAttributes());

        model.addAttribute("enableEnsemblLauncher", tracksUtil.hasDiffTracksPath(experiment.getAccession(), contrasts.iterator().next().getId()));

        model.addAttribute("anatomogram", gson.toJson(JsonNull.INSTANCE));
        model.addAttribute("experimentDescription", gson.toJson(JsonNull.INSTANCE));
        model.addAllAttributes(payloadAttributes(experiment, preferences));
        if (!result.hasErrors()) {

            try {

                DifferentialProfilesList differentialProfiles = profilesHeatMap.fetch(requestContext);
                if (!differentialProfiles.isEmpty()) {
                    model.addAttribute("gseaPlots", gson.toJson(atlasResourceHub.createJsonByContrastIdForTheOldHeatmap(experiment
                            .getAccession(), contrasts)));
                    model.addAttribute("jsonColumnHeaders", gson.toJson(constructColumnHeaders(contrasts,experiment)));
                    model.addAttribute("jsonProfiles", gson.toJson(differentialProfilesViewModelBuilder.build
                            (differentialProfiles, contrasts)));
                }
                return heatmapDataToJsonService.toJsonObject(request,preferences.getGeneQuery(), SemanticQuery.create
                        (), model.asMap());

            } catch (GenesNotFoundException e) {
                String msg = "No genes found matching query: '" + preferences.getGeneQuery().description() + "'";
                result.addError(new ObjectError("requestPreferences",msg ));//I'm not sure if this works- on error
                // Spring MVC magic kicks in?
                return heatmapDataToJsonService.jsonError(msg);
            }

        } else {
            return heatmapDataToJsonService.jsonError(FluentIterable.from(result.getAllErrors()).transform(new Function<ObjectError, String>() {
                @Nullable
                @Override
                public String apply(@Nullable ObjectError objectError) {
                    return objectError.toString();
                }
            }).join(Joiner.on(',')));
        }
    }

    private JsonArray constructColumnHeaders(Iterable<Contrast> contrasts, DifferentialExperiment
            differentialExperiment){
        JsonArray result = new JsonArray();
        Map<String, JsonArray> contrastImages = atlasResourceHub.contrastImages(differentialExperiment);
        for(Contrast contrast: contrasts){
            JsonObject o = contrast.toJson();
            o.add("contrastSummary", new ContrastSummaryBuilder()
                    .forContrast(contrast)
                    .withExperimentDesign(differentialExperiment.getExperimentDesign())
                    .withExperimentDescription(differentialExperiment.getDescription())
                    .build().toJson());
            o.add("resources", contrastImages.get(contrast.getId()));
            result.add(o);
        }
        return result;
    }

    private void initRequestPreferences(K requestPreferences, T experiment) {
        //if there is only one contrast we want to preselect it... from Robert feedback
        if (experiment.getContrasts().size() == 1) {
            requestPreferences.setQueryFactorValues(experiment.getContrastIds());
        }
    }

    private DifferentialRequestContext initRequestContext(T experiment, DifferentialRequestPreferences requestPreferences) {
        return differentialRequestContextBuilder.forExperiment(experiment)
                .withPreferences(requestPreferences).build();

    }

}
