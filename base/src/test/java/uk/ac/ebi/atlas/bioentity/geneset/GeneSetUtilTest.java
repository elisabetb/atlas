package uk.ac.ebi.atlas.bioentity.geneset;

import org.junit.Test;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.search.SemanticQueryTerm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

// This class extends GeneSetUtil to implicitly call the helper class default constructor, so coverage is 100%
// Semantically it explicitly states that the tested class is not final, which might or not be what you want...
public class GeneSetUtilTest extends GeneSetUtil {

    private final SemanticQuery GENE_QUERY_WITH_REACTOME_CATEGORY =
            SemanticQuery.create(SemanticQueryTerm.create("foo", "pathwayid"));
    private final SemanticQuery GENE_QUERY_WITH_INTERPRO_CATEGORY =
            SemanticQuery.create(SemanticQueryTerm.create("foo", "interpro"));
    private final SemanticQuery GENE_QUERY_WITH_GO_CATEGORY =
            SemanticQuery.create(SemanticQueryTerm.create("foo", "go"));
    private final SemanticQuery GENE_QUERY_WITH_PO_CATEGORY =
            SemanticQuery.create(SemanticQueryTerm.create("foo", "po"));

    private final SemanticQuery GENE_QUERY_WITH_REACTOME_VALUE =
            SemanticQuery.create(SemanticQueryTerm.create("R-HSA-123"));
    private final SemanticQuery GENE_QUERY_WITH_INTERPRO_VALUE =
            SemanticQuery.create(SemanticQueryTerm.create("IPR123"));
    private final SemanticQuery GENE_QUERY_WITH_GO_VALUE =
            SemanticQuery.create(SemanticQueryTerm.create("GO:123"));
    private final SemanticQuery GENE_QUERY_WITH_PO_VALUE =
            SemanticQuery.create(SemanticQueryTerm.create("PO:123"));
    private final SemanticQuery GENE_QUERY_WITH_PLANT_REACTOME_VALUE =
            SemanticQuery.create(SemanticQueryTerm.create("123456"));

    private final SemanticQuery GENE_QUERY_WITH_GENE_SET_CATEGORIES =
            SemanticQuery.create(
                    SemanticQueryTerm.create("foo", "pathwayid"),
                    SemanticQueryTerm.create("foo", "interpro"),
                    SemanticQueryTerm.create("foo", "go"),
                    SemanticQueryTerm.create("foo", "po"));
    private final SemanticQuery GENE_QUERY_WITH_GENE_SET_VALUES =
            SemanticQuery.create(
                    SemanticQueryTerm.create("R-HSA-123"),
                    SemanticQueryTerm.create("IPR123"),
                    SemanticQueryTerm.create("GO:123"),
                    SemanticQueryTerm.create("PO:123"),
                    SemanticQueryTerm.create("123456"));

    private final SemanticQuery GENERIC_GENE_QUERY = SemanticQuery.create("zinc finger");
    private final SemanticQuery TRICKY_GENE_QUERY =
            SemanticQuery.create(SemanticQueryTerm.create("IPR9000", "symbol"));
    private final SemanticQuery EMPTY_QUERY = SemanticQuery.create();

    @Test
    public void testIsGeneSet() throws Exception {
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_REACTOME_CATEGORY), is(true));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_INTERPRO_CATEGORY), is(true));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_GO_CATEGORY), is(true));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_PO_CATEGORY), is(true));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_REACTOME_VALUE), is(true));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_INTERPRO_VALUE), is(true));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_GO_VALUE), is(true));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_PO_VALUE), is(true));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_PLANT_REACTOME_VALUE), is(true));
    }

    @Test
    public void multiTermQuery() throws Exception {
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_GENE_SET_CATEGORIES), is(false));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENE_QUERY_WITH_GENE_SET_VALUES), is(false));
    }

    @Test
    public void emptyQuery() throws Exception {
        assertThat(matchesGeneSetCategoryOrGeneSetValue(GENERIC_GENE_QUERY), is (false));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(TRICKY_GENE_QUERY), is(false));
        assertThat(matchesGeneSetCategoryOrGeneSetValue(EMPTY_QUERY), is(false));
    }
}