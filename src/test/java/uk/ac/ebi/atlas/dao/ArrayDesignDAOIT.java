package uk.ac.ebi.atlas.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:solrContext.xml", "classpath:oracleContext.xml"})
public class ArrayDesignDAOIT {

    @Inject
    private ArrayDesignDAO subject;

    @Test
    public void testGetDesignElements() throws Exception {
        List<String> designElements = subject.getDesignElements("ENSG00000109929");
        assertThat(designElements, hasItem("211423_s_at"));
    }

    @Test
    public void testGetAllArrayDesignMapNames() throws Exception {
        Map<String,String> arrayDesigns = subject.getArrayDesignMapNames();
        assertThat(arrayDesigns.size(), is(74));
    }

}