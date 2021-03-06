/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataverse.apitester;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import java.io.File;
import java.io.IOException;
import static java.nio.file.Files.readAllBytes;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private static File rootDir = new File(System.getProperty("buildDirectory"));
    // Sometimes we want the imported data to stick around so we can look at it
    // in the UI.  To do this, set cleanup=false
    private static boolean cleanup = false;
   
    
    private static void createDataverse() {
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        JsonArrayBuilder arrBuilder2 = Json.createArrayBuilder();
        arrBuilder.add(Json.createObjectBuilder().add("contactEmail", "tom@mailinator.com"));
        arrBuilder2.add( "Other");
        JsonObject dvData = Json.createObjectBuilder().add("dataverseSubjects",arrBuilder2).add("alias", alias).add("name",alias).add("dataverseContacts", arrBuilder).build();       
        Response response = given().body(dvData.toString()).contentType(ContentType.JSON).when().post("/api/dataverses/:root?key="+apiToken);
        System.out.println("response: "+response.asString());
        Assert.assertEquals(201, response.getStatusCode());
       
    }
    private static void deleteDataverse() {
        //  delete all contents of dataverse
        Response response = given().when().get("/api/dataverses/" + alias + "/contents?key=" + apiToken);
        JsonPath jsonPath = new JsonPath(response.asString());
        List<HashMap> datasets = jsonPath.getList("data");
        for (HashMap dataset : datasets) {  
            Integer id = (Integer) dataset.get("id");
            response = given().param("key", apiToken).when().delete("/api/datasets/"+id+"/destroy");
            Assert.assertEquals(200,response.getStatusCode());
            System.out.println(response.asString());     
        }
        // Delete the dataverse
        response = given().when().delete("/api/dataverses/" + alias + "?key=" + apiToken);
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
    public void testDemoDDI() throws IOException {
        migrateSingleFile( "ds_5.xml");
    }
    
    @Test
    public void testCustomFields() throws IOException {
        migrateSingleFile("ddi_custom_fields.xml");
    }
    
   @Test
    public void testParseDoi() {
        migrateSingleFile( "ds_5_doi.xml");
    }
  //  This is commented out because the nightly build will make a call to a remote server,
  //  which won't have access to the test data in the source tree.
  //  Uncomment this to test locally.
    @Test 
    public void testMigrateMultipleVersions() {
        File path = new File(rootDir, "src/test/java/org/dataverse/apitester/data/ddi/parentDir2");
        String parentDv = alias;
        Response response = given().param("path", path.getAbsolutePath()).param("key", apiToken).param("dv", parentDv).when().get("/api/batch/migrate");
        System.out.println("response: " + response.asString());
        Assert.assertEquals(200, response.getStatusCode());

       
    }
    /**
     * This test migrates files that are organized within subdirectories
     * that match the alias of the dataverse that the files are imported into.
     */
    @Test
    public void testParentDirectory() {
        File path = new File(rootDir, "src/test/java/org/dataverse/apitester/data/ddi/parentDir1");
         Response response = given().param("path", path.getAbsolutePath()).param("key", apiToken).when().get("/api/batch/migrate");
        System.out.println("response: " + response.asString());
        Assert.assertEquals(202, response.getStatusCode());

        JsonPath jsonPath = new JsonPath(response.asString());
      //  Integer createdId = jsonPath.get("data[0][0].id");

        
    }
    
    @Test
    public void testZimbabwe() {
        File path = new File(rootDir, "src/test/java/org/dataverse/apitester/data/ddi/parentDir3");
         Response response = given().param("path", path.getAbsolutePath()).param("key", apiToken).when().get("/api/batch/import");
        System.out.println("response: " + response.asString());
        Assert.assertEquals(202, response.getStatusCode());
        // We know that this file contains invalid data, so check that it returns a validation message, rather than a created id
        JsonPath jsonPath = new JsonPath(response.asString());
    //       ArrayList dataList = (ArrayList)jsonPath.get("data[0]");
  //  Assert.assertEquals(35, dataList.size());
        
    }
    
    @Test
    public void testSampleDDIFull() throws IOException {
        importSingleFile("samplestudyddifull.xml");
    }
    
    @Test
    public void testImportNewSampleDDIFull() throws IOException {
        importSingleFile("samplestudyddifull_noDOI.xml");
       
    }
    
    @Test 
    public void testKeywordSubject() throws IOException {
        migrateSingleFile("keywordtest.xml");
    }
    
    private void importSingleFile(String fileName) throws IOException {
        
        String parentDv = alias;
        String xmlIn = new String(readAllBytes(Paths.get("src/test/java/org/dataverse/apitester/data/ddi/parentDir2/" + fileName)));
        Response response = given()
                .body(xmlIn)
                .contentType("application/atom+xml")
                .post("/api/batch/import/?dv=" + parentDv+"&key="+apiToken);
        System.out.println(response.asString());
        Assert.assertEquals(200, response.getStatusCode());
        JsonPath jsonPath = new JsonPath(response.asString());
        Integer createdId = jsonPath.get("data.id");
       
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
    
    private   void migrateSingleFile(String fileName) {
       String apiCommand="migrate";
       File path = new File(rootDir,"src/test/java/org/dataverse/apitester/data/ddi/parentDir2/"+fileName);
       String parentDv = alias;
       Response response = given().param("path", path.getAbsolutePath()).param("key", apiToken).param("dv",parentDv).when().get("/api/batch/"+apiCommand);
       System.out.println("response: "+response.asString());
       Assert.assertEquals(202, response.getStatusCode());
      
      
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
