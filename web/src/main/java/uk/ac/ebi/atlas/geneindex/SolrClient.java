/*
 * Copyright 2008-2012 Microarray Informatics Team, EMBL-European Bioinformatics Institute
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

package uk.ac.ebi.atlas.geneindex;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
@Scope("prototype")
public class SolrClient {
    private static final Logger LOGGER = Logger.getLogger(SolrClient.class);

    private static final String JSON_PATH_GENE_IDENTIFIERS = "$.response.docs[*].identifier";

    private static final String SOLR_SEARCH_QUERY_TEMPLATE = "select?q={conf}{query} " +
            "AND species:\"{organism}\"&start=0&rows=100000&fl=identifier&wt=json";

    private static final String SOLR_AUTOCOMPLETE_GENENAMES_TEMPLATE = "suggest_genename?q=autocomplete_genename:\"{0}\" AND species:\"{1}\"&wt=json&omitHeader=true&rows=0&json.nl=arrarr";

    private static final String SOLR_AUTOCOMPLETE_PROPERTIES_TEMPLATE = "suggest_properties?q=\"{0}\" AND species:\"{1}\"&wt=json&omitHeader=true&rows=0&json.nl=arrarr";

    private RestTemplate restTemplate;

    private String serverURL;


    @Inject
    public SolrClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("#{configuration['index.server.url']}")
    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public Set<String> findGeneIds(String searchText, String species) {    

        String geneQuery = buildQueryAllTextString(searchText);
        String jsonString = getJsonResponse(SOLR_SEARCH_QUERY_TEMPLATE, "{!lucene q.op=OR df=alltext}", geneQuery, species);

        List<String> geneIds = JsonPath.read(jsonString, JSON_PATH_GENE_IDENTIFIERS);

        return toUppercase(geneIds);

    }

    public List<String> findGeneNameSuggestions(String geneName, String species){

        String jsonString = getJsonResponse(SOLR_AUTOCOMPLETE_GENENAMES_TEMPLATE, customEscape(geneName), species);

        List<String> collations = extractCollations(jsonString);

        return removeSpeciesTerms(species, collations);

    }

    List<String> removeSpeciesTerms(String species, List<String> collations) {
        Set<String> speciesTerms = Sets.newHashSet(species.toLowerCase().split(" "));

        for (Iterator iterator = collations.iterator(); iterator.hasNext(); ){
            if(speciesTerms.contains(iterator.next())){
                iterator.remove();
            }
        }
        return collations;
    }


    JsonElement extractSuggestionsElement(String jsonString) {
        JsonObject spellCheckObject = new JsonParser().parse(jsonString).getAsJsonObject().getAsJsonObject("spellcheck");
        if (spellCheckObject != null) {
            return spellCheckObject.get("suggestions");
        }
        return null;
    }

    List<String> extractCollations(String jsonString) {
        List<String> suggestionStrings = new ArrayList<>();

        JsonElement suggestionsElement = extractSuggestionsElement(jsonString);

        if (suggestionsElement != null && suggestionsElement.isJsonArray()) {

            JsonArray suggestionElements = suggestionsElement.getAsJsonArray();

            for (JsonElement suggestionElement : suggestionElements) {
                JsonArray suggestionEntry = suggestionElement.getAsJsonArray();
                if ("collation".equals(suggestionEntry.get(0).getAsString())) {
                    String collation = suggestionEntry.get(1).getAsString();
                    suggestionStrings.add(extractSuggestion(collation));
                }
            }
        }
        return suggestionStrings;
    }

    String extractSuggestion(String collation) {
        String normalizedCollation = StringUtils.replace(collation, "autocomplete_genename:", "");
        return StringUtils.split(normalizedCollation, "\"")[0];
    }

    public List<String> findGenePropertySuggestions(String multiTermToken, String species){

        String jsonString = getJsonResponse(SOLR_AUTOCOMPLETE_PROPERTIES_TEMPLATE, customEscape(multiTermToken), species);

        return extractCollations(jsonString);
    }

    String getJsonResponse(String restQueryTemplate, String... arguments) {
        if(StringUtils.isBlank(arguments[0])){
            return "";
        }

        try {

            String jsonResponse = restTemplate.getForObject(serverURL + restQueryTemplate, String.class, arguments);

            LOGGER.debug("<getJsonResponse> response size (characters) = " + jsonResponse.length() + ", arguments: " + Arrays.toString(arguments));

            return jsonResponse;

        } catch (HttpClientErrorException e) {
            if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
                throw new InvalidQueryException("Invalid gene query, please check syntax! ", e);
            }
            LOGGER.error("<getJsonResponse> error connecting to the solr service: " + serverURL, e);
            throw e;
        } catch (RestClientException e) {
            LOGGER.error("<getJsonResponse> error connecting to the solr service: " + serverURL, e);
            throw e;
        }
    }

    Set<String> toUppercase(List<String> geneIds) {
        Set<String> result = new HashSet<>();
        for (String geneId : geneIds) {
            result.add(geneId.toUpperCase());
        }
        return result;
    }

    String buildQueryAllTextString(String searchText) {
        StringBuilder stringBuilder = new StringBuilder("(alltext:");
        stringBuilder.append(customEscape(searchText));
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    private String customEscape(String searchText) {
        return searchText.replace(":", "\\:");
    }
}
