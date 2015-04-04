package org.dataverse.apitester;

import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MainTest {

    public MainTest() {
        String specifiedUri = System.getProperty("apitester.baseuri");
        if (specifiedUri != null) {
            RestAssured.baseURI = specifiedUri;
        } else {
            RestAssured.baseURI = "http://localhost:8080";
        }
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // nice examples at http://www.hascode.com/2011/10/testing-restful-web-services-made-easy-using-the-rest-assured-framework/
    // and https://bitbucket.org/hascode/rest-assured-samples
    // and https://code.google.com/p/rest-assured/wiki/Usage
    @Test
    public void testStatusNotFound() {
        expect().statusCode(404).when().get("/doesnotexist");
    }

    @Test
    public void testSearchWithNoQueryParameter() {
        Response response = get("/api/search");
        assertEquals(401, response.getStatusCode());
        String json = response.asString();
        JsonPath jsonPath = new JsonPath(json);
        assertEquals(jsonPath.get("status"), "ERROR");
        assertEquals(jsonPath.get("message"), "Please provide a key query parameter (?key=XXX)");
    }

}
