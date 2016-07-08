package uk.ac.ebi.atlas.experimentpage.context;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.atlas.model.baseline.Factor;
import uk.ac.ebi.atlas.web.BaselineRequestPreferences;
import uk.ac.ebi.atlas.web.GeneQuery;
import uk.ac.ebi.atlas.web.SemanticQueryTerm;

import java.util.SortedSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestContextTest {

    private BaselineRequestContext subject;

    @Mock
    private BaselineRequestPreferences preferencesMock;

    @Mock
    private Factor factor1Mock;

    @Mock
    private Factor factor2Mock;

    @Before
    public void setUp() throws Exception {
        subject = new BaselineRequestContext();
        subject.setRequestPreferences(preferencesMock);

        when(preferencesMock.getGeneQuery()).thenReturn(GeneQuery.create("GENE_QUERY"));
        when(preferencesMock.getHeatmapMatrixSize()).thenReturn(42);
        when(preferencesMock.getCutoff()).thenReturn(0.05);
        when(preferencesMock.isSpecific()).thenReturn(true);
    }

    @Test
    public void testGetGeneQuery() throws Exception {
        assertThat(subject.getGeneQuery(), is(GeneQuery.create(ImmutableSet.of(SemanticQueryTerm.create("GENE_QUERY")))));
    }

    @Test
    public void testGetHeatmapMatrixSize() throws Exception {
        assertThat(subject.getHeatmapMatrixSize(), is(42));
    }

    @Test
    public void testGetSelectedQueryFactors() throws Exception {
        assertThat(subject.getSelectedQueryFactors(), is(nullValue()));
    }

    @Test
    public void testGetFilteredBySpecies() throws Exception {
        assertThat(subject.getFilteredBySpecies(), is(nullValue()));
    }

    @Test
    public void testGetCutoff() throws Exception {
        assertThat(subject.getCutoff(), is(0.05));
    }

    @Test
    public void testIsSpecific() throws Exception {
        assertThat(subject.isSpecific(), is(true));
    }

    @Test
    public void testGetAllQueryFactors() throws Exception {
        assertThat(subject.getAllQueryFactors(), is(nullValue()));
    }

    @Test
    public void testSetAllQueryFactors() throws Exception {
        SortedSet<Factor> set = Sets.newTreeSet();
        set.add(factor1Mock);
        set.add(factor2Mock);
        subject.setAllQueryFactors(set);
        assertThat(subject.getAllQueryFactors(), contains(factor1Mock, factor2Mock));
    }

    @Test
    public void testSetSelectedQueryFactors() throws Exception {
        subject.setSelectedQueryFactors(Sets.newHashSet(factor1Mock, factor2Mock));
        assertThat(subject.getSelectedQueryFactors(), hasItems(factor1Mock, factor2Mock));
    }

    @Test
    public void testSetFilteredBySpecies() throws Exception {
        subject.setFilteredBySpecies("homo sapiens");
        assertThat(subject.getFilteredBySpecies(), is("homo sapiens"));
    }

    @Test(expected = NullPointerException.class)
    public void testSetRequestPreferences() throws Exception {
        subject.setRequestPreferences(null);
        subject.getCutoff();
    }

    @Test
    public void testToString() throws Exception {
        assertThat(subject.toString(), containsString("requestPreferences"));
        assertThat(subject.toString(), containsString("filteredBySpecies"));
    }
}