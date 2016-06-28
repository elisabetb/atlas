
package uk.ac.ebi.atlas.widget;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import com.google.gson.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.atlas.experimentpage.baseline.BaselineExperimentPageService;
import uk.ac.ebi.atlas.experimentpage.baseline.BaselineExperimentPageServiceFactory;
import uk.ac.ebi.atlas.experimentpage.baseline.AnatomogramFactory;
import uk.ac.ebi.atlas.experimentpage.context.GenesNotFoundException;
import uk.ac.ebi.atlas.model.ExperimentType;
import uk.ac.ebi.atlas.model.Species;
import uk.ac.ebi.atlas.model.baseline.*;
import uk.ac.ebi.atlas.profiles.baseline.BaselineProfileInputStreamFactory;
import uk.ac.ebi.atlas.profiles.baseline.viewmodel.*;
import uk.ac.ebi.atlas.search.analyticsindex.baseline.BaselineAnalyticsSearchDao;
import uk.ac.ebi.atlas.search.analyticsindex.baseline.BaselineAnalyticsSearchService;
import uk.ac.ebi.atlas.search.baseline.BaselineExperimentProfile;
import uk.ac.ebi.atlas.search.baseline.BaselineExperimentProfileSearchService;
import uk.ac.ebi.atlas.search.baseline.BaselineExperimentProfilesList;
import uk.ac.ebi.atlas.search.baseline.BaselineExperimentSearchResult;
import uk.ac.ebi.atlas.solr.query.SolrQueryService;
import uk.ac.ebi.atlas.solr.query.SpeciesLookupService;
import uk.ac.ebi.atlas.web.ApplicationProperties;
import uk.ac.ebi.atlas.web.BaselineRequestPreferences;
import uk.ac.ebi.atlas.web.GeneQuery;
import uk.ac.ebi.atlas.web.controllers.ResourceNotFoundException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Set;
import java.util.SortedSet;

@Controller
@Scope("request")
public final class HeatmapWidgetController extends HeatmapWidgetErrorHandler {

    public static final String ORIGINAL_GENEQUERY = "geneQuery";

    private AnatomogramFactory anatomogramFactory;

    private SpeciesLookupService speciesLookupService;

    private final BaselineExperimentProfileSearchService baselineExperimentProfileSearchService;

    private final BaselineExperimentProfilesViewModelBuilder baselineExperimentProfilesViewModelBuilder;

    private final BaselineAnalyticsSearchService baselineAnalyticsSearchService;

    private final BaselineExperimentPageService baselineExperimentPageService;

    private final SolrQueryService solrQueryService;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Inject
    private HeatmapWidgetController(ApplicationProperties applicationProperties, SpeciesLookupService speciesLookupService,
                                    BaselineExperimentProfileSearchService baselineExperimentProfileSearchService,
                                    BaselineExperimentProfilesViewModelBuilder baselineExperimentProfilesViewModelBuilder,
                                    BaselineAnalyticsSearchService baselineAnalyticsSearchService,
                                    BaselineExperimentPageServiceFactory baselineExperimentPageServiceFactory,
                                    @Qualifier ("baselineProfileInputStreamFactory")BaselineProfileInputStreamFactory baselineProfileInputStreamFactory,
                                    SolrQueryService solrQueryService) {
        this.anatomogramFactory = new AnatomogramFactory(applicationProperties);
        this.speciesLookupService = speciesLookupService;
        this.baselineExperimentProfileSearchService = baselineExperimentProfileSearchService;
        this.baselineExperimentProfilesViewModelBuilder = baselineExperimentProfilesViewModelBuilder;
        this.baselineAnalyticsSearchService = baselineAnalyticsSearchService;
        this.baselineExperimentPageService = baselineExperimentPageServiceFactory.create(baselineProfileInputStreamFactory);
        this.solrQueryService = solrQueryService;
    }

    @RequestMapping(value = "/widgets/heatmap/referenceExperiment", params = "type=RNASEQ_MRNA_BASELINE")
    public String fetchReferenceExperimentProfilesJson(@ModelAttribute("preferences") @Valid BaselineRequestPreferences preferences,
                                                       @RequestParam(value = "disableGeneLinks", required = false) boolean disableGeneLinks,
                                                       BindingResult result, Model model, HttpServletRequest request,
                                                       HttpServletResponse response) {
        BaselineExperiment experiment = (BaselineExperiment) request.getAttribute("experiment");

        baselineExperimentPageService.prepareRequestPreferencesAndHeaderData(experiment, preferences, model,request,true);
        try {
            baselineExperimentPageService.populateModelWithHeatmapData(experiment, preferences, model,request,true, disableGeneLinks);
        } catch (GenesNotFoundException e) {
            throw new ResourceNotFoundException("No genes found matching query: '" + preferences.getGeneQuery() + "'");
        }

        // set here instead of in JSP, because the JSP may be included elsewhere
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return "heatmap-data";
    }

