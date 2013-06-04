package uk.ac.ebi.atlas.web.controllers.page;

import java.util.List;
import java.util.SortedSet;

public interface BioEntityPropertiesService {
    String getSpecies();

    //used in bioEntity.jsp
    List<PropertyLink> getPropertyLinks(String propertyType);

    String getBioEntityDescription();

    SortedSet<String> getEntityNames();
}
