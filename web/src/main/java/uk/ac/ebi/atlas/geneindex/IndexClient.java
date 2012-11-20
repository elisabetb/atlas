package uk.ac.ebi.atlas.geneindex;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
@Scope("prototype")
public class IndexClient {

    private static final String JSON_PATH_EXPRESSION = "$.response.docs[*].identifier";

    private static final String SOLR_REST_QUERY_TEMPLATE = "select?q={query} " +
                                                "AND species{organism}&start=0&rows=100000&fl=identifier&wt=json";

    private RestTemplate restTemplate;

    private String serverURL;

    @Inject
    public IndexClient(RestTemplate restTemplate, GenePropertyQueryBuilder queryBuilder) {
        this.restTemplate = restTemplate;
        this.queryBuilder = queryBuilder;
    }

    private GenePropertyQueryBuilder queryBuilder;

    @Value("#{configuration['index.server.url']}")
    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    protected String findGeneIdJson(String searchText, String organism) {

        String geneProperty = queryBuilder.buildQueryString(searchText);
        String organismQuery = ":\"" + organism.toLowerCase() + "\"";

        String object = restTemplate.getForObject(serverURL + SOLR_REST_QUERY_TEMPLATE, String.class, geneProperty, organismQuery);

        return object;
    }

    public List<String> findGeneIds(String searchText, String organism) {
        String jsonString = findGeneIdJson(searchText,  organism);
        return extractGeneIds(jsonString);
    }

    protected List<String> extractGeneIds(String jsonString){
        return JsonPath.read(jsonString, JSON_PATH_EXPRESSION);
    }

}
