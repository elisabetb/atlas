/*
 * Copyright 2008-2013 Microarray Informatics Team, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the Gene Expression Atlas project, including source code,
 * downloads and documentation, please see:
 *
 * http://gxa.github.com/gxa
 */

package uk.ac.ebi.atlas.web.controllers.page;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.atlas.web.DifferentialRequestPreferences;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Controller
@Scope("request")
public class GenePageController extends BioEntityPageController {

    public static final String ENTITY_NAME_PROPERTY_TYPE = "symbol";

    private String genePagePropertyTypes;

    private DifferentialGeneProfileService differentialGeneProfileService;

    private GenePropertiesService genePropertiesService;

    @Inject
    void setGenePropertiesService(GenePropertiesService genePropertiesService) {
        this.genePropertiesService = genePropertiesService;
    }

    @Value("#{configuration['index.types.genepage']}")
    void setGenePagePropertyTypes(String genePagePropertyTypes) {
        this.genePagePropertyTypes = genePagePropertyTypes;
    }

    @Inject
    void setDifferentialGeneProfileService(DifferentialGeneProfileService differentialGeneProfileService) {
        this.differentialGeneProfileService = differentialGeneProfileService;
    }

    @RequestMapping(value = "/genes/{identifier:.*}")
    public String showGenePage(@RequestParam(required = false) Double cutoff, @PathVariable String identifier, Model model) {

        DifferentialRequestPreferences requestPreferences = new DifferentialRequestPreferences();
        if (cutoff != null){
            requestPreferences.setCutoff(cutoff);
        }

        model.addAttribute("preferences", requestPreferences);

        DifferentialGeneProfileProperties differentialProfilesListMapForIdentifier =
                differentialGeneProfileService.getDifferentialProfilesListMapForIdentifier(identifier, requestPreferences.getCutoff());

        model.addAttribute("geneProfiles", differentialProfilesListMapForIdentifier);

        model.addAttribute("geneProfiles", differentialProfilesListMapForIdentifier);

        return showGenePage(identifier, model);
    }

    @Override
    List<String> getPagePropertyTypes() {
        return Lists.newArrayList(genePagePropertyTypes.split(","));
    }

    @Override
    protected BioEntityPropertiesService getBioEntityPropertiesService(String identifier) {
        genePropertiesService.init(identifier, ENTITY_NAME_PROPERTY_TYPE, getPagePropertyTypes());
        return genePropertiesService;
    }

}