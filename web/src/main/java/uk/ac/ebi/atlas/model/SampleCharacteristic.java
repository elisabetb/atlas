package uk.ac.ebi.atlas.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import uk.ac.ebi.atlas.utils.OntologyTermUtils;

import javax.annotation.Nullable;
import java.util.Set;

@AutoValue
public abstract class SampleCharacteristic {

    public static SampleCharacteristic create(String header, String value) {
        return create(header, value, new ImmutableSet.Builder<OntologyTerm>().build());
    }

    // TODO Think this over, too!
    public static SampleCharacteristic create(String header, String value, OntologyTerm ontologyTerm) {

        ImmutableSet.Builder<OntologyTerm> ontologyTermBuilder = new ImmutableSet.Builder<>();
        ontologyTermBuilder.add(ontologyTerm);
        return create(header, value, ontologyTermBuilder.build());
    }

//    public static SampleCharacteristic create(String header, String value, Set<OntologyTerm> ontologyTerms) {
//        Optional<OntologyTerm> ontologyTermOptional = (ontologyTerm == null) ? Optional.<OntologyTerm>absent() : Optional.of(ontologyTerm);
//        return create(header, value, ontologyTermOptional);
//    }

    public static SampleCharacteristic create(String header, String value, Set<OntologyTerm> ontologyTerms) {
        return new AutoValue_SampleCharacteristic(header, value, ontologyTerms);
    }

    public abstract String header();
    public abstract String value();
    public abstract Set<OntologyTerm> valueOntologyTerms();

    // TODO find usages and replace this call directly to OntologyTermUtils (?)
    public @Nullable String getValueOntologyTermId() {
        return valueOntologyTerms().isEmpty() ? null : OntologyTermUtils.joinIds(valueOntologyTerms());
    }

    // TODO find usages and replace this call directly to OntologyTermUtils (?)
    public @Nullable String getValueOntologyTermUri() {
        return valueOntologyTerms().isEmpty() ? null : OntologyTermUtils.joinURIs(valueOntologyTerms());
    }
}
