
package uk.ac.ebi.atlas.model.experiment.baseline.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.atlas.model.experiment.baseline.Factor;
import uk.ac.ebi.atlas.model.experiment.baseline.impl.FactorSet;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class FactorSetTest {

    private FactorSet subject;

    private Factor factorOne = new Factor("type1", "value1");
    private Factor factorTwo = new Factor("type2", "value2");
    private Factor factorThree = new Factor("type3", "value3");

    @Before
    public void setUp() throws Exception {
        subject = new FactorSet();
        subject.add(factorOne).add(factorTwo).add(factorThree);
    }

    @Test
    public void factorByTypeShouldSucceed() throws Exception {
        assertThat(subject.factorOfType("TYPE2"), is(factorTwo));
    }

    @Test
    public void factorByTypeShouldFail() throws Exception {
        assertThat(subject.factorOfType("type2"), is(nullValue()));
        assertThat(subject.factorOfType("typeX"), is(nullValue()));
    }

    @Test
    public void testContainsAll() throws Exception {
        assertThat(subject.containsAll(Sets.newHashSet(factorOne, factorThree)), is(true));
        assertThat(subject.containsAll(Sets.newHashSet(factorTwo)), is(true));

        Factor factorX = new Factor("typeX", "valueX");
        assertThat(subject.containsAll(Sets.newHashSet(factorTwo, factorX)), is(false));

        Factor factorEqualsToFactor2 = new Factor("type2", "value2");
        assertThat(subject.containsAll(Sets.newHashSet(factorEqualsToFactor2)), is(true));

        assertThat(subject.containsAll(Sets.newHashSet(factorEqualsToFactor2, factorTwo, factorThree)), is(true));

    }

    @Test
    public void equalsShouldSucceed() {

        FactorSet equalsToSubject = new FactorSet().add(factorTwo).add(factorOne).add(factorThree);

        assertThat(subject.equals(equalsToSubject), is(true));

        equalsToSubject = new FactorSet().add(new Factor("type2", "value2")).add(factorOne).add(factorThree);

        assertThat(subject.equals(equalsToSubject), is(true));

        FactorSet equalsToSubjectWithDuplicates = new FactorSet().add(factorTwo).add(factorTwo).add(factorOne).add(factorThree);

        assertThat(subject.equals(equalsToSubjectWithDuplicates), is(true));

    }

    @Test
    public void equalsShouldFail() {

        FactorSet differentFactors = new FactorSet().add(new Factor("typeX", "valueX")).add(factorThree);

        assertThat(subject.equals(differentFactors), is(false));

        FactorSet subsetOfSubject = new FactorSet().add(factorTwo).add(factorThree);

        assertThat(subject.equals(subsetOfSubject), is(false));

        FactorSet supersetOfSubject = new FactorSet().add(factorTwo).add(factorThree).add(factorOne).add(new Factor("typeX", "valueX"));

        assertThat(subject.equals(supersetOfSubject), is(false));
    }

    @Test
    public void overlapShouldSucceed() throws Exception {

        List<Factor> overlappingFactors = Lists.newArrayList(factorTwo, new Factor("typeX", "valueX"));

        assertThat(subject.overlapsWith(overlappingFactors), is(true));

        overlappingFactors = Lists.newArrayList(factorTwo);

        assertThat(subject.overlapsWith(overlappingFactors), is(true));

        overlappingFactors = Lists.newArrayList(factorTwo, factorOne, factorThree,
                factorTwo, new Factor("typeX", "valueX"));

        assertThat(subject.overlapsWith(overlappingFactors), is(true));

    }

    @Test
    public void overlapShouldFail() throws Exception {

        FactorSet overlappingFactorSet = new FactorSet().add(new Factor("typeX", "valueX"));

        assertThat(subject.equals(overlappingFactorSet), is(false));

        overlappingFactorSet = new FactorSet();

        assertThat(subject.equals(overlappingFactorSet), is(false));
    }


    @Test
    public void testRemove() throws Exception {
        List<Factor> afterRemoval = Lists.newArrayList(factorThree);
        assertThat(subject.without(Lists.newArrayList(factorOne, factorTwo)), is(afterRemoval));
    }

    @Test
    public void containsShouldSucceed() throws Exception {
        assertThat(subject.contains(factorOne), is(true));
    }

    @Test
    public void containsShouldFail() throws Exception {
        assertThat(subject.contains(new Factor("typeX", "valueX")), is(false));
    }

    @Test
    public void testDoesNotContainAnyOrganism() {
        assertThat(subject.containsOnlyOrganism(), is(false));
    }

    @Test
    public void testContainsOnlyOrganism() {
        FactorSet subjectContainsOnlyOrganism = new FactorSet();
        assertThat(subjectContainsOnlyOrganism.containsOnlyOrganism(), is(false));

        subjectContainsOnlyOrganism.add(new Factor("ORGANISM", "Homo sapiens"));

        assertThat(subjectContainsOnlyOrganism.containsOnlyOrganism(), is(true));

        subjectContainsOnlyOrganism.add(new Factor("type1", "value1"));

        assertThat(subjectContainsOnlyOrganism.containsOnlyOrganism(), is(false));
    }

    @Test
    public void testFactorWithMultipleOntologyTerms() {

    }
}