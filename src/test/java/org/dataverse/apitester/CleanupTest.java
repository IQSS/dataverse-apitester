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
 * Convenience class for removing test data that was created in BatchImportTest
 * (data can be left around in that test by setting cleanup=false)
 * @author ellenk
 */
public class CleanupTest {
    private static String apiToken="fee3a52a-8c6f-4c4c-9d92-4cc25f4289bd";
    private static String testUserName= "TomTester"; 
    private static String alias = "testBatch";
    private static File rootDir = new File("/Users/ellenk/src/dataverse-apitester");
    // Sometimes we want the imported data to stick around so we can look at it
    // in the UI.  To do this, set cleanup=false
    private static boolean cleanup = true;
    public CleanupTest() {
        RestAssured.baseURI = "http://localhost:8080";
    }
    
  
    private static void deleteDataverse() {
        //  delete all contents of dataverse
        Response response = given().when().get("/api/dvs/" + alias + "/contents?key=" + apiToken);
        JsonPath jsonPath = new JsonPath(response.asString());
        List<HashMap> datasets = jsonPath.getList("data");
        if (datasets != null) {
            for (HashMap dataset : datasets) {
                Integer id = (Integer) dataset.get("id");
                response = given().param("key", apiToken).when().delete("/api/datasets/" + id + "/destroy");
                Assert.assertEquals(200, response.getStatusCode());
                System.out.println(response.asString());
            }
        }
        // Delete the dataverse
        response = given().when().delete("/api/dvs/" + alias + "?key=" + apiToken);
        System.out.println("response: " + response.asString());
     
    }
    
   
    // To re-index:
    // 1. clear current index contents:
    // curl http://localhost:8983/solr/update/json?commit=true -H "Content-type: application/json" -X POST -d "{\"delete\": { \"query\":\"*:*\"}}"
    // 2. create new index:
    // http://localhost:8080/api/index
    
   // To delete a dataset: curl -X DELETE "http://localhost:8080/api/datasets/57?key=d76c2f42-e674-4350-87c7-159a331d1136"
    
    
    
  

    @Test
    public  void cleanup() {
        
            deleteDataverse();
            Response response = given().delete("/api/s/authenticatedUsers/" + testUserName + "/");
            System.out.println("Delete user response: " + response.asString());
            
    }

   

    // nice examples at http://www.hascode.com/2011/10/testing-restful-web-services-made-easy-using-the-rest-assured-framework/
    // and https://bitbucket.org/hascode/rest-assured-samples
    // and https://code.google.com/p/rest-assured/wiki/Usage
    @Test
    public void testStatusNotFound() {
        expect().statusCode(404).when().get("/doesnotexist");
    }
 
     
}
