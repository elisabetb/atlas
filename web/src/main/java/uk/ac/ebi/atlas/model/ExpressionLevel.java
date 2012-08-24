package uk.ac.ebi.atlas.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

public class ExpressionLevel implements Comparable<ExpressionLevel> {

    private String transcriptId;

    private Set<FactorValue> factorValues = new HashSet<>();

    private double rpkm;

    public ExpressionLevel(String transcriptId, double rpkm, Collection<FactorValue> factorValues) {
        this.transcriptId = checkNotNull(transcriptId);
        this.rpkm = rpkm;
        if (factorValues != null) {
            this.factorValues.addAll(factorValues);
        }
    }

    public ExpressionLevel(String transcriptId, double rpkm) {
        this(transcriptId, rpkm, null);
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public Set<FactorValue> getFactorValues() {
        return factorValues;
    }

    public double getRpkm() {
        return rpkm;
    }

    public ExpressionLevel addFactorValue(String factor, String value) {
        factorValues.add(new FactorValue(factor, value));
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transcriptId, factorValues, rpkm);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExpressionLevel other = (ExpressionLevel) obj;

        return Objects.equals(this.transcriptId, other.transcriptId)
                && Objects.equals(this.factorValues, other.factorValues)
                && Objects.equals(this.rpkm, other.rpkm);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("transcriptId", transcriptId)
                .add("rpkm", rpkm)
                .add("factorValues", factorValues).toString();
    }

    @Override
    public int compareTo(ExpressionLevel expressionLevel) {
        final int rpkmDiff = Double.compare(rpkm, expressionLevel.rpkm);
        if (rpkmDiff != 0) {
            return rpkmDiff;
        }

        return transcriptId.compareTo(expressionLevel.transcriptId);

    }
}
