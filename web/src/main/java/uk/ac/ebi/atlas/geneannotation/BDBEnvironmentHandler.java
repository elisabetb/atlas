package uk.ac.ebi.atlas.geneannotation;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Named
public class BDBEnvironmentHandler implements DisposableBean {

    AnnotationEnvironment annotationEnvironment;

    @Inject
    public BDBEnvironmentHandler(@Value("#{configuration['genename.bdb.location']}") String
                                                               environmentLocation) {
        this.annotationEnvironment = new AnnotationEnvironment(environmentLocation);
        annotationEnvironment.setup();
    }

    public AnnotationEnvironment getAnnotationEnvironment() {

        return annotationEnvironment;

    }

    public ConcurrentMap<String, String> geneNames() {
        if (getAnnotationEnvironment().isInitialised())
            return annotationEnvironment.geneNames();
        else {
            return new ConcurrentHashMap<String, String>();
        }
    }

    @Override
    public void destroy() throws Exception {
        annotationEnvironment.close();

    }
}
