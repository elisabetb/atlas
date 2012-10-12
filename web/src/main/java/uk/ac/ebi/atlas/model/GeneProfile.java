package uk.ac.ebi.atlas.model;

import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class GeneProfile implements Iterable<Expression> {

    private String geneId;

    //ToDo: ...Expression is not implementing equality and hashcode but here we are using it in a Set...
    private Set<Expression> expressions = new HashSet<>();

    private GeneProfile(String geneId) {
        this.geneId = geneId;
    }

    public static Builder forGeneId(String geneId) {
        return new Builder(geneId);
    }

    public String getGeneId() {
        return geneId;
    }

    public int getGeneSpecificity() {
        return expressions.size();
    }

    @Override
    public Iterator<Expression> iterator() {
        return expressions.iterator();
    }

    public Iterable<GeneExpression> organismPartExpressions(Set<String> organismParts){

        Set<GeneExpression> filteredExpressions = new HashSet<GeneExpression>();
        for (Expression expression: this){
            if (CollectionUtils.isEmpty(organismParts) || organismParts.contains(expression.getOrganismPart())){
                filteredExpressions.add(new GeneExpression(geneId, expression, getGeneSpecificity()));
            }
        }
        return filteredExpressions;
    }

    public static class Builder {

        private GeneProfile geneProfile;

        private static double DEFAULT_CUTOFF_VALUE = 0D;

        private double cutoffValue = DEFAULT_CUTOFF_VALUE;

        public Builder(String geneId) {
            geneProfile = new GeneProfile(geneId);
        }

        public Builder addExpression(Expression expression) {

            if (expression.isGreaterThan(cutoffValue)) {
                geneProfile.expressions.add(expression);
            }

            return this;
        }

        public Builder withCutoff(double cutoff) {
            checkState(geneProfile.expressions.size() == 0, "withCutoff should be invoked before adding any Expression!");
            checkArgument(cutoff >= 0, "cutoff must be >= 0");
            this.cutoffValue = cutoff;

            return this;
        }

        public GeneProfile create() {
            return geneProfile;
        }

        public boolean containsExpressions() {
            return !geneProfile.expressions.isEmpty();
        }

    }
}
