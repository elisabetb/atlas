package uk.ac.ebi.atlas.experiments;


import com.google.common.collect.ImmutableList;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.model.experiment.ExperimentType;
import uk.ac.ebi.atlas.trader.ExperimentTrader;
import uk.ac.ebi.atlas.utils.ExperimentInfo;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:solrContext.xml", "classpath:oracleContext.xml"})
public class ExperimentInfoListServiceIT {

    @Inject
    ExperimentTrader experimentTrader;


    ExperimentInfoListService subject;

    Collection<ExperimentType> types =
            ImmutableList.of(ExperimentType.SINGLE_CELL_RNASEQ_MRNA_BASELINE);

    @Before
    public void setUp(){
        subject = new ExperimentInfoListService(experimentTrader, types);
    }
    @Test
    public void fetchLatest(){
        List<ExperimentInfo> experiments = subject.getLatest();

        assertThat(experiments.size(), Matchers.greaterThan(0));
        assertThat(experiments.size(), Matchers.lessThan(10));

        DateTime tHead = DateTime.parse(experiments.get(0).getLastUpdate(), ExperimentInfoListService.expectedDateFormat);
        DateTime tTail = DateTime.parse(experiments.get(experiments.size()-1).getLastUpdate(),
                ExperimentInfoListService.expectedDateFormat);

        assertTrue("First element of the list was updated after the last one" , tHead.isAfter(tTail) || tHead.equals(tTail));
    }



}
