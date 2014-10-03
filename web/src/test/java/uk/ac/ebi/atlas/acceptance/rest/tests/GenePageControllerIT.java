package uk.ac.ebi.atlas.acceptance.rest.tests;

import com.jayway.restassured.response.Response;
import org.junit.Test;
import uk.ac.ebi.atlas.acceptance.rest.fixtures.RestAssuredFixture;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.endsWith;

public class GenePageControllerIT extends RestAssuredFixture {

    @Test
    public void mgiRedirects(){
        String from = "/genes/MGI:88495";
        String to = "/genes/ENSMUSG00000063889";

        Response response = given().redirects().follow(false).get(from);

        response.then().assertThat().statusCode(302);
        response.then().assertThat().header("Location", endsWith(to));
    }

}
