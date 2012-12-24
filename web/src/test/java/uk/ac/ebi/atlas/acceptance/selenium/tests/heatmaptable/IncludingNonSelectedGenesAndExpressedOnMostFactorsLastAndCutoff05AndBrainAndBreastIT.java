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

package uk.ac.ebi.atlas.acceptance.selenium.tests.heatmaptable;

import org.junit.Test;
import uk.ac.ebi.atlas.acceptance.selenium.pages.HeatmapTablePage;
import uk.ac.ebi.atlas.acceptance.selenium.utils.SeleniumFixture;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IncludingNonSelectedGenesAndExpressedOnMostFactorsLastAndCutoff05AndBrainAndBreastIT extends SeleniumFixture {

    private static final String HTTP_PARAMETERS = "cutoff=0.5"
            +"&organismParts=brain&organismParts=breast"
            +"&includeGenesExpressedOnNonSelectedFactorValues=true"
            +"&rankGenesExpressedOnMostFactorsLast=true";
    private static final String HIGHER_RANKING_GENE = "NAIP";
    private static final String LOWER_RANKING_GENE = "RANBP17";

    protected HeatmapTablePage subject;

    public void getStartingPage() {
        subject = new HeatmapTablePage(driver, HTTP_PARAMETERS);
        subject.get();
    }

    @Test
    public void averageFpkmAndRankingShouldBePenalizedByExpressionOnNonSelectedFactors() {

        //given
        subject.clickDisplayLevelsButton();

        double higherRankingGeneAverageFpkmOnSelectedFactors = subject.getAverageFpkm(29, "brain", "breast");
        double lowerRankingGeneAverageFpkmOnSelectedFactors = subject.getAverageFpkm(30, "brain", "breast" );

        //then
        assertThat(higherRankingGeneAverageFpkmOnSelectedFactors, is(1.5));
        //and
        assertThat(lowerRankingGeneAverageFpkmOnSelectedFactors, is(2D));

        //and even though average fpkm is greater for gene at row 30 than gen at row 29
        assertThat(lowerRankingGeneAverageFpkmOnSelectedFactors, is(greaterThan(higherRankingGeneAverageFpkmOnSelectedFactors)));
        //and even though max fpkm is greater for gene at row 30 than gene at row 29
        assertThat(subject.getMaxFpkm(30), is(greaterThan(subject.getMaxFpkm(29))));

        //gene at row 30 follows gene at row 29 because is penalized by fpkm values of non selected factors!
        assertThat(subject.getGeneThatRanksAt(29), is(HIGHER_RANKING_GENE));
        assertThat(subject.getGeneThatRanksAt(30), is(LOWER_RANKING_GENE));

    }

}
