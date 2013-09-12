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

package uk.ac.ebi.atlas.web.controllers.page.crossexperiment;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.atlas.model.differential.*;
import uk.ac.ebi.atlas.web.DifferentialRequestPreferences;

@Controller
@Scope("request")
public class BioentitiesController {

    @RequestMapping(value = "/query")
    public String showResultPage(@RequestParam (required = false) String condition, Model model) {

        model.addAttribute("entityIdentifier", condition);

        //model.addAttribute("differentialBioentityExpressions", differentialBioentityExpressions);

        //DifferentialExpressions differentialExpressions = mockDifferentialExpressionLimits();
        model.addAttribute("geneProfiles", mockDifferentialExpressions());

        model.addAttribute("preferences", new DifferentialRequestPreferences());
        return "bioEntities";
    }

    private DifferentialExpressions mockDifferentialExpressions() {
        DifferentialExpressions differentialExpressions = new DifferentialExpressions();
        differentialExpressions.add(mockDifferentialBioentityExpression(1, true));
        differentialExpressions.add(mockDifferentialBioentityExpression(2, true));
        differentialExpressions.add(mockDifferentialBioentityExpression(3, false));
        return differentialExpressions;
    }

    private DifferentialBioentityExpression mockDifferentialBioentityExpression(int index, boolean upRegulated) {
        DifferentialExpression differentialExpression = mockDifferentialExpression(index, upRegulated);

        return new DifferentialBioentityExpression("bioentityId" + index, "experimentAccession" + index,
                                                    differentialExpression, "species" + index, "designElement" + index);
    }

    private DifferentialExpression mockDifferentialExpression(int index, boolean upRegulated) {
        return new DifferentialExpression(index,upRegulated? index : -index,
                    new Contrast("contrastId" + index,"arrayDesignAccession" + index, new AssayGroup("referenceAssayAccession" + index)
                            ,new AssayGroup("testAssayAccession" + index),"contrastDisplayName" + index));
    }
}
