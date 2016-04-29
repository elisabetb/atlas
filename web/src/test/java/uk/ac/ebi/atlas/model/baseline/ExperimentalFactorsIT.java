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

package uk.ac.ebi.atlas.model.baseline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.model.baseline.impl.FactorSet;
import uk.ac.ebi.atlas.trader.cache.BaselineExperimentsCache;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:solrContextIT.xml", "classpath:oracleContext.xml"})
public class ExperimentalFactorsIT {

    @Inject
    private BaselineExperimentsCache experimentsCache;

    ExperimentalFactors subject;

    @Test
    public void testSomeExperiments() throws Exception {
        testFactorsFor("E-MTAB-513");
        testFactorsFor("E-MTAB-2706");

    }


    public void testFactorsFor(String accession) throws Exception {
        BaselineExperiment experiment = experimentsCache.getExperiment(accession);
        ExperimentalFactors subject = experiment.getExperimentalFactors();

        Set<Factor> allFactors  = subject.getAllFactors();
        assertThat(allFactors.size(), greaterThan(3));

        for(Factor f: allFactors) {

            assertTrue(subject.getFactors(f.getType()).contains(f));
            assertTrue(subject.getFactorsByType().get(f.getType()).contains(f));

            Set<Factor> complement = subject.getComplementFactors(Sets.newHashSet(f));
            Set<Factor> cooccurring = subject.getCoOccurringFactors(f);

            assertTrue(allFactors.containsAll(complement));
            assertTrue(allFactors.containsAll(cooccurring));
            assertFalse(complement.contains(f));
            assertFalse(cooccurring.contains(f));

            Set<Factor> intersection = new HashSet<>(complement);
            intersection.retainAll(cooccurring);
            assertTrue(intersection.isEmpty());

            SortedSet<AssayGroupFactor> complementAssays = subject.getComplementAssayGroupFactors(Sets.newHashSet(f));
            assertEquals(complement.size(), complementAssays.size());
            for(AssayGroupFactor agf : complementAssays){
                assertFalse(subject.getFactorGroup(agf.getAssayGroupId()).contains(f));
                assertTrue(complement.contains(agf.getFactor()));


                boolean isNonDefaultType = ! agf.getType().equals(subject.getDefaultQueryFactorType());
                boolean hasNonDefaultFactors = ! subject.getNonDefaultFactors(agf.getAssayGroupId()).isEmpty();
                assertEquals(isNonDefaultType, hasNonDefaultFactors);

            }

        }

        for(FactorGroup g: subject.getFactorGroupsInOrder()) {
            assertFalse(g.isEmpty());
            assertTrue(g.overlapsWith(allFactors));
        }
    }
}
