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

package uk.ac.ebi.atlas.commands;

import com.google.common.base.Function;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Ordering;
import uk.ac.ebi.atlas.commons.streams.ObjectInputStream;
import uk.ac.ebi.atlas.model.Experiment;
import uk.ac.ebi.atlas.model.baseline.Factor;
import uk.ac.ebi.atlas.model.baseline.GeneProfile;
import uk.ac.ebi.atlas.model.baseline.GeneProfileComparator;
import uk.ac.ebi.atlas.model.baseline.GeneProfilesList;
import uk.ac.ebi.atlas.model.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.model.differential.DifferentialExpression;
import uk.ac.ebi.atlas.model.differential.DifferentialProfile;
import uk.ac.ebi.atlas.model.differential.DifferentialProfilesList;

import javax.inject.Named;
import java.util.Collections;
import java.util.Comparator;
import java.util.Queue;
import java.util.Set;

@Named
public class RankDifferentialProfilesCommand{

    public DifferentialProfilesList execute(DifferentialExperiment experiment) throws GeneNotFoundException{

        DifferentialProfilesList differentialProfilesList= new DifferentialProfilesList();
        /*
        Comparator<GeneProfile> geneProfileComparator = buildGeneProfileComparator(requestContext.isSpecific()
                , requestContext.getSelectedQueryFactors(), requestContext.getAllQueryFactors(), requestContext.getCutoff());

        Queue<GeneProfile> rankingQueue = buildRankingQueue(geneProfileComparator, requestContext.getHeatmapMatrixSize());

        GeneProfile geneProfile;

        int geneCount = 0;

        while ((geneProfile = inputStream.readNext()) != null) {
            rankingQueue.add(geneProfile);
            geneCount++;
        }

        GeneProfilesList list = new GeneProfilesList(rankingQueue);

        Collections.sort(list, geneProfileComparator);

        list.setTotalResultCount(geneCount);
        */
        differentialProfilesList.add(new DifferentialProfile("X12").addExpression(new DifferentialExpression(1.1,2.2, experiment.getContrasts().first())));
        differentialProfilesList.add(new DifferentialProfile("Y21").addExpression(new DifferentialExpression(0.1,0.2, experiment.getContrasts().first())));

        return differentialProfilesList;

    }

    protected GeneProfilesList returnEmpty() throws GeneNotFoundException {
        throw new GeneNotFoundException("No genes found matching query: '");
    }

    protected Ordering<GeneProfile> buildGeneProfileComparator(boolean isSpecific, Set<Factor> selectedQueryFactors,
                                                               Set<Factor> allFactors, double cutoff) {
        return Ordering.from(new GeneProfileComparator(isSpecific, selectedQueryFactors, allFactors, cutoff)).reverse();
    }

    protected Queue<GeneProfile> buildRankingQueue(Comparator<GeneProfile> geneProfileComparator, int heatmapMatrixSize) {
        return MinMaxPriorityQueue.orderedBy(geneProfileComparator).maximumSize(heatmapMatrixSize).create();
    }


}