    @RequestMapping(value = "/widgets/heatmap/multiExperiment")
    public String multiExperimentJson(@RequestParam(value = "geneQuery", required = true) GeneQuery geneQuery,
                                      @RequestParam(value = "species", required = false) String species,
                                      @RequestParam(value = "propertyType", required = false) String propertyType,
                                      Model model,HttpServletRequest request, HttpServletResponse response) {

        String ensemblSpecies= StringUtils.isBlank(species)
                ?speciesLookupService.fetchFirstSpeciesByField(propertyType, geneQuery.asString())
                : Species.convertToEnsemblSpecies(species);


        Optional<Set<String>> geneIds = solrQueryService.expandGeneQueryIntoGeneIds(geneQuery.asString(), ensemblSpecies, true);

        BaselineExperimentSearchResult searchResult = geneIds.isPresent()
            ?   baselineExperimentProfileSearchService.query(geneIds.get())
            : new BaselineExperimentSearchResult();

        if (searchResult.isEmpty()) {
            throw new ResourceNotFoundException("No baseline expression in tissues found for "
                    + geneQuery.description());
        }

        populateModelWithMultiExperimentResults(request.getContextPath(),geneQuery, ensemblSpecies, searchResult, model);

        // set here instead of in JSP, because the JSP may be included elsewhere
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return "heatmap-data";
    }

    //This endpoint is used in gene/geneset page, for the callback,
    // e.g. http://wwwdev.ebi.ac.uk/gxa/genesets/GO:0000002
    @RequestMapping(value = "/widgets/heatmap/baselineAnalytics")
    public String analyticsJson(@RequestParam(value = "geneQuery", required = true) GeneQuery geneQuery,
                                @RequestParam(value = "species", required = false) String species,
                                @RequestParam(value = "propertyType", required = false) String propertyType,
                                @RequestParam(value = "source", required = false) String source,
                                Model model, HttpServletRequest request, HttpServletResponse response) {

        String ensemblSpecies = StringUtils.isBlank(species) ?
                speciesLookupService.fetchFirstSpeciesByField(propertyType, geneQuery.asString())
                : Species.convertToEnsemblSpecies(species);

        String defaultFactorQueryType = StringUtils.isBlank(source) ? "ORGANISM_PART" : source.toUpperCase();
        BaselineExperimentSearchResult searchResult = baselineAnalyticsSearchService.findExpressions(geneQuery, ensemblSpecies, defaultFactorQueryType);

        populateModelWithMultiExperimentResults(request.getContextPath(), geneQuery, ensemblSpecies, searchResult, model);

        // set here instead of in JSP, because the JSP may be included elsewhere
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return "heatmap-data";
    }

    private void populateModelWithMultiExperimentResults(String contextRoot, GeneQuery geneQuery, String ensemblSpecies,
                                                         BaselineExperimentSearchResult searchResult, Model model) {
        SortedSet<Factor> orderedFactors = searchResult.getFactorsAcrossAllExperiments();

        if (searchResult.containsFactorOfType("ORGANISM_PART")) {
            model.addAttribute("anatomogram", anatomogramFactory.get("ORGANISM_PART", ensemblSpecies, convert(orderedFactors), contextRoot));
        } else {
            model.addAttribute("anatomogram", gson.toJson(JsonNull.INSTANCE));
        }

        BaselineExperimentProfilesList experimentProfiles = searchResult.getExperimentProfiles();
        addJsonForHeatMap(experimentProfiles, orderedFactors, model);

        model.addAttribute("species", ensemblSpecies);
        model.addAttribute("isWidget", true);
        model.addAttribute("experiment", gson.toJson(JsonNull.INSTANCE));
        model.addAttribute("geneQuery", geneQuery);
    }

    private SortedSet<AssayGroupFactor> convert(SortedSet<Factor> orderedFactors) {
        ImmutableSortedSet.Builder<AssayGroupFactor> builder = ImmutableSortedSet.naturalOrder();

        for (Factor factor : orderedFactors) {
            AssayGroupFactor assayGropuFactor = new AssayGroupFactor("none",factor);
            builder.add(assayGropuFactor);
        }

        return builder.build();
    }

    private void addJsonForHeatMap(BaselineExperimentProfilesList baselineProfiles, SortedSet<Factor> orderedFactors, Model model) {
        if (baselineProfiles.isEmpty()) {
            return;
        }

        model.addAttribute("jsonColumnHeaders", gson.toJson(AssayGroupFactorViewModel.createList(convert(orderedFactors))));
        model.addAttribute("jsonProfiles", gson.toJson(baselineExperimentProfilesViewModelBuilder.buildJson(baselineProfiles, orderedFactors)));

        Set<Factor> nonExpressedFactors = Sets.newHashSet(orderedFactors);
        for (BaselineExperimentProfile baselineProfile : baselineProfiles) {
            double defaultCutoff =
                    baselineProfile.getExperimentType().equalsIgnoreCase(ExperimentType.PROTEOMICS_BASELINE.toString()) ?
                            BaselineAnalyticsSearchDao.DEFAULT_PROTEOMICS_CUT_OFF : BaselineAnalyticsSearchDao.DEFAULT_BASELINE_CUT_OFF;

            for (Factor factor : Sets.newHashSet(nonExpressedFactors)) {
                BaselineExpression expression = baselineProfile.getExpression(factor);
                if (expression != null && expression.isKnown() && expression.getLevel() >= defaultCutoff) {
                    nonExpressedFactors.remove(factor);
                }
            }
        }
        Set<String> nonExpressedColumnHeaders = Sets.newHashSetWithExpectedSize(nonExpressedFactors.size());
        for (Factor nonExpressedFactor : nonExpressedFactors) {
            nonExpressedColumnHeaders.add(nonExpressedFactor.getValue());
        }

        model.addAttribute("jsonNonExpressedColumnHeaders", gson.toJson(nonExpressedColumnHeaders));
    }
}
