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

package uk.ac.ebi.atlas.model.baseline.barcharts;

import au.com.bytecode.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import uk.ac.ebi.atlas.commons.streams.ObjectInputStream;
import uk.ac.ebi.atlas.model.Experiment;
import uk.ac.ebi.atlas.profiles.baseline.BaselineExpressionsQueueBuilder;
import uk.ac.ebi.atlas.profiles.baseline.ProteomicsBaselineExpressionsQueueBuilder;
import uk.ac.ebi.atlas.trader.ExperimentTrader;
import uk.ac.ebi.atlas.utils.CsvReaderFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;

@Named
@Scope("prototype")
public class BaselineExpressionsInputStreamFactory {

    @Value("#{configuration['experiment.magetab.path.template']}")
    private String baselineExperimentDataFileUrlTemplate;

    private BaselineExpressionsQueueBuilder baselineExpressionsQueueBuilder;
    private ProteomicsBaselineExpressionsQueueBuilder proteomicsBaselineExpressionsQueueBuilder;
    private CsvReaderFactory csvReaderFactory;
    private ExperimentTrader experimentTrader;

    @Inject
    public BaselineExpressionsInputStreamFactory(BaselineExpressionsQueueBuilder baselineExpressionsQueueBuilder,
                                                 ProteomicsBaselineExpressionsQueueBuilder proteomicsBaselineExpressionsQueueBuilder,
                                                 CsvReaderFactory csvReaderFactory,
                                                 ExperimentTrader experimentTrader) {
        this.baselineExpressionsQueueBuilder = baselineExpressionsQueueBuilder;
        this.proteomicsBaselineExpressionsQueueBuilder = proteomicsBaselineExpressionsQueueBuilder;
        this.csvReaderFactory = csvReaderFactory;
        this.experimentTrader = experimentTrader;
    }

    public ObjectInputStream<BaselineExpressions> createGeneExpressionsInputStream(String experimentAccession) {

        Experiment experiment = experimentTrader.getPublicExperiment(experimentAccession);

        String tsvFileURL = MessageFormat.format(baselineExperimentDataFileUrlTemplate, experimentAccession);
        CSVReader csvReader = csvReaderFactory.createTsvReader(tsvFileURL);

        if(experiment.getType().getDescription().equals("proteomics_baseline")) {
            return new BaselineExpressionsInputStream(csvReader, experimentAccession, proteomicsBaselineExpressionsQueueBuilder);
        } else {
            return new BaselineExpressionsInputStream(csvReader, experimentAccession, baselineExpressionsQueueBuilder);
        }
    }

}
