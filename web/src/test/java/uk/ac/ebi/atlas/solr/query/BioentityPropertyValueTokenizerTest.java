/*
 * Copyright 2008-2013 Microarray Informatics Team, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the Gene Expression Atlas project, including source code,
 * downloads and documentation, please see:
 *
 * http://gxa.github.com/gxa
 */

package uk.ac.ebi.atlas.solr.query;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uk.ac.ebi.atlas.solr.query.BioentityPropertyValueTokenizer.joinQuotingPhrases;
import static uk.ac.ebi.atlas.solr.query.BioentityPropertyValueTokenizer.splitBySpacePreservingQuotes;

public class BioentityPropertyValueTokenizerTest {

    @Test
    public void split() throws Exception {
        assertThat(splitBySpacePreservingQuotes("").size(), is(0));
        assertThat(splitBySpacePreservingQuotes("test1 test2"), contains("test1", "test2"));
        assertThat(splitBySpacePreservingQuotes("test1 test2\ntest3"), contains("test1", "test2", "test3"));
        assertThat(splitBySpacePreservingQuotes("\"test1 test2\""), contains("\"test1 test2\""));
        assertThat(splitBySpacePreservingQuotes("\"test1\" \"test2\""), contains("\"test1\"", "\"test2\""));
        assertThat(splitBySpacePreservingQuotes("\"mt-at\""), contains("\"mt-at\""));
        assertThat(splitBySpacePreservingQuotes("mt-at6"), contains("mt-at6"));
        assertThat(splitBySpacePreservingQuotes("protein_c"), contains("protein_c"));
        assertThat(splitBySpacePreservingQuotes("GO:0016"), contains("GO:0016"));
        assertThat(splitBySpacePreservingQuotes("hs2affx.1.41.s1_3p_s_at"), contains("hs2affx.1.41.s1_3p_s_at"));
        assertThat(splitBySpacePreservingQuotes("mitochondrial enco"), contains("mitochondrial", "enco"));
        assertThat(splitBySpacePreservingQuotes("GO:0008134 \"p53 binding\""), contains("GO:0008134", "\"p53 binding\""));
        assertThat(splitBySpacePreservingQuotes("ENSG00000131759 \"mRNA splicing, via spliceosome\""), contains("ENSG00000131759", "\"mRNA splicing, via spliceosome\""));
    }

    @Test
    public void splitNull() {
        assertThat(splitBySpacePreservingQuotes(null), is(emptyCollectionOf(String.class)));
    }

    @Test
    public void splitEmptyString() {
        assertThat(splitBySpacePreservingQuotes(""), is(emptyCollectionOf(String.class)));
    }

    @Test
    public void splitStringOfSpaces() {
        assertThat(splitBySpacePreservingQuotes(" "), is(emptyCollectionOf(String.class)));
        assertThat(splitBySpacePreservingQuotes("  "), is(emptyCollectionOf(String.class)));
    }

    @Test
    public void join() {
        assertThat(joinQuotingPhrases(ImmutableList.of("one", "two words", "three")), is("\"one\" \"two words\" \"three\""));
    }

}