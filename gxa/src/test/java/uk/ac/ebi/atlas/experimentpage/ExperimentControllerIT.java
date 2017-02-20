package uk.ac.ebi.atlas.experimentpage;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.model.DescribesDataColumns;
import uk.ac.ebi.atlas.model.experiment.Experiment;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import javax.annotation.Nullable;
import javax.inject.Inject;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:solrContext.xml", "classpath:oracleContext.xml"})
public class ExperimentControllerIT {

    @Inject
    ExperimentController subject;

    @Inject
    ExperimentTrader experimentTrader;

    @Test
    public void testSomeGoodExperiments() throws Exception {

        outputInFineFormatForExperiment("E-MTAB-2706");
        outputInFineFormatForExperiment("E-GEOD-54705");
        outputInFineFormatForExperiment("E-MTAB-4260");
        outputInFineFormatForExperiment("E-MTAB-513");




    }

    /*
    For differential experiments, a few groupings repeat and we might need something else for them.

     */

    public void outputInFineFormatForExperiment(String accession){
        Experiment<DescribesDataColumns> experiment = experimentTrader.getPublicExperiment(accession);

        List<String> allDescriptorIds = FluentIterable.from(experiment.getDataColumnDescriptors()).transform(
                new Function<DescribesDataColumns, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable DescribesDataColumns describesDataColumns) {
                        return describesDataColumns.getId();
                    }
                }).toList();

        JsonArray result = subject.groupingsForHeatmap(experiment);

        assertThat(result.size(), greaterThan(0));

        for(JsonElement element: result){
            assertTrue(element.getAsJsonObject().has("name"));
            assertTrue(element.getAsJsonObject().has("selected"));
            for(JsonElement grouping: element.getAsJsonObject().get("groupings").getAsJsonArray()){
                assertThat(grouping.getAsJsonArray().size(), is(2));
                for(JsonElement groupingValue : grouping.getAsJsonArray().get(1).getAsJsonArray()){
                    assertTrue(allDescriptorIds.contains(groupingValue.getAsString()));
                }
            }
        }
    }
}