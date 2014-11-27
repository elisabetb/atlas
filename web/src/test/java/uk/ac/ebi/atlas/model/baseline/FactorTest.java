package uk.ac.ebi.atlas.model.baseline;

import com.google.common.collect.ImmutableSet;
import uk.ac.ebi.atlas.model.OntologyTerm;

import java.util.Set;

public class FactorTest {

    private OntologyTerm ontologyTerm1 = new OntologyTerm("UBERON_0002349", "http://purl.obolibrary.org/obo/");
    private OntologyTerm ontologyTerm2 = new OntologyTerm("UBERON_0002082", "http://purl.obolibrary.org/obo/");
    private Factor subject = new Factor("type", "value", ontologyTerm1, ontologyTerm2);

}