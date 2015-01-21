package org.dataverse.apitester.sword;

import com.google.common.io.Files;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import static com.jayway.restassured.path.xml.XmlPath.from;
import com.jayway.restassured.response.Response;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import junit.framework.Assert;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SwordTest {

    static String username;
    static String apiToken;
    static String password = "foo";
    static String EMPTY_STRING = "";
    private static String globalId;
    final String expectedSwordSpecVersion = "2.0";
    static String dvAlias = "swordtestdv";
    static String datasetSwordIdUrl;

    @BeforeClass
    public static void createUser() throws IOException {
        File userJson = new File("src/test/java/org/dataverse/apitester/sword/data/sworduser.json");
        String jsonStr = new Scanner(userJson).useDelimiter("\\Z").next();
        Response response = given().body(jsonStr).contentType(ContentType.JSON).when().post("/api/users?key=burrito&password=" + password);
        Assert.assertEquals(200, response.getStatusCode());
        JsonPath jsonPath = JsonPath.from(response.body().asString());
        username = jsonPath.get("data.user.userName");
        apiToken = jsonPath.get("data.apiToken");
    }

    @Test
    public void testSwordServiceDocumentApiToken() {
        expect().statusCode(200)
                .body(
                        "service.version", equalTo(expectedSwordSpecVersion)
                )
                .given().auth().basic(apiToken, EMPTY_STRING)
                .when().get("/dvn/api/data-deposit/v1.1/swordv2/service-document");
    }

    @Test
    public void testSwordServiceDocumentPassword() {
        Response response = given()
                .auth().basic(username, password)
                .when().get("/dvn/api/data-deposit/v1.1/swordv2/service-document");
        assertEquals(response.getStatusCode(), 200);
        String xml = response.body().asString();
        String swordSpecVersion = from(xml).get("service.version");
        assertEquals(swordSpecVersion, "2.0");
    }

    @Test
    public void testSwordCreateDataset() throws IOException {
        // create dataverse
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
        arrBuilder.add(Json.createObjectBuilder().add("contactEmail", "tom@mailinator.com"));
        JsonObject dvData = Json.createObjectBuilder().add("alias", dvAlias).add("name", dvAlias).add("dataverseContacts", arrBuilder).build();
        Response createDataverseResponse = given().body(dvData.toString()).contentType(ContentType.JSON).when().post("/api/dvs/:root?key=" + apiToken);
        JsonPath jsonPath = JsonPath.from(createDataverseResponse.body().asString());
        File datasetXml = new File("src/test/java/org/dataverse/apitester/sword/data/dataset-trees1.xml");
        String xmlIn = Files.toString(datasetXml, StandardCharsets.UTF_8);
        Response createDatasetResponse = given()
                .auth().basic(apiToken, EMPTY_STRING)
                .body(xmlIn)
                .contentType("application/atom+xml")
                .post("/dvn/api/data-deposit/v1.1/swordv2/collection/dataverse/" + dvAlias);
        String xml = createDatasetResponse.body().asString();
        datasetSwordIdUrl = from(xml).get("entry.id");
        /**
         * @todo stop assuming the last 22 characters are the doi/globalId
         */
        globalId = datasetSwordIdUrl.substring(datasetSwordIdUrl.length() - 22);
    }

    @AfterClass
    public static void cleanUp() {
        // delete dataset
        Response deleteDatasetResponse = given()
                .auth().basic(apiToken, EMPTY_STRING)
                .relaxedHTTPSValidation()
                .delete("/dvn/api/data-deposit/v1.1/swordv2/edit/study/" + globalId);
        assertEquals(204, deleteDatasetResponse.getStatusCode());

        // delete dataverse
        Response deleteDataverseResponse = given().when().delete("/api/dvs/" + dvAlias + "?key=" + apiToken);
        assertEquals(200, deleteDataverseResponse.getStatusCode());

        // delete user
        Response deleteUserResponse = given().delete("/api/s/authenticatedUsers/" + username + "/");
        assertEquals(200, deleteUserResponse.getStatusCode());
    }
}
