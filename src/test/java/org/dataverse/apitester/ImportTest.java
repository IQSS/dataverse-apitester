/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataverse.apitester;

import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImportTest {
    private static String apiToken;
    private static Long testUserId; 
    public ImportTest() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    @BeforeClass
    public static void setUpClass() throws IOException{
       // Create a new user and get the API Key.   
       File userJson = new File("src/test/java/org/dataverse/apitester/data/test-user.json"); 
       String jsonStr = new Scanner(userJson).useDelimiter("\\Z").next();
       Response response = given().body(jsonStr).contentType(ContentType.JSON).when().post("/api/users/tom/burrito");
       String json = response.asString();
     
       System.out.println("json: "+json);
       JsonPath jsonPath = new JsonPath(json);
       apiToken = jsonPath.get("apiToken");
       testUserId = jsonPath.get("data.user.id");
      
        // use API key to create a dataverse to import the datasets into
    
         
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

   
}
