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
import uk.ac.ebi.atlas.geneindex.SolrQueryService;
import uk.ac.ebi.atlas.utils.ReactomeBiomartClient;

import javax.inject.Inject;
import java.util.List;

@Controller
@Scope("request")
public class GeneSetPageController extends BioEntityPageController {

    @Override
    List<String> getPagePropertyTypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    BioEntityPropertiesService getBioEntityPropertiesService(String identifier) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

/*
    private ReactomeBiomartClient reactomeBiomartClient;

    private SolrQueryService solrServer;

    private String geneSetPagePropertyTypes;

    @Inject
    public GeneSetPageController(ReactomeBiomartClient reactomeBiomartClient, SolrQueryService solrServer) {
        this.reactomeBiomartClient = reactomeBiomartClient;
        this.solrServer = solrServer;
    }

    @Value("#{configuration['index.types.genesetpage']}")
    void setGeneSetPagePropertyTypes(String geneSetPagePropertyTypes) {
        this.geneSetPagePropertyTypes = geneSetPagePropertyTypes;
    }

    @RequestMapping(value = "/genesets/{identifier:.*}")
    public String showGenePage(@PathVariable String identifier, Model model) {
        return super.showGenePage(identifier, model);

    }

    @Override
    protected void initModel(String identifier, Model model) {
        model.addAttribute("species", solrServer.getSpeciesForPropertyValue(identifier));
        model.addAttribute("names", Lists.newArrayList(identifier));
        model.addAttribute("description", reactomeBiomartClient.fetchPathwayName(identifier));
    }

    @Override
    List<String> getPagePropertyTypes() {
        return Lists.newArrayList(geneSetPagePropertyTypes.split(","));
    }

    @Override
    String getEntityNamePropertyType() {
        throw new UnsupportedOperationException();
    }
*/

}