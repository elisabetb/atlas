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

package uk.ac.ebi.atlas.transcript;

import com.google.common.base.Splitter;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.atlas.model.baseline.TranscriptProfile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TranscriptProfileRowMapper implements RowMapper<TranscriptProfile> {

    @Override
    public TranscriptProfile mapRow(ResultSet resultSet, int i) throws SQLException {
        String geneId = resultSet.getString("GENE_IDENTIFIER");
        String transcriptId = resultSet.getString("TRANSCRIPT_IDENTIFIER");
        String csvTranscriptExpressions = resultSet.getString("TRANSCRIPT_EXPRESSIONS");
        List<String> transcriptExpressions = Splitter.on(",").splitToList(csvTranscriptExpressions);
        return new TranscriptProfile(geneId, transcriptId, transcriptExpressions);
    }
}