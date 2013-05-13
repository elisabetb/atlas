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

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.ac.ebi.atlas.web.BaselineRequestPreferences;
import uk.ac.ebi.atlas.web.ExperimentPageRequestPreferences;

public class RequestPreferencesValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return BaselineRequestPreferences.class.equals(aClass);
    }

    @Override
    public void validate(Object object, Errors errors) {
        ExperimentPageRequestPreferences requestPreferences = (ExperimentPageRequestPreferences) object;
        if (requestPreferences.isGeneSetMatch() && StringUtils.isBlank(requestPreferences.getGeneQuery())) {
            errors.reject("geneSetWithEmptyQuery", "Please specify gene set id in the 'Gene Query' text area or uncheck the 'Gene set' option");
        }
    }
}
