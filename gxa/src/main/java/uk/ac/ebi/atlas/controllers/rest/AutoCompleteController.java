package uk.ac.ebi.atlas.controllers.rest;

import uk.ac.ebi.atlas.search.SemanticQueryTerm;
import uk.ac.ebi.atlas.solr.query.SuggestionService;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.atlas.species.SpeciesFactory;

import javax.inject.Inject;
import java.util.List;

@Controller
@Scope("request")
public class AutoCompleteController {

    private final SuggestionService suggestionService;
    private final SpeciesFactory speciesFactory;

    @Inject
    public AutoCompleteController(SuggestionService suggestionService, SpeciesFactory speciesFactory) {
        this.suggestionService = suggestionService;
        this.speciesFactory = speciesFactory;
    }

    @RequestMapping(value = "/json/suggestions", method = RequestMethod.GET,
                    produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String fetchTopSuggestions(
            @RequestParam(value = "query") String query,
            @RequestParam(value = "species", required = false, defaultValue="") String species) {

        if (StringUtils.isBlank(query)) {
            return StringUtils.EMPTY;
        }

        List<SemanticQueryTerm> suggestions =
                suggestionService.fetchTopSuggestions(query, speciesFactory.create(species).getReferenceName());
        return new Gson().toJson(suggestions);
    }

}
