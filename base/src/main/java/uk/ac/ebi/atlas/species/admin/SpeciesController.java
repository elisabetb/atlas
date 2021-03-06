package uk.ac.ebi.atlas.species.admin;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.atlas.controllers.JsonExceptionHandlingController;
import uk.ac.ebi.atlas.species.SpeciesPropertiesTrader;

import javax.inject.Inject;

@Controller
@Scope("request")
public class SpeciesController extends JsonExceptionHandlingController {

    private final SpeciesPropertiesTrader speciesPropertiesTrader;

    @Inject
    public SpeciesController(SpeciesPropertiesTrader speciesPropertiesTrader) {
        this.speciesPropertiesTrader = speciesPropertiesTrader;
    }

    @RequestMapping(value = "/json/species/{speciesName}",produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getSpeciesInfo(@PathVariable("speciesName") String speciesName) {
        return gson.toJson(speciesPropertiesTrader.get(speciesName));
    }

}
