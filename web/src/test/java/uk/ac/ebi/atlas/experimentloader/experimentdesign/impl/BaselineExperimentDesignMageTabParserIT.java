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

package uk.ac.ebi.atlas.experimentloader.experimentdesign.impl;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.commons.magetab.MageTabLimpopoUtils;
import uk.ac.ebi.atlas.model.ExperimentDesign;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:solrContextIT.xml", "classpath:oracleContext.xml"})
public class BaselineExperimentDesignMageTabParserIT {

    private static final String EXPERIMENT_ACCESSION_E_MTAB_513 = "E-MTAB-513";

    private static final String EXPERIMENT_ACCESSION_E_GEOD_26284 = "E-GEOD-26284";

    @Inject
    private MageTabLimpopoUtils mageTabLimpopoUtils;

    @Inject
    private ValueAndUnitJoiner valueAndUnitJoiner;

    private RnaSeqExperimentDesignMageTabParser subject;

    @Before
    public void setUp() throws Exception {
        subject = new RnaSeqExperimentDesignMageTabParser();
        subject.setMageTabLimpopoUtils(mageTabLimpopoUtils);
        subject.setValueAndUnitJoiner(valueAndUnitJoiner);
    }

    @Test
    public void testExtractCharacteristics513() throws Exception {
        ExperimentDesign experimentDesign = subject.parse(EXPERIMENT_ACCESSION_E_MTAB_513).getExperimentDesign();
        assertThat(experimentDesign.getSampleHeaders(), containsInAnyOrder("sex", "age", "organism part", "Organism", "ethnic group"));
    }

    @Test
    public void testExtractCharacteristics26284() throws Exception {
        ExperimentDesign experimentDesign = subject.parse(EXPERIMENT_ACCESSION_E_GEOD_26284).getExperimentDesign();
        assertThat(experimentDesign.getSampleHeaders(), containsInAnyOrder("sex", "biosource provider", "cell line", "cellular component", "organism part", "karyotype", "disease state", "cell type", "Organism"));
    }

    @Test
    public void testGetSpeciesForAssays() throws IOException {
        ExperimentDesign experimentDesign = subject.parse(EXPERIMENT_ACCESSION_E_MTAB_513).getExperimentDesign();
        Set<String> species = experimentDesign.getSpeciesForAssays(Sets.newHashSet("ERR030886", "ERR030883"));
        assertThat(species, containsInAnyOrder("Homo sapiens"));

    }

    @Test
    public void testGetSpeciesForAssaysOnMultispeciesExperiment() throws IOException {
        ExperimentDesign experimentDesign = subject.parse("E-GEOD-30352").getExperimentDesign();
        Set<String> species = experimentDesign.getSpeciesForAssays(Sets.newHashSet("SRR306848", "SRR306747"));
        assertThat(species, containsInAnyOrder("Homo sapiens", "Monodelphis domestica"));

    }

}