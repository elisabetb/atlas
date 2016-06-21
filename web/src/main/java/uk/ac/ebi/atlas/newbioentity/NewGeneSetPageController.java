package uk.ac.ebi.atlas.newbioentity;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.atlas.bioentity.GeneSetUtil;
import uk.ac.ebi.atlas.model.ExperimentType;
import uk.ac.ebi.atlas.web.GeneQuery;
import uk.ac.ebi.atlas.web.controllers.ResourceNotFoundException;

@Controller
@Scope("request")
public class NewGeneSetPageController extends NewBioentityPageController {

    @Value("#{configuration['index.property_names.genesetpage']}")
    void setGenePagePropertyTypes(String[] propertyNames) {
        this.propertyNames = propertyNames;
    }

    @RequestMapping(value = "/genesets/{identifier:.*}")
    public String showGeneSetPage(@PathVariable String identifier, Model model) {

        if (!GeneSetUtil.matchesGeneSetAccession(identifier)) {
            throw new ResourceNotFoundException("No gene set matching " + identifier);
        }

        bioentityPropertyServiceInitializer.initForGeneSetPage(bioEntityPropertyService, identifier);

        String species = GeneSetUtil.matchesReactomeID(identifier) ? bioEntityPropertyService.getSpecies() : "";

        model.addAttribute(
            "mainTitle",

            "Expression summary for " + bioEntityPropertyService.getBioEntityDescription() +
                (StringUtils.isNotBlank(species) ?
                    " - " + StringUtils.capitalize(species) :
                    "")
        );

        model.addAttribute("species", species);
        model.addAttribute("queryType", "geneSet");

        ImmutableSet<String> experimentTypes = analyticsIndexSearchDAO.fetchExperimentTypes(GeneQuery.create(identifier));
        model.addAttribute("hasDifferentialResults", ExperimentType.containsDifferential(experimentTypes));

        return super.showBioentityPage(identifier, model, experimentTypes);
    }

    @RequestMapping(value = {"/json/genesets/{identifier:.*}/differentialFacets"}, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String fetchDifferentialJsonFacets(@PathVariable String identifier) {
        return differentialAnalyticsSearchService.fetchDifferentialFacetsForSearch(GeneQuery.create(identifier));
    }

    @RequestMapping(value = {"/json/genesets/{identifier:.*}/differentialResults"}, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String fetchDifferentialJsonResults(@PathVariable String identifier) {
        return differentialAnalyticsSearchService.fetchDifferentialResultsForSearch(GeneQuery.create(identifier));
    }
}