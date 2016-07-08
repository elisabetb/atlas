package uk.ac.ebi.atlas.search.analyticsindex.differential;

import org.springframework.core.io.Resource;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import uk.ac.ebi.atlas.web.GeneQuery;
import uk.ac.ebi.atlas.web.OldGeneQuery;

import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import static uk.ac.ebi.atlas.utils.ResourceUtils.readPlainTextResource;

@Named
public abstract class DifferentialAnalyticsDAO {

    protected final RestTemplate restTemplate;

    protected final String solrBaseUrl;
    protected final String differentialGeneFacetsQuery;

    private static final String FQ_TEMPLATE = "&fq=pValue:[* TO {0}]";
    private static final String QUERY_TEMPLATE = "query?q={0}&rows={1,number,#}&omitHeader=true";
    protected static final String DIFFERENTIAL_ONLY = "experimentType:(rnaseq_mrna_differential OR microarray_1colour_mrna_differential OR microarray_2colour_mrna_differential OR microarray_1colour_microrna_differential)";
    protected static final String IDENTIFIER_SEARCH_FIELD = "identifierSearch";
    protected static final String BIOENTITY_IDENTIFIER_FIELD = "bioentityIdentifier";

    protected static final double DEFAULT_P_VALUE = 0.05;

    public DifferentialAnalyticsDAO(RestTemplate restTemplate, String solrBaseUrl, Resource differentialFacetsQueryJSON) {
        this.restTemplate = restTemplate;   // settings of restTemplate in applicationContext.xml
        this.solrBaseUrl = solrBaseUrl;
        this.differentialGeneFacetsQuery = "&json.facet=" + encodeQueryParam(readPlainTextResource(differentialFacetsQueryJSON).replaceAll("\\s+",""));
    }

    protected String buildSolrQuery(GeneQuery geneQuery, String searchField) {
        return geneQuery.isEmpty() ? "" : String.format("%s:(%s)", searchField, geneQuery.asSolr1DNF());
    }

    protected String buildSolrQuery(String searchTerm, String searchField) {
        return searchTerm.isEmpty() ? "" : String.format("%s:(%s)", searchField, searchTerm);
    }

    protected String buildQueryParameters(String q, int rows, double pValue) {
        return MessageFormat.format(QUERY_TEMPLATE, encodeQueryParam(q), rows) + encodeQuery(MessageFormat.format(FQ_TEMPLATE, pValue));
    }

    protected static String encodeQueryParam(String param) {
        try {
            return UriUtils.encodeQueryParam(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new DifferentialAnalyticsDAOException(e);
        }
    }

    protected static String encodeQuery(String s) {
        // doesn't encode =
        try {
            return UriUtils.encodeQuery(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new DifferentialAnalyticsDAOException(e);
        }
    }

    protected String fetchResponseAsString(String url) {
        try {
            return restTemplate.getForObject(new URI(url), String.class);
        } catch (RestClientException | URISyntaxException e) {
            throw new DifferentialAnalyticsDAOException(e);
        }
    }

    protected static class DifferentialAnalyticsDAOException extends RuntimeException {
        public DifferentialAnalyticsDAOException(Exception e) {
            super(e);
        }
    }
}
