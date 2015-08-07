package uk.ac.ebi.atlas.experimentimport.experimentdesign.condensedSdrf;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import uk.ac.ebi.atlas.commons.readers.TsvReader;
import uk.ac.ebi.atlas.commons.readers.FileTsvReaderBuilder;
import uk.ac.ebi.atlas.model.ExperimentDesign;
import uk.ac.ebi.atlas.model.ExperimentType;
import uk.ac.ebi.atlas.model.OntologyTerm;
import uk.ac.ebi.atlas.model.SampleCharacteristic;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by Alfonso Muñoz-Pomer Fuentes <amunoz@ebi.ac.uk> on 24/07/15.
 */

@Named
@Scope("prototype")
public class CondensedSdrfParser {

    private static final Logger LOGGER = Logger.getLogger(CondensedSdrfParser.class);

    @Value("#{configuration['experiment.condensed-sdrf.path.template']}")
    private String sdrfPathTemplate;

    private static final Set<String> FACTORS_NEEDING_DOSE = Sets.newHashSet("compound", "irradiate");

    private static final String DOSE = "dose";
    private static final String ONTOLOGY_TERM_DELIMITER = " ";

    private static final int EXPERIMENT_ACCESSION_INDEX = 0;
    private static final int ASSAY_DESIGN_INDEX = 1;
    private static final int RUN_OR_ASSAY_INDEX = 2;
    private static final int FACTOR_OR_CHARACTERISTIC_INDEX = 3;
    private static final int FACTOR_TYPE_INDEX = 4;
    private static final int CHARACTERISTIC_TYPE_INDEX = 4;
    private static final int FACTOR_VALUE_INDEX = 5;
    private static final int CHARACTERISTIC_VALUE_INDEX = 5;
    private static final int ONTOLOGY_TERMS_INDEX = 6;

    private static final String FACTOR = "factor";
    private static final String CHARACTERISTIC = "characteristic";

    private FileTsvReaderBuilder fileTsvReaderBuilder;
    private final IdfParser idfParser;
    private final ValueAndUnitJoiner valueAndUnitJoiner;

    @Inject
    public CondensedSdrfParser(FileTsvReaderBuilder fileTsvReaderBuilder, IdfParser idfParser, ValueAndUnitJoiner valueAndUnitJoiner) {
        this.fileTsvReaderBuilder = fileTsvReaderBuilder;
        this.idfParser = idfParser;
        this.valueAndUnitJoiner = valueAndUnitJoiner;
    }

    public CondensedSdrfParserOutput parse(String experimentAccession, ExperimentType experimentType) throws IOException, CondensedSdrfParserException {
        ExperimentDesign experimentDesign = new ExperimentDesign();

        TsvReader tsvReader = fileTsvReaderBuilder.forTsvFilePathTemplate(sdrfPathTemplate).withExperimentAccession(experimentAccession).build();

        ImmutableList.Builder<String[]> factorsBuilder = new ImmutableList.Builder<>();
        ImmutableList.Builder<String[]> sampleCharacteristicsBuilder = new ImmutableList.Builder<>();

        for (String[] tsvLine : tsvReader.readAll()) {
            if (tsvLine[FACTOR_OR_CHARACTERISTIC_INDEX].equals(FACTOR)) {
                factorsBuilder.add(tsvLine);
            } else if (tsvLine[FACTOR_OR_CHARACTERISTIC_INDEX].equals(CHARACTERISTIC)) {
                sampleCharacteristicsBuilder.add(tsvLine);
            } else {
                LOGGER.warn(String.format(
                        "Parsing condensed SDRF for %s: Unknown factor/characteristic descriptor “%s”",
                        experimentAccession, tsvLine[FACTOR_OR_CHARACTERISTIC_INDEX]));
            }
        }

        Multimap<String, String[]> assayRunToTsvLines = mapFactorTsvLinesByAssayRun(factorsBuilder.build());
        addFactorValuesToExperimentDesign(experimentDesign, assayRunToTsvLines);
        addCharacteristicToExperimentDesign(experimentDesign, sampleCharacteristicsBuilder.build());

        addArrays(experimentDesign, assayRunToTsvLines);

        idfParser.parse(experimentAccession);

        return new CondensedSdrfParserOutput(experimentAccession, experimentType, idfParser.getTitle(), idfParser.getPubMedIds(), experimentDesign);
    }

    private void addCharacteristicToExperimentDesign(ExperimentDesign experimentDesign, List<String[]> sampleCharacteristicsTsvLines) {

        for (String[] sampleCharacteristicTsvLine : sampleCharacteristicsTsvLines) {
            String header = sampleCharacteristicTsvLine[CHARACTERISTIC_TYPE_INDEX];
            String value = cleanValueAndUnitIfNeeded(sampleCharacteristicTsvLine[CHARACTERISTIC_VALUE_INDEX]);
            String characteristicValueOntologyTermAsString = sampleCharacteristicTsvLine.length > ONTOLOGY_TERMS_INDEX ? sampleCharacteristicTsvLine[ONTOLOGY_TERMS_INDEX] : null;
            OntologyTerm[] characteristicOntologyTerms = parseOntologyTerms(characteristicValueOntologyTermAsString);

            SampleCharacteristic sampleCharacteristic = SampleCharacteristic.create(header, value, characteristicOntologyTerms);
            experimentDesign.putSampleCharacteristic(sampleCharacteristicTsvLine[RUN_OR_ASSAY_INDEX], header, sampleCharacteristic);
        }
    }

