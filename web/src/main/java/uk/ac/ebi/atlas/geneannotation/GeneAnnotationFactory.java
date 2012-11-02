package uk.ac.ebi.atlas.geneannotation;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Named
public class GeneAnnotationFactory {

    private BDBEnvironmentHandler environmentHandler;

    @Inject
    public GeneAnnotationFactory(BDBEnvironmentHandler environmentHandler) {
        this.environmentHandler = environmentHandler;
    }

    public ConcurrentMap<String, String> getGeneNames() {
        if (environmentHandler.getAnnotationEnvironment().isInitialised())
            return environmentHandler.getAnnotationEnvironment().geneNames();
        else {
            return new ConcurrentHashMap<String, String>();
        }


    }
}
