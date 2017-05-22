package uk.ac.ebi.atlas.search.diffanalytics;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

public class DiffAnalyticsDAOTest {


    @Mock
    JdbcTemplate jdbcTemplate;


    @Mock
    DiffAnalyticsRowMapper dbeRowMapper;


    DiffAnalyticsDao subject;

    @Before
            public void setUp(){
        MockitoAnnotations.initMocks(this);
        this.subject = new DiffAnalyticsDao(jdbcTemplate,dbeRowMapper);
    }

    @Test
    public void weQueryWithSomeReallyGoodSql(){
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.when(jdbcTemplate.query(captor.capture(), eq(dbeRowMapper),  any())).thenReturn(new ArrayList<DiffAnalytics>());

        String geneId = "geneId";

        subject.fetchTopExpressions(geneId);

        String v = captor.getValue();

        assertTrue(Pattern.matches("SELECT.*FROM.*", v.toUpperCase()));
        assertTrue(v.toUpperCase().contains("PRIVATE = 'F'"));
    }

}
