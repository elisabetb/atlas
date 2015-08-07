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

package uk.ac.ebi.atlas.model.baseline;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import org.apache.velocity.util.StringUtils;
import uk.ac.ebi.atlas.model.OntologyTerm;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class Factor implements Comparable<Factor> {

    private String header;
    private String type;
    private String value;
    private Set<OntologyTerm> valueOntologyTerms;

    public Factor() {}

    public Factor(String header, String value) {
        this(header, value, new OntologyTerm[0]);
    }

    public Factor(String header, String value, OntologyTerm ... valueOntologyTerms) {
        this.header = header;
        this.type = normalize(checkNotNull(header));
        this.value = checkNotNull(value);
        this.valueOntologyTerms = new ImmutableSet.Builder<OntologyTerm>().add(valueOntologyTerms).build();
    }

    public static String normalize(String type) {
        return type.replaceAll(" ", "_").toUpperCase();
    }

    public static String convertToLowerCase(String type) {
        StringBuilder result = new StringBuilder();
        String[] split = type.replaceAll("_", " ").split(" ");

        boolean firstTokenCapitalized = false;
        for (String token : split) {
            token = token.toLowerCase();

            if (!firstTokenCapitalized) {
                token = StringUtils.capitalizeFirstLetter(token);
                firstTokenCapitalized = true;
            }
            result.append(token);

        result.append(" ");
        }

        return result.toString().trim();

    }

    //same as type but un-normalized
    public String getHeader() {
        return header;
    }

    public String getValue() {
        return value;
    }

    //normalized version of header
    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, value);
    }

    // header is not part of identity, it is only for display purposes
    // ontology term is not part of identity, it is only used for display purposes (ie: the anatomogram)
    @Override
    public boolean equals(Object other) {
        return Objects.equal(this.getClass(), other.getClass())
                && Objects.equal(this.type, ((Factor) other).type)
                && Objects.equal(this.value, ((Factor) other).value);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("type", type)
                .add("value", value)
                .add("valueOntologyTerms", Arrays.asList(valueOntologyTerms))
                .toString();
    }

    @Override
    public int compareTo(Factor factor) {
        int factorCompare = type.compareTo(factor.type);
        if (factorCompare != 0) {
            return factorCompare;
        }
        return value.compareTo(factor.value);
    }

    public static SortedSet<String> getValues(Set<Factor> factors) {
        SortedSet<String> result = new TreeSet<>();
        for (Factor factor : factors) {
            result.add(factor.getValue());
        }
        return result;
    }

    public Set<OntologyTerm> getValueOntologyTerms() {
        return valueOntologyTerms;
    }

    // Used by heatmap-widget.jsp
    public @Nullable String getValueOntologyTermId() {
        if (valueOntologyTerms.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (OntologyTerm valueOntologyTerm : valueOntologyTerms) {
            sb.append(valueOntologyTerm.id()).append(" ");
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }
}
