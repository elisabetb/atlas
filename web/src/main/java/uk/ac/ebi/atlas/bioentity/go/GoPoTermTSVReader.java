package uk.ac.ebi.atlas.bioentity.go;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableMap;

import java.io.Closeable;
import java.io.IOException;

public class GoPoTermTSVReader implements Closeable {

    private final CSVReader csvReader;
    private String termType;

    private ImmutableMap<String, GoPoTerm> accessionToGoPoTerm;

    public GoPoTermTSVReader(CSVReader csvReader, String termType) {
        this.csvReader = csvReader;
        this.termType = termType;
    }

    void readAll() throws IOException {

        ImmutableMap.Builder<String, GoPoTerm> builder = ImmutableMap.builder();
        String[] nextLine;
        while ((nextLine = csvReader.readNext()) != null) {
            String accession = nextLine[0];
            String term = nextLine[1].replace("_", " ");
            int depth = nextLine.length == 3 ? Integer.getInteger(nextLine[2]) : 0;

            if(accession.startsWith(termType)) {
                builder.put(accession, GoPoTerm.create(accession, term, depth));
            }
        }

        accessionToGoPoTerm = builder.build();
    }

    ImmutableMap<String, GoPoTerm> getAccessionToTermMap() {
        return accessionToGoPoTerm;
    }

    @Override
    public void close() throws IOException {
        csvReader.close();
    }
}
