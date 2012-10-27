package uk.ac.ebi.atlas.streams;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ebi.atlas.commons.ObjectInputStream;
import uk.ac.ebi.atlas.geneannotation.GeneNamesProvider;
import uk.ac.ebi.atlas.model.GeneProfile;
import uk.ac.ebi.atlas.model.GeneProfileBuilderConcreteFactory;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeneProfileInputStreamBuilderTest {


    @Mock
    private InputStream inputStreamMock;

    @Mock
    private ExpressionsBuffer.Builder expressionsBufferBuilderMock;

    @Mock
    private ExpressionsBuffer expressionsBufferMock;

    @Mock
    private GeneProfilesInputStream geneProfilesInputStreamMock;

    private String[] headersMock = new String[]{"", "header_value_1", "header_value_2"};

    private GeneProfilesInputStream.Builder subject;

    @Before
    public void initMocks() throws IOException {
        when(geneProfilesInputStreamMock.readCsvLine()).thenReturn(headersMock);
        when(expressionsBufferBuilderMock.forExperiment(anyString())).thenReturn(expressionsBufferBuilderMock);
        when(expressionsBufferBuilderMock.withHeaders(headersMock)).thenReturn(expressionsBufferBuilderMock);
        when(expressionsBufferBuilderMock.create()).thenReturn(expressionsBufferMock);
    }

    @Before
    public void initSubject() {
        subject = new GeneProfilesInputStream
                        .Builder(geneProfilesInputStreamMock, expressionsBufferBuilderMock)
                        .forTsvFileInputStream(inputStreamMock);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenExperimentAccessionHasntBeenSet() {
        //when
        subject.create();
    }

    @Test
    public void createShouldSucceedAfterExperimentAccessionHasBeenSet() {
        //when
        subject.withExperimentAccession("AN_EXPERIMENT_ACCESSION");

        ObjectInputStream<GeneProfile> geneProfilesInputStream = subject.create();
        //then
        assertThat(geneProfilesInputStream, is(not(nullValue())));
        //and
        verify(geneProfilesInputStreamMock).setExpressionBuffer(expressionsBufferMock);

    }

    @Test
    public void shouldReadTheHeaderLineFromTheDataFileWhenWeSetTheExperimentAccession() throws IOException {
        //when
        subject.withExperimentAccession("AN_EXPERIMENT_ACCESSION");
        //then
        verify(geneProfilesInputStreamMock).readCsvLine();
    }

    @Test
    public void shouldInitializeTheExpressionBufferWhenWeSetTheExperimentAccession() throws IOException {
        //when
        subject.withExperimentAccession("AN_EXPERIMENT_ACCESSION");

        //then
        verify(expressionsBufferBuilderMock).forExperiment("AN_EXPERIMENT_ACCESSION");
        verify(expressionsBufferBuilderMock).withHeaders(headersMock);
        verify(expressionsBufferBuilderMock).create();
    }


}
