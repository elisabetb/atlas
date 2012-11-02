package uk.ac.ebi.atlas.geneannotation;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.TransactionWorker;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import uk.ac.ebi.atlas.commons.ObjectInputStream;
import uk.ac.ebi.atlas.utils.biomart.BioMartGeneNameStream;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ConcurrentMap;

@Named("geneAnnotationLoader")
@Scope("prototype")
public class GeneAnnotationLoader {

    private static final Logger logger = Logger.getLogger(GeneAnnotationLoader.class);

//    private TransactionRunner transactionRunner;

    private BioMartGeneNameStream.Builder geneNameStreamBuilder;

//    private AnnotationEnvironment annotationEnvironment;
    private BDBEnvironmentHandler environmentHandler;

    @Inject
    public GeneAnnotationLoader(BDBEnvironmentHandler environmentHandler, BioMartGeneNameStream.Builder geneNameStreamBuilder) {
        this.environmentHandler = environmentHandler;
        this.geneNameStreamBuilder = geneNameStreamBuilder;
//        transactionRunner = annotationEnvironment.getTransactionRunner();
    }


    protected void loadAnnotations(ObjectInputStream<String[]> annotationsInputStream,
                                GeneAnnotationTransactionWorker transactionWorker) throws Exception {

        String[] line;

        while ((line = annotationsInputStream.readNext()) != null) {
            getAnnotationEnvironment().getTransactionRunner().run(transactionWorker.setLine(line));

        }
    }

    public void loadGeneNames() {

        GeneAnnotationTransactionWorker<String> transactionWorker = new GeneNameTransactionWorker
                (getAnnotationEnvironment().geneNames());

        try (ObjectInputStream<String[]> annotationsInputStream = geneNameStreamBuilder.create()) {
            loadAnnotations(annotationsInputStream, transactionWorker);
        } catch (Exception e) {
            throw new IllegalStateException("Error while writing gene name DB: " + e.getMessage());
        }
    }

    private AnnotationEnvironment getAnnotationEnvironment() {
        AnnotationEnvironment annotationEnvironment = environmentHandler.getAnnotationEnvironment();
        if (!annotationEnvironment.isInitialised()) {
            throw new IllegalStateException("Cannot access to BDB for gene loading");
        }

        return annotationEnvironment;
    }
    static class GeneNameTransactionWorker extends GeneAnnotationTransactionWorker<String> {

        protected GeneNameTransactionWorker(StoredMap<String, String> map) {
            super(map);
        }

        @Override
        protected String getValue() {
            return line[1];
        }

        @Override
        protected String getKey() {
            return line[0];
        }

    }

    static abstract class GeneAnnotationTransactionWorker<V> implements TransactionWorker {

        protected String[] line;

        private ConcurrentMap<String, V> map;

        protected GeneAnnotationTransactionWorker(ConcurrentMap<String, V> map) {
            this.map = map;
        }

        public GeneAnnotationTransactionWorker setLine(String[] line) {
            this.line = line;
            return this;
        }

        @Override
        public void doWork() throws Exception {

            try {
                if (!map.containsKey(getKey()))
                    map.put(getKey(), getValue());
                else
                    map.replace(getKey(), getValue());
            } catch (Exception e) {
                throw new IllegalStateException("Error while writing gene annotations DB: " + e.getMessage());
            }
        }

        protected abstract V getValue();

        protected abstract String getKey();
    }

}
