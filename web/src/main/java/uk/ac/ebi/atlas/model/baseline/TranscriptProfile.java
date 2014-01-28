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

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TranscriptProfile implements Serializable {

    private String geneId;

    private String transcriptId;

    private List<Double> expressions = Lists.newArrayList();

    public TranscriptProfile(String geneId, String transcriptId, List<String> expressions) {
        this.geneId = geneId;
        this.transcriptId = transcriptId;
        for (String expression: expressions) {
            this.expressions.add(parseExpressionString(expression));
        }
    }

    private double parseExpressionString(String expression) {
        return "FAIL".equals(expression) ? 0 : Double.parseDouble(expression);
    }

    public String getGeneId() {
        return geneId;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public List<Double> getExpressions() {
        return Collections.unmodifiableList(expressions);
    }

    public double getExpression(int index) {
        return expressions.get(index);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TranscriptProfile other = (TranscriptProfile) obj;

        return Objects.equals(geneId, other.geneId) && Objects.equals(transcriptId, other.transcriptId)
                && Objects.equals(expressions, other.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(geneId, transcriptId, expressions);
    }

    @Override
    public String toString(){
        return "TranscriptProfile: geneId = " + geneId
                + ", transcriptId = " + transcriptId
                + ", expressions = " + expressions;
    }

}
