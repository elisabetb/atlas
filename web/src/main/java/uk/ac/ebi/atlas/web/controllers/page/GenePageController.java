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
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.ac.ebi.atlas.geneindex.SolrClient;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Controller
@Scope("request")
public class GenePageController {

    static final String PROPERTY_TYPE_SYMBOL = "symbol";

    static final String PROPERTY_TYPE_DESCRIPTION = "description";

    SolrClient solrClient;

    @Value("#{configuration['index.types.genepage']}")
    private String genePagePropertyTypes;

    @Inject
    GenePageController(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    @RequestMapping(value = "/genes/{identifier}")
    public String showGenePage(@PathVariable String identifier, Model model) {

        model.addAttribute("property_types", filterPropertyTypes());

        String species = solrClient.findSpeciesForGeneId(identifier);
        model.addAttribute("species", species);

        Multimap<String, String> properties = solrClient.fetchGenePageProperties(identifier);
        model.addAttribute("properties", properties);

        // there should be only one element of this kind
        model.addAttribute(PROPERTY_TYPE_SYMBOL, properties.get(PROPERTY_TYPE_SYMBOL).iterator().next());

        // cleanup gene description
        String description = properties.get(PROPERTY_TYPE_DESCRIPTION).iterator().next();
        if (description.contains("[")) {
            description = description.substring(0, description.indexOf("["));
        }
        model.addAttribute(PROPERTY_TYPE_DESCRIPTION, description);

        return "gene";
    }

    List<String> filterPropertyTypes() {
        String[] split = genePagePropertyTypes.split(",");
        List<String> allPropertyTypes = Arrays.asList(split);
        List<String> filteredPropertyTypes = Lists.newArrayList();
        for (String property_type : allPropertyTypes) {
            if (!property_type.equals(PROPERTY_TYPE_SYMBOL) && !property_type.equals(PROPERTY_TYPE_DESCRIPTION)) {
                filteredPropertyTypes.add(property_type);
            }
        }
        return filteredPropertyTypes;
    }
}