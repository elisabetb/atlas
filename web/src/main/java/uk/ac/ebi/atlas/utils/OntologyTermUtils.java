package uk.ac.ebi.atlas.utils;

import uk.ac.ebi.atlas.model.OntologyTerm;

import java.util.Set;

public class OntologyTermUtils {

    public static final String ONTOLOGY_TERM_DELIMITER = " ";

    public static String joinSources(Set<OntologyTerm> ontologyTerms) {
        StringBuilder sb = new StringBuilder();
        for (OntologyTerm ontologyTerm : ontologyTerms) {
            sb.append(ontologyTerm.source() + ONTOLOGY_TERM_DELIMITER);
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public static String joinIds(Set<OntologyTerm> ontologyTerms) {
        StringBuilder sb = new StringBuilder();
        for (OntologyTerm ontologyTerm : ontologyTerms) {
            sb.append(ontologyTerm.id() + ONTOLOGY_TERM_DELIMITER);
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public static String joinURIs(Set<OntologyTerm> ontologyTerms) {
        StringBuilder sb = new StringBuilder();
        for (OntologyTerm ontologyTerm : ontologyTerms) {
            sb.append(ontologyTerm.uri() + ONTOLOGY_TERM_DELIMITER);
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}
