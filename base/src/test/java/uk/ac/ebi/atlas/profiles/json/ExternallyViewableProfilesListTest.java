package uk.ac.ebi.atlas.profiles.json;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Test;
import uk.ac.ebi.atlas.model.AssayGroup;
import uk.ac.ebi.atlas.model.AssayGroupsFake;
import uk.ac.ebi.atlas.model.ExpressionUnit;
import uk.ac.ebi.atlas.model.FactorAcrossExperiments;
import uk.ac.ebi.atlas.model.experiment.baseline.*;
import uk.ac.ebi.atlas.model.experiment.baseline.impl.FactorSet;
import uk.ac.ebi.atlas.model.experiment.differential.*;
import uk.ac.ebi.atlas.model.experiment.differential.rnaseq.RnaSeqProfile;
import uk.ac.ebi.atlas.search.SemanticQuery;
import uk.ac.ebi.atlas.search.baseline.BaselineExperimentProfile;
import uk.ac.ebi.atlas.search.baseline.BaselineExperimentProfilesList;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ExternallyViewableProfilesListTest {

    @Test
    public void weCanSerializeDataForTheWidget() throws Exception {
        String defaultQueryFactorType = "type1";

        BaselineExperimentProfile firstProfile =
                new BaselineExperimentProfile(
                        BaselineExperimentTest.mockExperiment(AssayGroupsFake.get(), "experiment_1"),
                        new FactorSet());

        FactorAcrossExperiments f11 = new FactorAcrossExperiments(new Factor(defaultQueryFactorType, "11"));
        firstProfile.add(f11, new BaselineExpression(12.34));

        BaselineExperimentProfile secondProfile = new BaselineExperimentProfile(
                BaselineExperimentTest.mockExperiment(AssayGroupsFake.get(), "experiment_2"),
                new FactorSet());
        FactorAcrossExperiments f21 = new FactorAcrossExperiments(new Factor(defaultQueryFactorType, "21"));

        secondProfile.add(f21, new BaselineExpression(56.78));


        FactorAcrossExperiments f3 = new FactorAcrossExperiments(new Factor(defaultQueryFactorType, "3"));

        List<FactorAcrossExperiments> factorsAcrossExperiments = ImmutableList.of(f11, f21, f3);

        BaselineExperimentProfilesList profiles = new BaselineExperimentProfilesList();
        profiles.add(firstProfile);
        profiles.add(secondProfile);

        JsonObject result =
                ExternallyViewableProfilesList.createForExperimentProfiles(
                        SemanticQuery.create(), profiles,factorsAcrossExperiments).asJson();


        assertThat(
                result.get("rows").getAsJsonArray().size(),
                is(ImmutableList.of(firstProfile, secondProfile).size()));

        for(JsonElement row: result.get("rows").getAsJsonArray()){
            assertThat(
                    row.getAsJsonObject().get("expressions").getAsJsonArray().size(),
                    is(factorsAcrossExperiments.size()));
        }

    }


    @Test
    public void weCanSerializeDataForTheBaselinePage() throws Exception {
        Function<BaselineProfile, URI> provideLinkToProfile = new Function<BaselineProfile, URI>() {
            @Nullable
            @Override
            public URI apply(@Nullable BaselineProfile o) {
                try {
                    return new URI("https://www.ebi.ac.uk/gxa/genes/" + o.getId());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        AssayGroup g1 = new AssayGroup("g1", "run_11", "run_12", "run_13");
        AssayGroup g2 = new AssayGroup("g2", "run_21", "run_22", "run_23", "run_24");

        List<AssayGroup> factorsAcrossExperiments = ImmutableList.of(g1, g2);

        BaselineProfile p1 = new BaselineProfile("gene_1_id", "gene_1_name");
        p1.add(g1, new BaselineExpression(1000));
        p1.add(g2, new BaselineExpression(2000));


        BaselineProfile p2 = new BaselineProfile("gene_2_id", "gene_2_name");
        p2.add(g1, new BaselineExpression(3000));

        BaselineProfilesList profiles = new BaselineProfilesList();
        profiles.add(p1);
        profiles.add(p2);

        JsonObject result =
                new ExternallyViewableProfilesList<>(
                        profiles, provideLinkToProfile, factorsAcrossExperiments,
                        baselineProfile -> ExpressionUnit.Absolute.Rna.TPM).asJson();


        assertThat(result.get("rows").getAsJsonArray().size(), is(ImmutableList.of(p1, p2).size()));

        for(JsonElement row: result.get("rows").getAsJsonArray()){
            assertThat(
                    row.getAsJsonObject().get("expressions").getAsJsonArray().size(),
                    is(factorsAcrossExperiments.size()));
        }
    }

    @Test
    public void weCanSerializeDataForTheDifferentialPage() throws Exception {
        Function<RnaSeqProfile, URI> provideLinkToProfile = new Function<RnaSeqProfile, URI>() {
            @Nullable
            @Override
            public URI apply(@Nullable RnaSeqProfile o) {
                try {
                    return new URI("https://www.ebi.ac.uk/gxa/genes/" + o.getId());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };


        AssayGroup g1 = new AssayGroup("g1", "run_11", "run_12", "run_13");
        AssayGroup g2 = new AssayGroup("g2", "run_21", "run_22", "run_23", "run_24");
        AssayGroup g3 = new AssayGroup("g3", "run_31", "run_32");

        Contrast g1_g2 = new Contrast("g1_g2","", g1, g2, "first contrast");
        Contrast g1_g3 = new Contrast("g1_g3","", g1, g3, "second contrast");


        List<Contrast> factorsAcrossExperiments = ImmutableList.of(g1_g2, g1_g3);

        RnaSeqProfile p1 = new RnaSeqProfile("gene_1_id", "gene_1_name");
        p1.add(g1_g2, new DifferentialExpression(1000, 10));
        p1.add(g1_g3, new DifferentialExpression(2000, 20));


        RnaSeqProfile p2 = new RnaSeqProfile("gene_2_id", "gene_2_name");
        p2.add(g1_g2, new DifferentialExpression(3000, 30));

        DifferentialProfilesList<RnaSeqProfile> profiles = new DifferentialProfilesList<>();
        profiles.add(p1);
        profiles.add(p2);

        JsonObject result =
                new ExternallyViewableProfilesList<>(
                        profiles, provideLinkToProfile, factorsAcrossExperiments,
                        rnaSeqProfile -> ExpressionUnit.Relative.FOLD_CHANGE).asJson();


        assertThat(result.get("rows").getAsJsonArray().size(), is(ImmutableList.of(p1, p2).size()));

        for(JsonElement row: result.get("rows").getAsJsonArray()){
            assertThat(row.getAsJsonObject().get("expressions").getAsJsonArray().size(), is(factorsAcrossExperiments.size()));
        }
    }
}