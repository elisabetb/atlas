package uk.ac.ebi.atlas.geneannotation;


import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ConcurrentMap;

@Named("geneNamesProvider")
public class GeneNamesProvider {

    private ConcurrentMap<String, String> geneNames;

    @Inject
    public GeneNamesProvider(BDBEnvironmentHandler bdbEnvironmentHandler) {
        this.geneNames = bdbEnvironmentHandler.geneNames();
    }

    public String getGeneName(String ensGeneId) {
        String value = geneNames.get(ensGeneId);
        return value == null ? ensGeneId : value;
    }

//    @Resource(name = "geneNames")
//    public void setGeneNames(ConcurrentMap<String, String> geneNames) {
//        this.geneNames = geneNames;
//    }
}