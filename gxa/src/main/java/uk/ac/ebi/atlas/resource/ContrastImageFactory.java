package uk.ac.ebi.atlas.resource;

import org.springframework.beans.factory.annotation.Value;
import uk.ac.ebi.atlas.model.resource.ContrastImage;
import uk.ac.ebi.atlas.model.resource.ExternalImage;
import uk.ac.ebi.atlas.model.resource.ResourceType;

import javax.inject.Named;
import java.util.Optional;

@Named
public class ContrastImageFactory{

    @Value("#{configuration['experiment.gsea-plot.path.template']}")
    String gseaPathTemplate;

    @Value("#{configuration['experiment.rnaseq.ma-plot.path.template']}")
    String rnaSeqPathTemplate;

    @Value("#{configuration['experiment.microarray.ma-plot.path.template']}")
    String microarrayPathTemplate;


    ExternalImage getContrastImage(ResourceType resourceType, String experimentAccession,
                                   Optional<String> arrayDesign, String contrastId ){
        String pathTemplate = "";
        switch (resourceType) {
            case PLOT_GSEA_INTERPRO:
                pathTemplate = gseaPathTemplate.replace("{2}", "interpro");
                break;
            case PLOT_GSEA_GO:
                pathTemplate = gseaPathTemplate.replace("{2}", "go");
                break;
            case PLOT_GSEA_REACTOME:
                pathTemplate = gseaPathTemplate.replace("{2}", "reactome");
                break;
            case PLOT_MA:
                pathTemplate = arrayDesign.isPresent()? microarrayPathTemplate : rnaSeqPathTemplate;
                break;
        }

        if(arrayDesign.isPresent() && resourceType.equals(ResourceType.PLOT_MA)){
            return new ContrastImage(
                    resourceType, pathTemplate, "external-resources/{0}/{1}/{2}/" + resourceType.fileName(),
                    experimentAccession, arrayDesign.get(), contrastId);
        } else {
            return new ContrastImage(
                    resourceType, pathTemplate, "external-resources/{0}/{1}/" + resourceType.fileName(),
                    experimentAccession, contrastId);
        }
    }

    ExternalImage getContrastImage(ResourceType resourceType, String experimentAccession, String contrastId ) {
        return getContrastImage(resourceType, experimentAccession, Optional.empty(), contrastId);
    }
}
