package uk.ac.ebi.atlas.trader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:solrContext.xml", "classpath:oracleContext.xml"})
public class SpeciesKingdomTraderIT {

    @Inject
    JdbcTemplate jdbcTemplate;

    SpeciesKingdomTrader subject;

    @Before
    public void setUp(){
        this.subject = new SpeciesKingdomTrader(jdbcTemplate);
    }

    @Test
    public void caenorhabditisElegansKingomIsAnimals() throws Exception {
        assertThat(subject.getKingdom("caenorhabditis elegans"), is("animals"));
    }

    @Test
    public void bosTaurusKingdomIsAnimals() throws Exception {
        assertThat(subject.getKingdom("bos taurus"), is("animals"));
    }

    @Test
    public void oryzaSativaJaponicaGroupKingodmIsPlants() throws Exception {
        assertThat(subject.getKingdom("oryza sativa japonica group"), is("plants"));
    }


}