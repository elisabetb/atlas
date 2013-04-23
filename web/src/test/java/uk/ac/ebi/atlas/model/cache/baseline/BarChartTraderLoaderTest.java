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

package uk.ac.ebi.atlas.model.cache.baseline;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.atlas.model.baseline.barcharts.BarChartTrader;
import uk.ac.ebi.atlas.model.baseline.barcharts.BitIndexBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BarChartTraderLoaderTest {

    public static final String EXPERIMENT_ACCESSION = "experimentAccession";

    @Mock
    private BitIndexBuilder bitIndexBuilderMock;

    @Mock
    private BarChartTrader barChartTraderMock;

    @Mock
    private BarChartTraderLoader subject;

    @Before
    public void setUp() throws Exception {
        when(subject.createBitIndexBuilder()).thenReturn(bitIndexBuilderMock);
        when(bitIndexBuilderMock.forExperiment(EXPERIMENT_ACCESSION)).thenReturn(bitIndexBuilderMock);
        when(bitIndexBuilderMock.create()).thenReturn(barChartTraderMock);
    }

    @Test
    public void testLoad() throws Exception {
        doCallRealMethod().when(subject).load(EXPERIMENT_ACCESSION);

        assertThat(subject.load(EXPERIMENT_ACCESSION), is(barChartTraderMock));
    }
}