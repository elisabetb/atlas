package uk.ac.ebi.atlas.thirdpartyintegration.ebeye;

import org.junit.Test;
import uk.ac.ebi.atlas.model.AssayGroup;
import uk.ac.ebi.atlas.model.ExperimentDesign;
import uk.ac.ebi.atlas.model.differential.Contrast;
import uk.ac.ebi.atlas.model.differential.DifferentialExperiment;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DifferentialExperimentContrastLinesTest {

    private static final String EXPERIMENT_ACCESSION = "EXPERIMENT_ACCESSION";
    private static final String CONTRAST_ID = "CONTRAST_ID";
    private static final String ASSAY1 = "ASSAY1";
    private static final String ASSAY2 = "ASSAY2";
    private static final String ASSAY3 = "ASSAY3";
    private static final String ASSAY4 = "ASSAY4";
    private static final String SAMPLE_HEADER = "SAMPLE_HEADER1";
    private static final String FACTOR_HEADER = "FACTOR_HEADER1";

    private static final String FACTOR_VALUE1 = "FACTOR_VALUE1";
    private static final String FACTOR_VALUE2 = "FACTOR_VALUE2";

    private static final String SAMPLE_VALUE1 = "SAMPLE_VALUE1";
    private static final String SAMPLE_VALUE2 = "SAMPLE_VALUE2";
    private static final String SAMPLE_VALUE3 = "SAMPLE_VALUE3";
    private static final String SAMPLE_VALUE4 = "SAMPLE_VALUE4";
    private static final String REFERENCE = "reference";
    private static final String CHARACTERISTIC = "characteristic";
    private static final String FACTOR = "factor";
    private static final String TEST = "test";

    private DifferentialExperimentContrastLines subject;

    @Test
    public void contrastDetailLines() {
        AssayGroup referenceAssayGroup = new AssayGroup("g1", ASSAY1, ASSAY2);
        AssayGroup testAssayGroup = new AssayGroup("g2", ASSAY3, ASSAY4);
        Contrast contrast1 = new Contrast(CONTRAST_ID, "array design accession", referenceAssayGroup, testAssayGroup, "display name");

        ExperimentDesign experimentDesign = new ExperimentDesign();

        experimentDesign.putSample(ASSAY1, SAMPLE_HEADER, SAMPLE_VALUE1);
        experimentDesign.putFactor(ASSAY1, FACTOR_HEADER, FACTOR_VALUE1);
        experimentDesign.putSample(ASSAY2, SAMPLE_HEADER, SAMPLE_VALUE2);
        experimentDesign.putFactor(ASSAY2, FACTOR_HEADER, FACTOR_VALUE1);

        experimentDesign.putSample(ASSAY3, SAMPLE_HEADER, SAMPLE_VALUE3);
        experimentDesign.putFactor(ASSAY3, FACTOR_HEADER, FACTOR_VALUE2);
        experimentDesign.putSample(ASSAY4, SAMPLE_HEADER, SAMPLE_VALUE4);
        experimentDesign.putFactor(ASSAY4, FACTOR_HEADER, FACTOR_VALUE2);

        Set<Contrast> contrasts = Collections.singleton(contrast1);
        DifferentialExperiment experiment = new DifferentialExperiment(EXPERIMENT_ACCESSION, new Date(), contrasts, "description", false, Collections.EMPTY_SET, Collections.EMPTY_SET, experimentDesign);

        this.subject = new DifferentialExperimentContrastLines(experiment);

        Iterator<String[]> lines = subject.iterator();

        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, REFERENCE, CHARACTERISTIC, SAMPLE_HEADER, SAMPLE_VALUE2}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, REFERENCE, FACTOR, FACTOR_HEADER, FACTOR_VALUE1}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, REFERENCE, CHARACTERISTIC, SAMPLE_HEADER, SAMPLE_VALUE1}));

        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, TEST, CHARACTERISTIC, SAMPLE_HEADER, SAMPLE_VALUE4}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, TEST, FACTOR, FACTOR_HEADER, FACTOR_VALUE2}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, TEST, CHARACTERISTIC, SAMPLE_HEADER, SAMPLE_VALUE3}));
    }

    @Test
    public void duplicateContrastDetailLines(){
        AssayGroup referenceAssayGroup = new AssayGroup("g1", ASSAY1, ASSAY2);
        AssayGroup testAssayGroup = new AssayGroup("g2", ASSAY3, ASSAY4);
        Contrast contrast1 = new Contrast(CONTRAST_ID, "array design accession", referenceAssayGroup, testAssayGroup, "display name");

        ExperimentDesign experimentDesign = new ExperimentDesign();
        //This group is for Reference Assay (added two same assay to the group)
        experimentDesign.putSample(ASSAY1, SAMPLE_HEADER, SAMPLE_VALUE1);
        experimentDesign.putFactor(ASSAY1, FACTOR_HEADER, FACTOR_VALUE1);

        experimentDesign.putSample(ASSAY2, SAMPLE_HEADER, SAMPLE_VALUE1);
        experimentDesign.putFactor(ASSAY2, FACTOR_HEADER, FACTOR_VALUE1);

        //This group is for Test Assay
        experimentDesign.putSample(ASSAY3, SAMPLE_HEADER, SAMPLE_VALUE3);
        experimentDesign.putFactor(ASSAY3, FACTOR_HEADER, FACTOR_VALUE2);
        experimentDesign.putSample(ASSAY4, SAMPLE_HEADER, SAMPLE_VALUE4);
        experimentDesign.putFactor(ASSAY4, FACTOR_HEADER, FACTOR_VALUE2);

        //Adding the contrasts
        Set<Contrast> contrasts = Collections.singleton(contrast1);
        DifferentialExperiment experiment = new DifferentialExperiment(EXPERIMENT_ACCESSION, new Date(), contrasts,
                "description", false, Collections.EMPTY_SET, Collections.EMPTY_SET, experimentDesign);

        this.subject = new DifferentialExperimentContrastLines(experiment);

        Iterator<String[]> lines = subject.iterator();

        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, REFERENCE, CHARACTERISTIC, SAMPLE_HEADER, SAMPLE_VALUE1}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, REFERENCE, FACTOR, FACTOR_HEADER, FACTOR_VALUE1}));

        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, TEST, CHARACTERISTIC, SAMPLE_HEADER, SAMPLE_VALUE4}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, TEST, FACTOR, FACTOR_HEADER, FACTOR_VALUE2}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, TEST, CHARACTERISTIC, SAMPLE_HEADER, SAMPLE_VALUE3}));
    }

    @Test
    public void emptyValuesDetailLines(){
        AssayGroup referenceAssayGroup = new AssayGroup("g1", ASSAY1, ASSAY2);
        AssayGroup testAssayGroup = new AssayGroup("g2", ASSAY3, ASSAY4);
        Contrast contrast1 = new Contrast(CONTRAST_ID, "array design accession", referenceAssayGroup, testAssayGroup, "display name");

        ExperimentDesign experimentDesign = new ExperimentDesign();
        //This group is for Reference Assay (added two same assay to the group)
        experimentDesign.putSample(ASSAY1, SAMPLE_HEADER, "");
        experimentDesign.putFactor(ASSAY1, FACTOR_HEADER, FACTOR_VALUE1);

        experimentDesign.putSample(ASSAY2, SAMPLE_HEADER, SAMPLE_VALUE2);
        experimentDesign.putFactor(ASSAY2, FACTOR_HEADER, FACTOR_VALUE1);

        //This group is for Test Assay
        experimentDesign.putSample(ASSAY3, SAMPLE_HEADER, SAMPLE_VALUE3);
        experimentDesign.putFactor(ASSAY3, FACTOR_HEADER, FACTOR_VALUE2);
        experimentDesign.putSample(ASSAY4, SAMPLE_HEADER, SAMPLE_VALUE4);
        experimentDesign.putFactor(ASSAY4, FACTOR_HEADER, FACTOR_VALUE2);

        //Adding the contrasts
        Set<Contrast> contrasts = Collections.singleton(contrast1);
        DifferentialExperiment experiment = new DifferentialExperiment(EXPERIMENT_ACCESSION, new Date(), contrasts,
                "description", false, Collections.EMPTY_SET, Collections.EMPTY_SET, experimentDesign);

        this.subject = new DifferentialExperimentContrastLines(experiment);

        Iterator<String[]> lines = subject.iterator();

        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, REFERENCE, CHARACTERISTIC, SAMPLE_HEADER, SAMPLE_VALUE2}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, REFERENCE, FACTOR, FACTOR_HEADER, FACTOR_VALUE1}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, REFERENCE, CHARACTERISTIC, SAMPLE_HEADER, ""}));

        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, TEST, CHARACTERISTIC, SAMPLE_HEADER, SAMPLE_VALUE4}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, TEST, FACTOR, FACTOR_HEADER, FACTOR_VALUE2}));
        assertThat(lines.next(), is(new String[]{EXPERIMENT_ACCESSION, CONTRAST_ID, TEST, CHARACTERISTIC, SAMPLE_HEADER, SAMPLE_VALUE3}));
    }
}
