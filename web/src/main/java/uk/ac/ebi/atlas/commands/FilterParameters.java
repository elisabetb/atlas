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

package uk.ac.ebi.atlas.commands;

import com.google.common.base.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import uk.ac.ebi.atlas.model.Experiment;
import uk.ac.ebi.atlas.model.Factor;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

//ToDo: this class now smells, GeneProfileInputStreamFilter now only requires queryFactors from this class and that is available also from other beans.
public class FilterParameters {

    public static final String FACTOR_VALUE_SEPARATOR = ":";

    private String geneQuery;

    private Set<Factor> selectedFilterFactors;

    private Double cutoff;


    private Set<Factor> queryFactors;

    private String queryFactorType;

    private FilterParameters(Builder builder) {
        geneQuery = builder.geneQuery;
        selectedFilterFactors = builder.selectedFilterFactors;
        cutoff = builder.cutoff;
        queryFactors = builder.queryFactors;
        queryFactorType = builder.queryFactorType;

    }

    public String getGeneQuery() {
        return geneQuery;
    }

    public Set<Factor> getSelectedFilterFactors() {
        return selectedFilterFactors;
    }

    public Double getCutoff() {
        return cutoff;
    }

    public Set<Factor> getQueryFactors() {
        return queryFactors;
    }

    public String getQueryFactorType() {
        return queryFactorType;
    }

    public String formattedQueryFactorType() {
        return formatForDisplay(queryFactorType);
    }

    protected String formatForDisplay(String queryFactorType){
        return StringUtils.capitalize(queryFactorType.replaceAll("_", " ").toLowerCase());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass())
                    .add("geneQuery", geneQuery)
                    .add("selectedFilterFactors", selectedFilterFactors)
                    .add("cutoff", cutoff)
                    .add("queryFactors", queryFactors)
                    .add("queryFactorType", queryFactorType)
                .toString();
    }

    @Named("filterParametersBuilder")
    @Scope("request")
    public static class Builder {

        private Double cutoff;

        private String queryFactorType;

        private String geneQuery;
        private Set<Factor> selectedFilterFactors = new HashSet<>();
        private Set<Factor> queryFactors = new HashSet<>();
        private Set<String> queryFactorValues = Collections.EMPTY_SET;
        private Set<String> serializedFilterFactorValues = Collections.EMPTY_SET;

        Experiment experiment;

        public Builder() {
        }

        public Builder forExperiment(Experiment experiment) {
            this.experiment = experiment;
            return this;
        }

        public Builder withCutoff(Double cutoff) {
            this.cutoff = cutoff;
            return this;
        }

        public Builder withQueryFactorType(String queryFactorType) {
            this.queryFactorType = queryFactorType;
            return this;
        }

        public Builder withFilterFactorValues(Set<String> serializedFilterFactorValues) {
            if(CollectionUtils.isNotEmpty(serializedFilterFactorValues)){
                this.serializedFilterFactorValues = serializedFilterFactorValues;
            }
            return this;
        }

        protected Factor buildFromSerializedFilterFactorValues(Experiment experiment, String serializedFilterFactorValues) {
            String[] split = serializedFilterFactorValues.split(FACTOR_VALUE_SEPARATOR);
            if (split.length == 2) {
                String name = experiment.getFactorName(split[0], split[1]);
                return new Factor(split[0], name, split[1]);
            }
            throw new IllegalArgumentException("serialized Factor string should be colon separated between type and value.");
        }

        public Builder withGeneQuery(String geneQuery) {
            this.geneQuery = geneQuery;
            return this;
        }

        public Builder withQueryFactorValues(Set<String> queryFactorValuesString) {
            if(CollectionUtils.isNotEmpty(queryFactorValuesString)){
                this.queryFactorValues = queryFactorValuesString;
            }
            return this;
        }

        public FilterParameters build() {
            checkState(experiment != null, "Please invoke forExperiment before build");

            if (StringUtils.isBlank(queryFactorType)){
                queryFactorType = experiment.getDefaultQueryFactorType();
            }
            if (CollectionUtils.isEmpty(serializedFilterFactorValues)){
                for (Factor factor : experiment.getDefaultFilterFactors()){
                    //ToDo: this should be removed as soon as we have a proper builder for the Experiment class, so that experiment can fetch DefaultFactorValue names once and for all at load time
                    String factorValueName = experiment.getFactorName(factor.getType(), factor.getValue());
                    factor.setName(factorValueName);
                    selectedFilterFactors.addAll(experiment.getDefaultFilterFactors());
                }
            } else {
                for (String serializedFilterFactorValue : serializedFilterFactorValues) {
                    this.selectedFilterFactors.add(buildFromSerializedFilterFactorValues(experiment, serializedFilterFactorValue));
                }
            }
            for (String queryFactorValues : this.queryFactorValues) {
                this.queryFactors.add(new Factor(queryFactorType, queryFactorValues));
            }
            return new FilterParameters(this);
        }
    }
}
