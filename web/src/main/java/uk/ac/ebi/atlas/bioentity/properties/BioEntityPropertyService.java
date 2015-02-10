/*
 * Copyright 2008-2013 Microarray Informatics Team, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the Gene Expression Atlas project, including source code,
 * downloads and documentation, please see:
 *
 * http://gxa.github.com/gxa
 */

package uk.ac.ebi.atlas.bioentity.properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.SortedSetMultimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import uk.ac.ebi.atlas.bioentity.go.GoPoTerm;
import uk.ac.ebi.atlas.bioentity.go.GoTermTrader;
import uk.ac.ebi.atlas.dao.ArrayDesignDao;
import uk.ac.ebi.atlas.utils.UniProtClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

@Named("bioEntityPropertyService")
@Scope("request")
public class BioEntityPropertyService {

    public static final String PROPERTY_TYPE_DESCRIPTION = "description";

    private BioEntityPropertyDao bioEntityPropertyDao;
    private UniProtClient uniProtClient;
    private ArrayDesignDao arrayDesignDao;
    private final BioEntityPropertyLinkBuilder linkBuilder;

    private SortedSetMultimap<String, String> propertyValuesByType;

    private Set<GoPoTerm> goPoTerms;

    private String species;

    private SortedSet<String> entityNames;

    private String identifier;


    @Inject
    public BioEntityPropertyService(BioEntityPropertyDao bioEntityPropertyDao, UniProtClient uniProtClient, BioEntityPropertyLinkBuilder linkBuilder, ArrayDesignDao arrayDesignDao) {
        this.bioEntityPropertyDao = bioEntityPropertyDao;
        this.uniProtClient = uniProtClient;
        this.arrayDesignDao = arrayDesignDao;
        this.linkBuilder = linkBuilder;
    }

    public void init(String species, SortedSetMultimap<String, String> propertyValuesByType, SortedSet<String> entityNames, String identifier) {
        ImmutableSet.Builder<GoPoTerm> builder = new ImmutableSet.Builder<>();
        init(species, propertyValuesByType, builder.build(), entityNames, identifier);
    }

    public void init(String species, SortedSetMultimap<String, String> propertyValuesByType, Set<GoPoTerm> goPoTerms, SortedSet<String> entityNames, String identifier) {
        this.species = species;
        this.propertyValuesByType = propertyValuesByType;
        this.entityNames = entityNames;
        this.identifier = identifier;
        this.goPoTerms = goPoTerms;

        // this is to add mirbase sequence for ENSEMBL mirnas
        if (propertyValuesByType.containsKey("mirbase_id") && !propertyValuesByType.containsKey("mirbase_sequence")) {
            addMirBaseSequence();
        }
    }

    public String getSpecies() {
        return species;
    }

    public List<PropertyLink> fetchPropertyLinks(String propertyType) {
        if ("reactome".equals(propertyType) && !propertyValuesByType.containsKey(propertyType)) {
            addReactomePropertyValues();
        } else if ("design_element".equals(propertyType) && !propertyValuesByType.containsKey(propertyType)) {
            addDesignElements();
        }

        List<PropertyLink> propertyLinks = Lists.newArrayList();
        for (String propertyValue : propertyValuesByType.get(propertyType)) {
            Optional<PropertyLink> link = linkBuilder.createLink(identifier, propertyType, propertyValue, species);
            if (link.isPresent()) {
                propertyLinks.add(link.get());
            }
        }
        return propertyLinks;
    }

    private void addMirBaseSequence() {
        String mirbase_id = propertyValuesByType.get("mirbase_id").first();
        Set<String> mirbase_sequence = bioEntityPropertyDao.fetchPropertyValuesForGeneId(mirbase_id, "mirbase_sequence");
        propertyValuesByType.putAll("mirbase_sequence", mirbase_sequence);
    }

    private void addDesignElements() {
        List<String> designElements = arrayDesignDao.getDesignElements(identifier);
        if (!designElements.isEmpty()) {
            propertyValuesByType.putAll("design_element", designElements);
        }
    }

    public String getBioEntityDescription() {
        String description = getFirstValueOfProperty(PROPERTY_TYPE_DESCRIPTION);
        return StringUtils.substringBefore(description, "[");
    }

    public SortedSet<String> getEntityNames() {
        return entityNames;
    }

    String getFirstValueOfProperty(String propertyType) {
        Collection<String> properties = propertyValuesByType.get(propertyType);
        return CollectionUtils.isNotEmpty(properties) ? properties.iterator().next() : "";
    }

    void addReactomePropertyValues() {
        Collection<String> uniprotIds = propertyValuesByType.get("uniprot");
        if (CollectionUtils.isNotEmpty(uniprotIds)) {
            for (String uniprotId : uniprotIds) {
                Collection<String> reactomeIds = uniProtClient.fetchReactomeIds(uniprotId);
                propertyValuesByType.putAll("reactome", reactomeIds);
            }
        }
    }
//
//    private void mapGoAccessionsByDepth() {
//        ImmutableMultimap.Builder<Integer, String> builder = new ImmutableMultimap.Builder<>();
//
//        for (String goAccession : propertyValuesByType.get("go")) {
//            builder.put(goTermTrader.getDepth(goAccession), goAccession);
//        }
//
//        goAccessionsByDepth = builder.build();
//    }

}

