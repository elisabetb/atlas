package uk.ac.ebi.atlas.model.readers;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class AnalysisMethodsTsvReaderTest {

    private static final String PATH_TEMPLATE = "A_PATH_TEMPLATE";

    private TsvReader subject = new TsvReaderImpl(PATH_TEMPLATE);

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testIsComment() {
        TsvReaderImpl.IsComment isCommentPredicate = new TsvReaderImpl.IsComment();
        assertThat(isCommentPredicate.apply("  #  Xyz"), is(true));
        assertThat(isCommentPredicate.apply(" #Xyz"), is(true));
        assertThat(isCommentPredicate.apply("#Xyz"), is(true));
    }
}
