/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataverse.apitester;

import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MainTest {

    public MainTest() {
        RestAssured.baseURI = "http://dvn-build.hmdc.harvard.edu";
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
        assertEquals(400, response.getStatusCode());
        String json = response.asString();
        JsonPath jsonPath = new JsonPath(json);
        assertEquals(jsonPath.get("status"), "ERROR");
        assertEquals(jsonPath.get("message"), "q parameter is missing");
    }

    @Test
    public void testSwordServiceDocument() {
        expect().statusCode(200)
                .body(
                        "service.version", equalTo("2.0")
                )
                /**
                 * @todo Can we assume that pete will always be there?
                 */
                .given().auth().basic("pete", "pete")
                .when().get("/dvn/api/data-deposit/v1.1/swordv2/service-document");
    }

}
