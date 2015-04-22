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

package uk.ac.ebi.atlas.experimentpage.baseline.geod26284;

import org.junit.BeforeClass;
import uk.ac.ebi.atlas.acceptance.selenium.fixture.SingleDriverSeleniumFixture;
import uk.ac.ebi.atlas.acceptance.selenium.pages.Geod26284HeatmapTablePage;

public class WholeCellAndA549AndCutoffButWithoutAnyGeneQuerySIT extends Geod26284HeatmapTableTests {

    @BeforeClass
    public static void getStartingPage() {
        subject = new Geod26284HeatmapTablePage(SingleDriverSeleniumFixture.create(),
                "geneQuery=&serializedFilterFactors=CELLULAR_COMPONENT%3Awhole+cell%2CCELL_LINE%3AA549&queryFactorType=RNA&displayLevels=true");
        subject.get();
    }

    @Override
    protected String getQueryFactorLabel() {
        return "RNA";
    }

    @Override
    protected String[] getTop9Genes() {
        return new String[]{"ZBTB32", "NME2", "ARHGAP31", "CLDN11", "GPR124", "ABCC8", "PHF7", "PGLYRP1", "CCDC88C"};
    }

    @Override
    protected String[] getHeatmapHeader() {
        return new String[]{"long non-polyA…", "long polyA RNA"};
    }

    @Override
    protected String[] getFirstGeneProfile() {
        return new String[]{"2", ""};
    }

    @Override
    protected String[] getNinthGeneProfile() {
        return new String[]{"0.9", ""};
    }

    @Override
    protected String getGeneCount() {
        return "369";
    }

}