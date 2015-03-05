/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataverse.apitester;

import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Convenience class for removing test data that was created in BatchImportTest
 * (data can be left around in that test by setting cleanup=false)
 * 
 * @author ellenk
 */
public class CleanupTest {
    // The apiToken is just a place holder - replace it with a current one
    private static String apiToken="373bb841-b48a-4ef9-a57a-0330f12b26e3";
    private static String vm6Token ="6c3f9b03-cc72-4006-9c6c-5a72a7a3110c";
    private static String testUserName= "TomTester"; 
    private static String alias = "testBatch";
    private static File rootDir = new File("/Users/ellenk/src/dataverse-apitester");
    private static String local = "http://localhost:8080";
    private static String vm6 = "http://dvn-vm6.hmdc.harvard.edu:8080";
    // Sometimes we want the imported data to stick around so we can look at it
    // in the UI.  To do this, set cleanup=false<contact email="&lt;a href= &quot;mailto:IFPRI-Data@cgiar.org&quot; >
    private static boolean cleanup = true;
    public CleanupTest() {
        RestAssured.baseURI = local;
    }
    
  
    private static void deleteDataverse(String alias, String token) {
        //  delete all contents of dataverse
        Response response = given().when().get("/api/dataverses/" + alias + "/contents?key=" + token);
            System.out.println("response: " + response.asString());
    JsonPath jsonPath = new JsonPath(response.asString());
        List<HashMap> datasets = jsonPath.getList("data");
        if (datasets != null) {
            for (HashMap dataset : datasets) {
                Integer id = (Integer) dataset.get("id");
                if (dataset.get("type").equals("dataset")) {
                    response = given().param("key", token).when().delete("/api/datasets/" + id + "/destroy");
                   System.out.println(response.asString());
                    Assert.assertEquals(200, response.getStatusCode());
                }
            }
        }
        // Delete the dataverse
        response = given().when().delete("/api/dataverses/" + alias + "?key=" + token);
        System.out.println("response: " + response.asString());
     
    }
    
   
    // To re-index:
    // 1. clear current index contents:
    // curl http://localhost:8983/solr/update/json?commit=true -H "Content-type: application/json" -X POST -d "{\"delete\": { \"query\":\"*:*\"}}"
    // 2. create new index:
    // http://localhost:8080/api/index
    
   // To delete a dataset: curl -X DELETE "http://localhost:8080/api/datasets/57?key=d76c2f42-e674-4350-87c7-159a331d1136"
    
    
    
  

    @Test
    public void cleanup() {
        // get the a list of all dataverses, and remove them
        Response response = given().when().get("/api/dataverses/:root/contents?key=" + apiToken);
        JsonPath jsonPath = new JsonPath(response.asString());
        List<HashMap> contentMap = jsonPath.getList("data");
        if (contentMap != null) {
            for (HashMap content : contentMap) {
                Integer id = (Integer) content.get("id");
                if (content.get("type").equals("dataverse")) {
                    deleteDataverse(Integer.toString(id), apiToken);
                }
            }
        }

        response = given().delete("/api/s/authenticatedUsers/" + testUserName + "/");
        System.out.println("Delete user response: " + response.asString());

    }

   @Test
    public void cleanupVm6() {
         RestAssured.baseURI = vm6;
        // get the a list of all dataverses, and remove them
        Response response = given().when().get("/api/dataverses/:root/contents?key=" + vm6Token);
        JsonPath jsonPath = new JsonPath(response.asString());
        List<HashMap> contentMap = jsonPath.getList("data");
        if (contentMap != null) {
            for (HashMap content : contentMap) {
                Integer id = (Integer) content.get("id");
                if (content.get("type").equals("dataverse")) {
                    deleteDataverse(Integer.toString(id), vm6Token);
                }
            }
        }

       

    }

    // nice examples at http://www.hascode.com/2011/10/testing-restful-web-services-made-easy-using-the-rest-assured-framework/
    // and https://bitbucket.org/hascode/rest-assured-samples
    // and https://code.google.com/p/rest-assured/wiki/Usage
    @Test
    public void testStatusNotFound() {
        expect().statusCode(404).when().get("/doesnotexist");
    }
 
     
}
