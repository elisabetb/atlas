package uk.ac.ebi.atlas.web;

import com.google.auto.value.AutoValue;

/**
 * Created by Alfonso Muñoz-Pomer Fuentes <amunoz@ebi.ac.uk> on 12/02/2016.
 */
@AutoValue
public abstract class SemanticQueryTerm {
    public static SemanticQueryTerm create(String value) {
        return create(value, "");
    }

    public static SemanticQueryTerm create(String value, String source) {
        return new AutoValue_SemanticQueryTerm(value, source);
    }

    public abstract String value();
    public abstract String source();
}
