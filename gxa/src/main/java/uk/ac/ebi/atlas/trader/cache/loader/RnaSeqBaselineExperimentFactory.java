package uk.ac.ebi.atlas.trader.cache.loader;

import uk.ac.ebi.atlas.resource.DataFileHub;
import uk.ac.ebi.atlas.species.SpeciesFactory;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class RnaSeqBaselineExperimentFactory extends BaselineExperimentFactory {

    @Inject
    public RnaSeqBaselineExperimentFactory(ConfigurationTrader configurationTrader,
                                           SpeciesFactory speciesFactory, DataFileHub dataFileHub) {
        super(ExperimentType.RNASEQ_MRNA_BASELINE,configurationTrader, speciesFactory);
    }
}
