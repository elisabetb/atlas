package uk.ac.ebi.atlas.profiles.baseline.viewmodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.StringUtils;
import uk.ac.ebi.atlas.model.baseline.*;
import uk.ac.ebi.atlas.model.baseline.impl.FactorSet;
import uk.ac.ebi.atlas.search.baseline.BaselineExperimentProfile;
import uk.ac.ebi.atlas.search.baseline.BaselineExperimentProfilesList;
import uk.ac.ebi.atlas.search.baseline.BaselineExperimentSlice;
import uk.ac.ebi.atlas.utils.ColourGradient;
import uk.ac.ebi.atlas.utils.NumberUtils;
import uk.ac.ebi.atlas.web.FilterFactorsConverter;

import java.awt.*;
import java.util.Collection;
import java.util.SortedSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BaselineExperimentProfilesViewModelBuilderTest {

    private static final String ORGANISM_PART = "ORGANISM_PART";
    private static final Factor ADIPOSE = new Factor(ORGANISM_PART, "adipose", "ontologyTerm");
    private static final Factor ADRENAL = new Factor(ORGANISM_PART, "adrenal");
    private static final Factor BRAIN = new Factor(ORGANISM_PART, "brain");
    private static final Factor BREAST = new Factor(ORGANISM_PART, "breast");

    private static final AssayGroupFactor G1_ADIPOSE = new AssayGroupFactor("g1", ADIPOSE);
    private static final AssayGroupFactor G2_ADRENAL = new AssayGroupFactor("g2", ADRENAL);
    private static final AssayGroupFactor G3_BRAIN = new AssayGroupFactor("g3", BRAIN);
    private static final AssayGroupFactor G4_BREAST = new AssayGroupFactor("g4", BREAST);

    private static final ImmutableSortedSet<AssayGroupFactor> ASSAY_GROUP_FACTORS = ImmutableSortedSet.of(G1_ADIPOSE, G2_ADRENAL, G3_BRAIN, G4_BREAST);

    private Color startColour = Color.LIGHT_GRAY;
    private Color endColour = Color.BLUE;
    private Color blankColour = Color.WHITE;
    private double colourScale = 1;
    private ColourGradient colorGradient = new ColourGradient(startColour, endColour, blankColour, colourScale);
    private BaselineExpressionViewModelBuilder baselineExpressionViewModelBuilder = new BaselineExpressionViewModelBuilder(colorGradient, new NumberUtils());
    private BaselineExperimentProfilesViewModelBuilder subject = new BaselineExperimentProfilesViewModelBuilder(baselineExpressionViewModelBuilder, new NumberUtils(), new FilterFactorsConverter());
    private SortedSet<Factor> orderedFactors = ImmutableSortedSet.of(ADIPOSE, ADRENAL, BRAIN, BREAST);

    private static final FactorGroup EMPTY_FACTOR_SET = new FactorSet();


    @Mock
    private BaselineExperiment experiment1;
    private FactorGroup experiment1FilterFactors = new FactorSet(new Factor("ORGANISM", "Homo sapiens"));
    private SortedSet<Factor> experiment1nonFilterFactors = ImmutableSortedSet.of(ADIPOSE, BRAIN);
    private SortedSet<Factor> nonFilterFactors_EMPTY = ImmutableSortedSet.of();

    @Mock
    private BaselineExperiment experiment2;

    private BaselineExperimentProfile profile1;
    private BaselineExperimentProfile profile2;

    @Mock
    private BaselineExperimentSlice experimentSlice1;

    @Mock
    private BaselineExperimentSlice experimentSlice2;

    @Before
    public void before() {
        when(experimentSlice1.experiment()).thenReturn(experiment1);
        when(experimentSlice1.filterFactors()).thenReturn(experiment1FilterFactors);
        when(experimentSlice1.nonFilterFactors()).thenReturn(experiment1nonFilterFactors);
        when(experimentSlice1.experimentAccession()).thenReturn("EXP1");
        when(experimentSlice1.experimentDisplayName()).thenReturn("EXP1NAME");

        when(experimentSlice2.experiment()).thenReturn(experiment2);
        when(experimentSlice2.filterFactors()).thenReturn(EMPTY_FACTOR_SET);
        when(experimentSlice2.nonFilterFactors()).thenReturn(nonFilterFactors_EMPTY);
        when(experimentSlice2.experimentAccession()).thenReturn("EXP2");
        when(experimentSlice2.experimentDisplayName()).thenReturn("EXP2NAME");

        profile1 = new BaselineExperimentProfile(experimentSlice1);
        profile2 = new BaselineExperimentProfile(experimentSlice2);
    }

    @Test
    public void buildProfilesViewModel() {
        Collection<BaselineExperimentProfile> baselineExperimentProfiles = ImmutableList.of(profile1, profile2);
        BaselineExperimentProfilesList baselineExperimentProfilesList = new BaselineExperimentProfilesList(baselineExperimentProfiles);
        BaselineProfilesViewModel profiles = subject.build(baselineExperimentProfilesList, orderedFactors);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(profiles);

        String expected = "{\n" +
                "  \"minExpressionLevel\": 1.7976931348623157E308,\n" +
                "  \"maxExpressionLevel\": 0.0,\n" +
                "  \"searchResultTotal\": 0,\n" +
                "  \"rows\": [\n" +
                "    {\n" +
                "      \"id\": \"EXP1\",\n" +
                "      \"name\": \"EXP1NAME\",\n" +
                "      \"expressions\": [\n" +
                "        {\n" +
                "          \"factorName\": \"adipose\",\n" +
                "          \"color\": \"\",\n" +
                "          \"value\": \"\",\n" +
                "          \"svgPathId\": \"ontologyTerm\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"factorName\": \"adrenal\",\n" +
                "          \"color\": \"\",\n" +
                "          \"value\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"factorName\": \"brain\",\n" +
                "          \"color\": \"\",\n" +
                "          \"value\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"factorName\": \"breast\",\n" +
                "          \"color\": \"\",\n" +
                "          \"value\": \"\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"serializedFilterFactors\": \"ORGANISM:Homo sapiens\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"EXP2\",\n" +
                "      \"name\": \"EXP2NAME\",\n" +
                "      \"expressions\": [\n" +
                "        {\n" +
                "          \"factorName\": \"adipose\",\n" +
                "          \"color\": \"\",\n" +
                "          \"value\": \"\",\n" +
                "          \"svgPathId\": \"ontologyTerm\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"factorName\": \"adrenal\",\n" +
                "          \"color\": \"\",\n" +
                "          \"value\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"factorName\": \"brain\",\n" +
                "          \"color\": \"\",\n" +
                "          \"value\": \"\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"factorName\": \"breast\",\n" +
                "          \"color\": \"\",\n" +
                "          \"value\": \"\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"serializedFilterFactors\": \"\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        assertThat(json, is(expected));

    }

    @Test
    public void buildAssayGroupFactorsViewModel() {
        Gson gson = new Gson();
        String json = gson.toJson(ASSAY_GROUP_FACTORS);

        String expected = "[\n" +
                "  {\n" +
                "    \"assayGroupId\": \"g1\",\n" +
                "    \"factor\": {\n" +
                "      \"header\": \"ORGANISM_PART\",\n" +
                "      \"type\": \"ORGANISM_PART\",\n" +
                "      \"value\": \"adipose\",\n" +
                "      \"valueOntologyTerm\": \"ontologyTerm\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"assayGroupId\": \"g2\",\n" +
                "    \"factor\": {\n" +
                "      \"header\": \"ORGANISM_PART\",\n" +
                "      \"type\": \"ORGANISM_PART\",\n" +
                "      \"value\": \"adrenal\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"assayGroupId\": \"g3\",\n" +
                "    \"factor\": {\n" +
                "      \"header\": \"ORGANISM_PART\",\n" +
                "      \"type\": \"ORGANISM_PART\",\n" +
                "      \"value\": \"brain\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"assayGroupId\": \"g4\",\n" +
                "    \"factor\": {\n" +
                "      \"header\": \"ORGANISM_PART\",\n" +
                "      \"type\": \"ORGANISM_PART\",\n" +
                "      \"value\": \"breast\"\n" +
                "    }\n" +
                "  }\n" +
                "]";

        assertThat(json, is(StringUtils.trimAllWhitespace(expected)));

    }

}