package uk.ac.ebi.atlas.geneannotation;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.je.*;
import org.apache.log4j.Logger;

import java.io.File;

public class AnnotationEnvironment {
    private static final Logger logger = Logger.getLogger(AnnotationEnvironment.class);

    private static final String GENES_DB = "genes.db";
    private Environment environment;

    private Database geneNameDatabase;

    private String environmentLocation;

    private boolean isInitialised = false;

    public AnnotationEnvironment(String environmentLocation) {
        this.environmentLocation = environmentLocation;
    }

    public void setup() {
        setupEnvironment();
        setupGeneNameDatabase();

    }

    private void setupEnvironment() {
        EnvironmentConfig envConfig = new EnvironmentConfig();

        envConfig.setAllowCreate(true);
        envConfig.setTransactional(true);
        File envHome = new File(environmentLocation);
        if (!envHome.exists()) {
            if (!envHome.mkdirs()) {
                isInitialised = false;
                logger.error("Cannot create directories for BDB environment on " + environmentLocation);
            }
        }
        try {
            environment = new Environment(envHome, envConfig);
            isInitialised = true;
        } catch (DatabaseException | IllegalArgumentException e) {
            logger.error("Cannot open BDB environment on " + environmentLocation, e);
            isInitialised = false;
        }
    }

    public void setupGeneNameDatabase() {

        if (!isInitialised) return;

        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);

        try {
            geneNameDatabase = environment.openDatabase(null, GENES_DB, dbConfig);
        } catch (DatabaseNotFoundException | DatabaseExistsException | IllegalArgumentException | IllegalStateException e) {
            logger.error("Cannot open BDB environment on " + environmentLocation, e);
            isInitialised = false;
        }
    }

    public boolean isInitialised() {
        return isInitialised;
    }

    public StoredMap<String, String> geneNames() {
        TupleBinding<String> keyBinding = TupleBinding.getPrimitiveBinding(String.class);
        TupleBinding<String> dataBinding = TupleBinding.getPrimitiveBinding(String.class);

        StoredMap<String, String> storedMap = new StoredMap<String, String>
                (geneNameDatabase, keyBinding, dataBinding, true);

        return storedMap;
    }

    public TransactionRunner getTransactionRunner() {
        return new TransactionRunner(environment);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Database getGeneNameDatabase() {
        return geneNameDatabase;
    }

    public void close() {
        if (isInitialised) {
            geneNameDatabase.close();
            environment.close();
        }
    }

    public static void main(String[] args) {
        AnnotationEnvironment environment1 = new AnnotationEnvironment("/Users/nsklyar/Data/bdb/gene");
        environment1.setup();
        int size = environment1.geneNames().size();
        System.out.println("size = " + size);
        while (true) {

        }
    }
}