    private void addFactorValuesToExperimentDesign(ExperimentDesign experimentDesign, Multimap<String, String[]> factorsByAssayRun) {

        for (String runOrAssay : factorsByAssayRun.keys()) {

            String compoundFactorType = null;
            String compoundFactorValue = null;
            String compoundFactorValueOntologyTermAsString = null;

            for (String[] factorTsvLine : factorsByAssayRun.get(runOrAssay)) {

                String factorType = factorTsvLine[FACTOR_TYPE_INDEX];
                String factorValue = cleanValueAndUnitIfNeeded(factorTsvLine[FACTOR_VALUE_INDEX]);
                String factorValueOntologyTermAsString = factorTsvLine.length > ONTOLOGY_TERMS_INDEX ? factorTsvLine[ONTOLOGY_TERMS_INDEX] : null;

                if (isFactorThatHasADose(factorType)) {

                    compoundFactorType = factorType;
                    compoundFactorValue = factorValue;
                    compoundFactorValueOntologyTermAsString = factorValueOntologyTermAsString;

                } else if (isDoseFactor(factorType)) {

                    if (!Strings.isNullOrEmpty(compoundFactorValue)) {
                        factorValue = Joiner.on(" ").join(compoundFactorValue, factorValue);
                        factorType = compoundFactorType;
                        factorValueOntologyTermAsString = compoundFactorValueOntologyTermAsString;

                        compoundFactorType = null;
                        compoundFactorValue = null;
                        compoundFactorValueOntologyTermAsString = null;
                    } else {
                        throw new IllegalStateException(DOSE + " : " + factorValue + " has no corresponding value for any of the following factors: " + FACTORS_NEEDING_DOSE);
                    }

                }

                experimentDesign.putFactor(runOrAssay, factorType, factorValue, parseOntologyTerms(factorValueOntologyTermAsString));
            }

            //Add compound factor in a case there was no dose corresponding to it
            if (!Strings.isNullOrEmpty(compoundFactorType) && !Strings.isNullOrEmpty(compoundFactorValue)) {
                experimentDesign.putFactor(runOrAssay, compoundFactorType, compoundFactorValue, parseOntologyTerms(compoundFactorValueOntologyTermAsString));
            }

        }

    }


    private boolean isFactorThatHasADose(String factorName) {
        return FACTORS_NEEDING_DOSE.contains(factorName.toLowerCase());
    }

    private boolean isDoseFactor(String factorName) {
        return DOSE.equals(factorName.toLowerCase());
    }

    private OntologyTerm[] parseOntologyTerms(String ontologyTermTsvField) {
        ImmutableSet.Builder<OntologyTerm> ontologyTermsBuilder = new ImmutableSet.Builder<>();

        if (!Strings.isNullOrEmpty(ontologyTermTsvField)) {
            for (String ontologyTermAsString : ontologyTermTsvField.split(ONTOLOGY_TERM_DELIMITER)) {
                ontologyTermsBuilder.add(OntologyTerm.createFromUri(ontologyTermAsString));
            }
        }

        Set<OntologyTerm> ontologyTerms = ontologyTermsBuilder.build();
        return ontologyTerms.toArray(new OntologyTerm[ontologyTerms.size()]);
    }

    private void addArrays(ExperimentDesign experimentDesign, Multimap<String, String[]> assayRunToTsvLines) {
        for (String assayRun : assayRunToTsvLines.keys()) {

            String assayDesign = assayRunToTsvLines.get(assayRun).iterator().next()[ASSAY_DESIGN_INDEX];
            for (String[] tsvLine : assayRunToTsvLines.get(assayRun)) {
                if (!tsvLine[ASSAY_DESIGN_INDEX].equals(assayDesign)) {
                    throw new IllegalStateException("Assays with multiple array designs are not supported.");
                }
            }

            experimentDesign.putArrayDesign(assayRun, assayDesign);
        }
    }


    protected String cleanValueAndUnitIfNeeded(String value) {
//        if (!Strings.isNullOrEmpty(value)) {
//            value = value.replaceAll("( )+", " ").replaceAll("(_)+", "_").trim();
//            value = valueAndUnitJoiner.pluraliseAndJoin(value);
//        }
        return value;
    }


    private Multimap<String, String[]> mapFactorTsvLinesByAssayRun(List<String[]> factorTsvLines) {

        ImmutableMultimap.Builder<String, String[]> multimapBuilder = new ImmutableMultimap.Builder<>();

        for (String[] factorTsvLine : factorTsvLines) {
            multimapBuilder.put(factorTsvLine[RUN_OR_ASSAY_INDEX], factorTsvLine);
        }

        return multimapBuilder.build();
    }


    private class CondensedSdrfParserException extends RuntimeException {
        public CondensedSdrfParserException(String message) {
            super(message);
        }

    }
}
