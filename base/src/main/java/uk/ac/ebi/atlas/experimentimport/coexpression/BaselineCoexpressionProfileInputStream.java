package uk.ac.ebi.atlas.experimentimport.coexpression;

import uk.ac.ebi.atlas.commons.streams.ObjectInputStream;
import au.com.bytecode.opencsv.CSVReader;
import org.apache.solr.util.BoundedTreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkArgument;

public class BaselineCoexpressionProfileInputStream implements ObjectInputStream<BaselineCoexpressionProfile> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaselineCoexpressionProfileInputStream.class);

    private final int maxCoexpressionProfileSize;

    private static final int GENE_ID_COLUMN_INDEX = 0;

    private final CSVReader csvReader;
    private final String fileName;
    private final String[] geneIDsHeader;
    private final Queue<BaselineCoexpressionProfile> queue = new LinkedList<>();
    private int lineNumber = 0;


    public BaselineCoexpressionProfileInputStream(CSVReader csvReader, String fileName) {
        this(csvReader, fileName, 100);
    }

    BaselineCoexpressionProfileInputStream(CSVReader csvReader, String fileName, int maxCoexpressionProfileSize) {
        this.maxCoexpressionProfileSize = maxCoexpressionProfileSize;
        this.fileName = fileName;
        this.csvReader = csvReader;
        this.geneIDsHeader = readCsvLine();
        checkArgument(geneIDsHeader != null && geneIDsHeader.length > 1,
                "Could not read in the first line- possibly a malformed or empty file");
    }


    @Override
    public void close() throws IOException {
        csvReader.close();
    }


    private String[] readCsvLine() {
        lineNumber++;
        try {
            return csvReader.readNext();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(String.format("%s exception thrown while reading line %s", fileName, lineNumber), e);
        }
    }

    @Override
    public BaselineCoexpressionProfile readNext() {
        if (queue.isEmpty()) {
            BaselineCoexpressionProfile baselineCoexpressionProfile = readNextNonZeroLine();

            if (baselineCoexpressionProfile == null) {
                //EOF
                return null;
            }

            queue.add(baselineCoexpressionProfile);
        }

        return queue.remove();
    }

    private BaselineCoexpressionProfile readNextNonZeroLine() {

        String[] line = readCsvLine();
        if (line == null) {
            // EOF
            return null;
        }

        return new BaselineCoexpressionProfile(line[GENE_ID_COLUMN_INDEX], readCoexpressionProfileValues(line, lineNumber-1));
    }

    private Iterable<BaselineCoexpression> readCoexpressionProfileValues(String[] line, int
            diagonalElementPosition) {
        checkArgument(line.length == geneIDsHeader.length,
                "Line length differs from the header length, but the file is supposed to be a matrix!");

        TreeSet<BaselineCoexpression> coexpressionProfile = new BoundedTreeSet<>(maxCoexpressionProfileSize);
        for (int i = 0; i < line.length; i++) {
            if (i != GENE_ID_COLUMN_INDEX && i != diagonalElementPosition) {
                String ceGeneID = geneIDsHeader[i];
                coexpressionProfile.add(
                        BaselineCoexpression.create(Double.parseDouble(line[i]), ceGeneID));
            }
        }
        return coexpressionProfile;
    }
}