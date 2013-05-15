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

//ToDo: this test is too complex. It's not really unit test on GeneProfile because it tests all the chain of builder , preconditions , etc....

package uk.ac.ebi.atlas.model.baseline;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.atlas.geneannotation.GeneNamesProvider;
import uk.ac.ebi.atlas.model.baseline.impl.FactorSet;

import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BaselineProfileTest {

    private static final String QUERY_FACTOR_TYPE = "ORGANISM_PART";
    @Mock
    private GeneNamesProvider geneNamesProviderMock;

    private static String GENE_ID = "geneId_1";
    private static String GENE_NAME = "geneName_1";

    private Factor factor1 = new Factor(QUERY_FACTOR_TYPE, "nose");
    private Factor factor2 = new Factor(QUERY_FACTOR_TYPE, "trunk");
    private Factor factor3 = new Factor(QUERY_FACTOR_TYPE, "head");
    private Factor factor4 = new Factor(QUERY_FACTOR_TYPE, "billabong");

    private BaselineExpression expression_1 = new BaselineExpression(2.2D, new FactorSet().add(factor1));
    private BaselineExpression expression_2 = new BaselineExpression(3D, new FactorSet().add(factor2));
    private BaselineExpression expression_3 = new BaselineExpression(3.001D, new FactorSet().add(factor3));

    private BaselineProfile subject;

    private double cutoff  = 0.5;
    //private GeneProfile.Builder geneProfileBuilder;

    @Before
    public void setUp() throws Exception {
        when(geneNamesProviderMock.getGeneName(GENE_ID)).thenReturn(GENE_NAME);

        subject = new BaselineProfile(GENE_ID);

        subject.add(QUERY_FACTOR_TYPE, expression_1).add(QUERY_FACTOR_TYPE, expression_2).add(QUERY_FACTOR_TYPE, expression_3);

    }

    @Test
    public void testGetGeneSpecificity() throws Exception {
        assertThat(subject.getSpecificity(), is(3));
    }

    @Test
    public void averageExpressionLevelOnNullCollection() {
        assertThat(subject.getAverageExpressionLevelOn(null), is(0D));
    }

    @Test
    public void averageExpressionLevelOnEmptyCollection() {
        assertThat(subject.getAverageExpressionLevelOn(new HashSet<Factor>()), is(0D));
    }

    @Test
    public void averageExpressionLevel() {
        double averageExpressionLevel = subject.getAverageExpressionLevelOn(Sets.newHashSet(factor1, factor3));
        assertThat(averageExpressionLevel, is(2.6005000000000003D));

        averageExpressionLevel = subject.getAverageExpressionLevelOn(Sets.newHashSet(factor1, factor3,
                new Factor(QUERY_FACTOR_TYPE, "leg")));
        assertThat(averageExpressionLevel, is(1.733666666666667D));
    }

    @Test
    public void testAddAll(){

        BaselineProfile sumProfile = subject.addAll(buildOtherProfile());
        assertThat(sumProfile.getId(), is(subject.getId()));
        assertThat(sumProfile.getExpressionLevel(factor1), is(subject.getExpressionLevel(factor1) + 1D));
        assertThat(sumProfile.getExpressionLevel(factor2), is(subject.getExpressionLevel(factor2) + 2D));
        assertThat(sumProfile.getExpressionLevel(factor3), is(subject.getExpressionLevel(factor3) + 3D));
        assertThat(sumProfile.getExpressionLevel(factor4), is(300D));

    }

    @Test
    public void addAllShouldPreserveLevelsThatAreNotExpressedInOtherProfile(){

        BaselineProfile otherProfile = new BaselineProfile("other profile").add(QUERY_FACTOR_TYPE, expression_2);

        BaselineProfile sumProfile = subject.addAll(otherProfile);
        assertThat(sumProfile.getId(), is(subject.getId()));
        assertThat(sumProfile.getExpressionLevel(factor1), is(subject.getExpressionLevel(factor1)));
        assertThat(sumProfile.getExpressionLevel(factor2), is(subject.getExpressionLevel(factor2) + expression_2.getLevel()));
        assertThat(sumProfile.getExpressionLevel(factor3), is(subject.getExpressionLevel(factor3)));
        assertThat(sumProfile.getExpressionLevel(factor4), is(0D));

    }

    @Test
    public void testFold(){

        BaselineProfile sumProfile = subject.foldProfile(3);
        assertThat(sumProfile.getId(), is(subject.getId()));
        assertThat(sumProfile.getExpressionLevel(factor1), is(0.7D));
        assertThat(sumProfile.getExpressionLevel(factor2), is(1.0D));
        assertThat(sumProfile.getExpressionLevel(factor3), is(1.0D));
        assertThat(sumProfile.getExpressionLevel(factor4), is(0D));

    }

    BaselineProfile buildOtherProfile(){

        BaselineExpression expression_1 = new BaselineExpression(1D, new FactorSet().add(factor1));
        BaselineExpression expression_2 = new BaselineExpression(2D, new FactorSet().add(factor2));
        BaselineExpression expression_3 = new BaselineExpression(3D, new FactorSet().add(factor3));
        BaselineExpression expression_4 = new BaselineExpression(300D, new FactorSet().add(factor4));

        BaselineProfile baselineProfile = new BaselineProfile("OTHER_ID");

        return baselineProfile.add(QUERY_FACTOR_TYPE, expression_1).add(QUERY_FACTOR_TYPE, expression_2)
                                .add(QUERY_FACTOR_TYPE, expression_3).add(QUERY_FACTOR_TYPE, expression_4);
    }

}