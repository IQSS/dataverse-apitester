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
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import javax.json.*;

/**
 *
 * @author ellenk
 */
public class BatchImportTest {
    private static String apiToken;
    private static String testUserName; 
    private static String alias = "testBatch";
    private static File rootDir = new File("/Users/ellenk/src/dataverse-apitester");
    // Sometimes we want the imported data to stick around so we can look at it
    // in the UI.  To do this, set cleanup=false
    private static boolean cleanup = false;
    public BatchImportTest() {
        RestAssured.baseURI = "http://localhost:8080";
    }
    
    private static void createDataverse() {
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        arrBuilder.add(Json.createObjectBuilder().add("contactEmail", "tom@mailinator.com"));
        JsonObject dvData = Json.createObjectBuilder().add("alias", alias).add("name",alias).add("dataverseContacts", arrBuilder).build();
       
        
        Response response = given().body(dvData.toString()).contentType(ContentType.JSON).when().post("/api/dvs/:root?key="+apiToken);
        System.out.println("response: "+response.asString());
        Assert.assertEquals(201, response.getStatusCode());
       
    }
    private static void deleteDataverse() {
        //  delete all contents of dataverse
        Response response = given().when().get("/api/dvs/" + alias + "/contents?key=" + apiToken);
        JsonPath jsonPath = new JsonPath(response.asString());
        List<HashMap> datasets = jsonPath.getList("data");
        for (HashMap dataset : datasets) {  
            Integer id = (Integer) dataset.get("id");
            response = given().param("key", apiToken).when().delete("/api/datasets/"+id+"/destroy");
            Assert.assertEquals(200,response.getStatusCode());
            System.out.println(response.asString());     
        }
        // Delete the dataverse
        response = given().when().delete("/api/dvs/" + alias + "?key=" + apiToken);
        System.out.println("response: " + response.asString());
        Assert.assertEquals(200, response.getStatusCode());

    }
    
    @BeforeClass
    public static void setUpClass() throws IOException{
       // Create a new user and get the API Key.   
       File userJson = new File("src/test/java/org/dataverse/apitester/data/test-user.json"); 
       String jsonStr = new Scanner(userJson).useDelimiter("\\Z").next();
       Response response = given().body(jsonStr).contentType(ContentType.JSON).when().post("/api/users/tomtester/burrito");
       
       System.out.println("response: "+response.asString());
       Assert.assertEquals(200, response.getStatusCode());
       
        JsonPath jsonPath = new JsonPath(response.asString());
       apiToken = jsonPath.get("data.apiToken");
       testUserName = jsonPath.get("data.user.userName");
       Integer id = jsonPath.get("data.user.id");
       
       // make this user a superuser
       response = given().post("/api/s/superuser/"+testUserName+"/");
       System.out.println("Toggle user response: "+response.asString());
       Assert.assertEquals(200,response.getStatusCode());
       
       // use API key to create a dataverse to import the datasets into
       createDataverse();
         
  }
    // To re-index:
    // 1. clear current index contents:
    // curl http://localhost:8983/solr/update/json?commit=true -H "Content-type: application/json" -X POST -d "{\"delete\": { \"query\":\"*:*\"}}"
    // 2. create new index:
    // http://localhost:8080/api/index
    
   // To delete a dataset: curl -X DELETE "http://localhost:8080/api/datasets/57?key=d76c2f42-e674-4350-87c7-159a331d1136"
    
    @Test
    public void testDemoDDI() {
        processSingleFile( "ds_5.xml", "migrate");
    }
    
    @Test
    public void testMigrateMultipleVersions() {
        String dirName = "version_test";
         File path = new File(rootDir,"src/test/java/org/dataverse/apitester/data/ddi/"+dirName);
       String parentDv = alias;
       Response response = given().param("path", path.getAbsolutePath()).param("key", apiToken).when().get("/api/batch/migrate/"+parentDv);
       System.out.println("response: "+response.asString());
       Assert.assertEquals(200, response.getStatusCode());
       
       JsonPath jsonPath = new JsonPath(response.asString());
       Integer createdId = jsonPath.get("data[0].id");
       
       // Now get the created dataset from the api 
       response = given().param("key", apiToken).when().get("/api/datasets/"+createdId);
       Assert.assertEquals(200,response.getStatusCode());
       System.out.println(response.asString());
       
       
    
      
    
    }
    
    @Test
    public void testSampleDDIFull() {
        processSingleFile("samplestudyddifull.xml", "migrate");
    }
    
    @Test
    public void testImportNewSampleDDIFull() {
          processSingleFile("samplestudyddifull_noDOI.xml", "import");
    }
    
    private   void processSingleFile(String fileName, String apiCommand) {
       
       File path = new File(rootDir,"src/test/java/org/dataverse/apitester/data/ddi/"+fileName);
       String parentDv = alias;
       Response response = given().param("path", path.getAbsolutePath()).param("key", apiToken).when().get("/api/batch/"+apiCommand+"/"+parentDv);
       System.out.println("response: "+response.asString());
       Assert.assertEquals(200, response.getStatusCode());
       
       JsonPath jsonPath = new JsonPath(response.asString());
       Integer createdId = jsonPath.get("data[0].id");
       
       // Now get the created dataset from the api 
       response = given().param("key", apiToken).when().get("/api/datasets/"+createdId);
       Assert.assertEquals(200,response.getStatusCode());
       System.out.println(response.asString());
       
       // Now delete the created set: 
       if (cleanup) {
           response = given().param("key", apiToken).when().delete("/api/datasets/"+createdId);
            Assert.assertEquals(200,response.getStatusCode());
            System.out.println(response.toString());
       }
      
    }

    @AfterClass
    public static void tearDownClass() {
        if (cleanup) {
            deleteDataverse();
            Response response = given().delete("/api/s/authenticatedUsers/" + testUserName + "/");
            System.out.println("Delete user response: " + response.asString());
            Assert.assertEquals(200, response.getStatusCode());
        }
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
