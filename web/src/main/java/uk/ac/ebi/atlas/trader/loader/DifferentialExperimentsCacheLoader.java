package uk.ac.ebi.atlas.trader.loader;

import uk.ac.ebi.atlas.experimentimport.ExperimentDTO;
import uk.ac.ebi.atlas.model.ExperimentConfiguration;
import uk.ac.ebi.atlas.model.ExperimentDesign;
import uk.ac.ebi.atlas.model.differential.Contrast;
import uk.ac.ebi.atlas.model.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.trader.ConfigurationTrader;
import uk.ac.ebi.atlas.trader.SpeciesFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Set;

@Named
public class DifferentialExperimentsCacheLoader extends ExperimentsCacheLoader<DifferentialExperiment> {

    private ConfigurationTrader configurationTrader;

    private SpeciesFactory speciesFactory;

    @Inject
    public DifferentialExperimentsCacheLoader(ConfigurationTrader configurationTrader, SpeciesFactory speciesFactory) {
        this.configurationTrader = configurationTrader;
        this.speciesFactory = speciesFactory;
    }

    @Override
    protected DifferentialExperiment load(ExperimentDTO experimentDTO, String experimentDescription,
                                          boolean hasExtraInfoFile, ExperimentDesign experimentDesign) throws IOException {

        String experimentAccession = experimentDTO.getExperimentAccession();

        ExperimentConfiguration experimentConfiguration = configurationTrader.getExperimentConfiguration(experimentAccession);
        Set<Contrast> contrasts = experimentConfiguration.getContrasts();

        boolean hasRData = experimentConfiguration.hasRData();

        return new DifferentialExperiment(experimentAccession, experimentDTO.getLastUpdate(), contrasts,
                experimentDescription, hasExtraInfoFile, hasRData, speciesFactory.create(experimentDTO),
                experimentDTO.getPubmedIds(), experimentDesign);

    }
}
