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

package uk.ac.ebi.atlas.configuration;

import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.atlas.expdesign.ExpDesignTsvWriter;
import uk.ac.ebi.atlas.expdesign.ExperimentDesignWriter;
import uk.ac.ebi.atlas.expdesign.ExperimentDesignWriterFactory;
import uk.ac.ebi.atlas.geneannotation.ArrayDesignDao;
import uk.ac.ebi.atlas.geneannotation.arraydesign.ArrayDesignType;
import uk.ac.ebi.atlas.geneannotation.arraydesign.DesignElementMappingLoader;
import uk.ac.ebi.atlas.model.ConfigurationTrader;
import uk.ac.ebi.atlas.model.ExperimentType;
import uk.ac.ebi.atlas.model.differential.microarray.MicroarrayExperimentConfiguration;
import uk.ac.ebi.atlas.transcript.TranscriptProfilesLoader;

import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentManagerTest {

    private static final String EXPERIMENT_ACCESSION = "EXPERIMENT_ACCESSION";
    private static final String TEST_EXCEPTION = "TEST_EXCEPTION";
    private static final String ARRAY_DESIGN = "ARRAY_DESIGN";

    @Mock
    private ExperimentType experimentTypeMock;

    @Mock
    private ExperimentDesignWriter experimentDesignWriterMock;

    @Mock
    private ExpDesignTsvWriter experimentDesignTsvWriterMock;

    @Mock
    private CSVWriter csvWriterMock;

    @Mock
    private TranscriptProfilesLoader transcriptProfileLoaderMock;

    @Mock
    private ArrayDesignDao arrayDesignDaoMock;

    @Mock
    private ConfigurationTrader configurationTraderMock;

    @Mock
    private DesignElementMappingLoader designElementLoaderMock;

    @Mock
    private MicroarrayExperimentConfiguration microarrayExperimentConfigurationMock;

    private ExperimentManager subject;

    @Mock
    private ExperimentDesignWriterFactory experimentDesignWriterFactoryMock;

    @Before
    public void setUp() throws Exception {

        when(experimentDesignTsvWriterMock.forExperimentAccession(EXPERIMENT_ACCESSION)).thenReturn(csvWriterMock);
        when(experimentDesignTsvWriterMock.getFileAbsolutePath()).thenReturn("UNIT_TEST");

        when(transcriptProfileLoaderMock.load(EXPERIMENT_ACCESSION)).thenReturn(0);
        when(configurationTraderMock.getMicroarrayExperimentConfiguration(EXPERIMENT_ACCESSION)).thenReturn(microarrayExperimentConfigurationMock);
        when(microarrayExperimentConfigurationMock.getArrayDesignNames()).thenReturn(Sets.newTreeSet(Sets.newHashSet(ARRAY_DESIGN)));

        given(experimentTypeMock.createExperimentDesignWriter(experimentDesignWriterFactoryMock)).willReturn(experimentDesignWriterMock);

        subject = new ExperimentManager(experimentDesignTsvWriterMock, transcriptProfileLoaderMock,
                arrayDesignDaoMock, configurationTraderMock, designElementLoaderMock, experimentDesignWriterFactoryMock);
    }

    @Test
    public void generateExperimentDesignShouldUseTheCsvWriter() throws Exception {

        subject.generateExpDesign(EXPERIMENT_ACCESSION, experimentTypeMock);
        verify(experimentDesignTsvWriterMock).forExperimentAccession(EXPERIMENT_ACCESSION);
        verify(experimentTypeMock).createExperimentDesignWriter(experimentDesignWriterFactoryMock);
        verify(experimentDesignWriterMock).write(EXPERIMENT_ACCESSION, csvWriterMock);
        verify(csvWriterMock).flush();
    }

    @Test(expected = IOException.class)
    public void shouldThrowIllegalStateExceptionWhenClosingCsvWriterFails() throws Exception {

        willThrow(new IOException()).given(csvWriterMock).close();

        subject.generateExpDesign(EXPERIMENT_ACCESSION, experimentTypeMock);
    }

    @Test(expected = IOException.class)
    public void shouldThrowIllegalStateExceptionWhenWritingExperimentDesignFails() throws Exception {

        willThrow(new IOException()).given(experimentDesignWriterMock).write(EXPERIMENT_ACCESSION, csvWriterMock);

        subject.generateExpDesign(EXPERIMENT_ACCESSION, experimentTypeMock);
    }

    @Test
    public void testLoadTranscripts() throws Exception {
        subject.loadTranscripts(EXPERIMENT_ACCESSION);
        verify(transcriptProfileLoaderMock).load(EXPERIMENT_ACCESSION);
    }

    @Test(expected = IllegalStateException.class)
    public void testTranscriptLoaderException() throws Exception {
        Mockito.doThrow(new IOException(TEST_EXCEPTION)).when(transcriptProfileLoaderMock).load(EXPERIMENT_ACCESSION);
        subject.loadTranscripts(EXPERIMENT_ACCESSION);
    }

    @Test
    public void testLoadArrayDesignPresent() throws Exception {
        when(arrayDesignDaoMock.isArrayDesignPresent(ARRAY_DESIGN)).thenReturn(true);
        subject.loadArrayDesign(EXPERIMENT_ACCESSION, ArrayDesignType.MICRO_ARRAY);
        verify(designElementLoaderMock, times(0)).loadMappings(ARRAY_DESIGN, ArrayDesignType.MICRO_ARRAY);
    }

    @Test
    public void testLoadArrayDesignNotPresent() throws Exception {
        when(arrayDesignDaoMock.isArrayDesignPresent(ARRAY_DESIGN)).thenReturn(false);
        subject.loadArrayDesign(EXPERIMENT_ACCESSION, ArrayDesignType.MICRO_ARRAY);
        verify(designElementLoaderMock, times(1)).loadMappings(ARRAY_DESIGN, ArrayDesignType.MICRO_ARRAY);
    }

    @Test
    public void testLoadArrayDesignMicroRNAPresent() throws Exception {
        when(arrayDesignDaoMock.isArrayDesignPresent(ARRAY_DESIGN)).thenReturn(true);
        subject.loadArrayDesign(EXPERIMENT_ACCESSION, ArrayDesignType.MICRO_RNA);
        verify(designElementLoaderMock, times(0)).loadMappings(ARRAY_DESIGN, ArrayDesignType.MICRO_RNA);
    }

    @Test
    public void testLoadArrayDesignMicroRNANotPresent() throws Exception {
        when(arrayDesignDaoMock.isArrayDesignPresent(ARRAY_DESIGN)).thenReturn(false);
        subject.loadArrayDesign(EXPERIMENT_ACCESSION, ArrayDesignType.MICRO_RNA);
        verify(designElementLoaderMock, times(1)).loadMappings(ARRAY_DESIGN, ArrayDesignType.MICRO_RNA);
    }
}