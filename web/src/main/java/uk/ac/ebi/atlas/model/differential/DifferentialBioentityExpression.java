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

package uk.ac.ebi.atlas.model.differential;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.atlas.model.AssayGroup;

public class DifferentialBioentityExpression {

    private String bioentityId;

    private String bioentityName;

    private String experimentAccession;

    private DifferentialExpression expression;

    private String species;

    private String designElement;

    public DifferentialBioentityExpression(String bioentityId, String bioentityName, String experimentAccession, DifferentialExpression expression,
                                           String species, String designElement) {
        this.bioentityId = bioentityId;
        this.bioentityName = bioentityName;
        this.experimentAccession = experimentAccession;
        this.expression = expression;
        this.species = species;
        this.designElement = designElement;
    }

    public String getBioentityId() {
        return bioentityId;
    }

    public String getContrastDisplayName() {
        return expression.getContrast().getDisplayName();
    }

    public String getContrastId() {
        return expression.getContrast().getId();
    }

    public AssayGroup getContrastTestAssayGroup() {
        return expression.getContrast().getTestAssayGroup();
    }

    public String getExperimentAccession() {
        return experimentAccession;
    }

    public String getExperimentPageUrl() {
        return experimentAccession + "?geneQuery=" + bioentityId + "&queryFactorValues=" + getContrastId();
    }

    public DifferentialExpression getExpression() {
        return expression;
    }

    public String getSpecies() {
        return species;
    }

    public String getDesignElement() {
        return designElement;
    }

    public String getBioentityName() {
        return StringUtils.isEmpty(bioentityName) ? bioentityId : bioentityName;
    }
}