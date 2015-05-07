package uk.ac.ebi.atlas.experimentimport.analytics.baseline;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.base.Joiner;
import org.junit.Test;
import uk.ac.ebi.atlas.utils.CsvReaderFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ProteomicsBaselineAnalyticsInputStreamTest {
    private static CsvReaderFactory csvReaderFactory = new CsvReaderFactory();

    public static final String GENE_ID_1 = "ENSG00000003400";
    public static final String GENE_ID_2 = "ENSG00000004939";
    private static final String GENE_NAME_1 = "CASP10";
    private static final String GENE_NAME_2 = "SLC4A1";

    private static final String TSV_HEADER = Joiner.on("\t").join(new String[]{"Gene ID", "Gene Name", "g1.SpectralCount", "g2.SpectralCount", "g3.SpectralCount", "g4.SpectralCount", "g5.SpectralCount", "g1.WithInSampleAbundance", "g2.WithInSampleAbundance", "g3.WithInSampleAbundance", "g4.WithInSampleAbundance", "g5.WithInSampleAbundance"});
    private static final String TSV_LINE_1 = Joiner.on("\t").join(new String[]{GENE_ID_1, GENE_NAME_1, "0", "7", "8", "5", "0", "0", "5.01E+06", "1.33E-05", "7.90E-06", "0"});
    private static final String TSV_LINE_2 = Joiner.on("\t").join(new String[]{GENE_ID_2, GENE_NAME_2, "80.5", "0", "0", "4", "25", "0.000309075", "0", "0", "2.82E+06", "8.49E-05"});

    private static final String TSV_LINE_NO_EXPRESSION = Joiner.on("\t").join(new String[]{GENE_ID_1, GENE_NAME_1, "0", "0", "0", "0", "0", "0", "0", "0", "0", "0"});
    private static final String TSV_LINE_LOWDATA = Joiner.on("\t").join(new String[]{GENE_ID_1, GENE_NAME_1, "0", "0", "0", "5", "0", "0", "0", "NA", "7.90E-06", "0"});
    private static final String TSV_LINE_FAIL = Joiner.on("\t").join(new String[]{GENE_ID_1, GENE_NAME_1, "0", "0", "0", "0", "5", "0", "0", "NA", "0", "7.90E-06"});
    private static final String TSV_LINE_NA = Joiner.on("\t").join(new String[]{GENE_ID_1, GENE_NAME_1, "0", "5", "0", "0", "0", "0", "7.90E-06", "NA", "0", "0"});

    private static String TSV_CONTENTS = Joiner.on("\n").join(new String[]{TSV_HEADER, TSV_LINE_1, TSV_LINE_2});


    @Test
    public void readTwoTsvLines() throws IOException {
        Reader tsvSource = new StringReader(TSV_CONTENTS);
        CSVReader csvReader = csvReaderFactory.createTsvReader(tsvSource);
        ProteomicsBaselineAnalyticsInputStream subject = new ProteomicsBaselineAnalyticsInputStream(csvReader, "Test");

        BaselineAnalytics line1g2 = new BaselineAnalytics(GENE_ID_1, "g2", 5010000);
        BaselineAnalytics line1g3 = new BaselineAnalytics(GENE_ID_1, "g3", 0.0000133);
        BaselineAnalytics line1g4 = new BaselineAnalytics(GENE_ID_1, "g4", 0.0000079);

        BaselineAnalytics line2g1 = new BaselineAnalytics(GENE_ID_2, "g1", 0.000309075);
        BaselineAnalytics line2g4 = new BaselineAnalytics(GENE_ID_2, "g4", 2820000);
        BaselineAnalytics line2g5 = new BaselineAnalytics(GENE_ID_2, "g5", 0.0000849);

        assertThat(subject.readNext(), is(line1g2));
        assertThat(subject.readNext(), is(line1g3));
        assertThat(subject.readNext(), is(line1g4));

        assertThat(subject.readNext(), is(line2g1));
        assertThat(subject.readNext(), is(line2g4));
        assertThat(subject.readNext(), is(line2g5));
        assertThat(subject.readNext(), is(nullValue()));
    }

    @Test
    public void readTsvLineWithNoExpression() throws IOException {
        String tsvContents = Joiner.on("\n").join(new String[]{TSV_HEADER, TSV_LINE_NO_EXPRESSION});

        Reader tsvSource = new StringReader(tsvContents);
        CSVReader csvReader = csvReaderFactory.createTsvReader(tsvSource);
        ProteomicsBaselineAnalyticsInputStream subject = new ProteomicsBaselineAnalyticsInputStream(csvReader, "Test");

        assertThat(subject.readNext(), is(nullValue()));
    }

    @Test
    public void readTsvLineWithLowData() throws IOException {
        String tsvContents = Joiner.on("\n").join(new String[]{TSV_HEADER, TSV_LINE_LOWDATA});

        Reader tsvSource = new StringReader(tsvContents);
        CSVReader csvReader = csvReaderFactory.createTsvReader(tsvSource);
        ProteomicsBaselineAnalyticsInputStream subject = new ProteomicsBaselineAnalyticsInputStream(csvReader, "Test");

        BaselineAnalytics line1g2 = new BaselineAnalytics(GENE_ID_1, "g4", 0.0000079);
        assertThat(subject.readNext(), is(line1g2));
        assertThat(subject.readNext(), is(nullValue()));
    }

    @Test
    public void readTsvLineWithNA() throws IOException {
        String tsvContents = Joiner.on("\n").join(new String[]{TSV_HEADER, TSV_LINE_NA});

        Reader tsvSource = new StringReader(tsvContents);
        CSVReader csvReader = csvReaderFactory.createTsvReader(tsvSource);
        ProteomicsBaselineAnalyticsInputStream subject = new ProteomicsBaselineAnalyticsInputStream(csvReader, "Test");

        BaselineAnalytics line1g4 = new BaselineAnalytics(GENE_ID_1, "g2", 0.0000079);
        assertThat(subject.readNext(), is(line1g4));
        assertThat(subject.readNext(), is(nullValue()));

    }

    @Test
    public void readTsvLineWithFail() throws IOException {
        String tsvContents = Joiner.on("\n").join(new String[]{TSV_HEADER, TSV_LINE_FAIL});

        Reader tsvSource = new StringReader(tsvContents);
        CSVReader csvReader = csvReaderFactory.createTsvReader(tsvSource);
        ProteomicsBaselineAnalyticsInputStream subject = new ProteomicsBaselineAnalyticsInputStream(csvReader, "Test");

        BaselineAnalytics line1g2 = new BaselineAnalytics(GENE_ID_1, "g5", 0.0000079);
        assertThat(subject.readNext(), is(line1g2));
        assertThat(subject.readNext(), is(nullValue()));
    }

    @Test
    public void tryResourcesClosesUnderlyingReaderWhenFinished() throws IOException {
        Reader tsvSource = spy(new StringReader(TSV_CONTENTS));
        CSVReader csvReader = csvReaderFactory.createTsvReader(tsvSource);

        try (ProteomicsBaselineAnalyticsInputStream subject = new ProteomicsBaselineAnalyticsInputStream(csvReader, "Test")) {
            subject.readNext();
        }

        verify(tsvSource).close();
    }

    @Test
    public void tryResourcesAutoClosesUnderlyingReaderOnException() throws IOException {
        Reader tsvSource = spy(new StringReader(TSV_CONTENTS));
        CSVReader csvReader = csvReaderFactory.createTsvReader(tsvSource);

        try (ProteomicsBaselineAnalyticsInputStream subject = new ProteomicsBaselineAnalyticsInputStream(csvReader, "Test")) {
            subject.readNext();
            throw new RuntimeException("foobar");
        } catch (RuntimeException e) {
            // ignore
        }

        verify(tsvSource).close();
    }
}