/*
 * Copyright 2008-2012 Microarray Informatics Team, EMBL-European Bioinformatics Institute
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

package uk.ac.ebi.atlas.web;

import com.google.common.collect.Sets;
import org.springframework.context.annotation.Scope;
import uk.ac.ebi.atlas.model.caches.ExperimentsCache;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

@Named("applicationProperties")
@Scope("singleton")
public class ApplicationProperties {

    private ExperimentsCache experimentsCache;

    private Properties configurationProperties;

    private SortedSet<String> organismParts;

    @Inject
    public ApplicationProperties(ExperimentsCache experimentsCache, @Named("configuration") Properties configurationProperties){
        this.experimentsCache = experimentsCache;
        this.configurationProperties = configurationProperties;
        String organismPartsCSV = this.configurationProperties.getProperty("organism.parts");
        this.organismParts = new TreeSet<String>(Sets.newHashSet(organismPartsCSV.split(",")));
    }

    public SortedSet<String> getAllOrganismParts(){
        return organismParts;
    }

    public String getAnatomogramFileName(String experimentAccession, boolean isMale){
        String specie = experimentsCache.getExperiment(experimentAccession).getSpecie();
        String key = "organism.anatomogram." + specie.toLowerCase();
        String fileName = configurationProperties.getProperty( key + (isMale ? ".male" : ".female"));
        return fileName != null ? fileName : configurationProperties.getProperty(key);

    }

}
