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
    private static String globalId1;
    private static String globalId2;
    private static String globalId3;
    final String expectedSwordSpecVersion = "2.0";
    static String dvAlias1 = "swordtestdv1";
    static String dvAlias2 = "swordtestdv2";
    static String dvAlias3 = "swordtestdv3";
    static String datasetSwordIdUrl;

    @BeforeClass
    public static void createUser() throws IOException {
        File userJson = new File("src/test/java/org/dataverse/apitester/sword/data/sworduser.json");
        String jsonStr = new Scanner(userJson).useDelimiter("\\Z").next();
        Response response = given().body(jsonStr).contentType(ContentType.JSON).when().post("/api/builtin-users?key=burrito&password=" + password);
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
        assertEquals(response.getStatusCode(), 403);
    }

    @Test
    public void testSwordCreateDataset() throws IOException {
        // create dataverse
        JsonArrayBuilder contactArrayBuilder = Json.createArrayBuilder();
        contactArrayBuilder.add(Json.createObjectBuilder().add("contactEmail", "tom@mailinator.com"));
        JsonArrayBuilder subjectArrayBuilder = Json.createArrayBuilder();
        subjectArrayBuilder.add("Other");
        JsonObject dvData = Json.createObjectBuilder().add("alias", dvAlias1).add("name", dvAlias1).add("dataverseContacts", contactArrayBuilder).add("dataverseSubjects", subjectArrayBuilder).build();
        Response createDataverseResponse = given().body(dvData.toString()).contentType(ContentType.JSON).when().post("/api/dataverses/:root?key=" + apiToken);
        assertEquals(201, createDataverseResponse.getStatusCode());
        JsonPath jsonPath = JsonPath.from(createDataverseResponse.body().asString());
        File datasetXml = new File("src/test/java/org/dataverse/apitester/sword/data/dataset-trees1.xml");
        String xmlIn = Files.toString(datasetXml, StandardCharsets.UTF_8);
        Response createDatasetResponse = given()
                .auth().basic(apiToken, EMPTY_STRING)
                .body(xmlIn)
                .contentType("application/atom+xml")
                .post("/dvn/api/data-deposit/v1.1/swordv2/collection/dataverse/" + dvAlias1);
        String xml = createDatasetResponse.body().asString();
//        System.out.println("xml: " + xml);
        assertEquals(201, createDatasetResponse.getStatusCode());
        datasetSwordIdUrl = from(xml).get("entry.id");
        /**
         * @todo stop assuming the last 22 characters are the doi/globalId
         */
        globalId1 = datasetSwordIdUrl.substring(datasetSwordIdUrl.length() - 22);
    }

    @Test
    public void testSwordEditDataset() throws IOException {
        // create dataverse
        JsonArrayBuilder contactArrayBuilder = Json.createArrayBuilder();
        contactArrayBuilder.add(Json.createObjectBuilder().add("contactEmail", "tom@mailinator.com"));
        JsonArrayBuilder subjectArrayBuilder = Json.createArrayBuilder();
        subjectArrayBuilder.add("Other");
        JsonObject dvData = Json.createObjectBuilder().add("alias", dvAlias2).add("name", dvAlias2).add("dataverseContacts", contactArrayBuilder).add("dataverseSubjects", subjectArrayBuilder).build();
        Response createDataverseResponse = given().body(dvData.toString()).contentType(ContentType.JSON).when().post("/api/dataverses/:root?key=" + apiToken);
//        Response createDataverseResponse = given().body(dvData.toString()).contentType(ContentType.JSON).when().post("/api/dvs/:root?key=" + apiToken);
        assertEquals(201, createDataverseResponse.getStatusCode());
        JsonPath jsonPath = JsonPath.from(createDataverseResponse.body().asString());
        File datasetXml = new File("src/test/java/org/dataverse/apitester/sword/data/dataset-trees1.xml");
        String xmlIn = Files.toString(datasetXml, StandardCharsets.UTF_8);
        Response createDatasetResponse = given()
                .auth().basic(apiToken, EMPTY_STRING)
                .body(xmlIn)
                .contentType("application/atom+xml")
                .post("/dvn/api/data-deposit/v1.1/swordv2/collection/dataverse/" + dvAlias2);
        String xml = createDatasetResponse.body().asString();
//        System.out.println("xml: " + xml);
        assertEquals(201, createDatasetResponse.getStatusCode());
        datasetSwordIdUrl = from(xml).get("entry.id");
        /**
         * @todo stop assuming the last 22 characters are the doi/globalId
         */
        globalId2 = datasetSwordIdUrl.substring(datasetSwordIdUrl.length() - 22);
        File datasetXml2 = new File("src/test/java/org/dataverse/apitester/sword/data/dataset-trees1-edit1.xml");
        String xmlIn2 = Files.toString(datasetXml2, StandardCharsets.UTF_8);
        Response editDatasetResponse = given()
                .auth().basic(apiToken, EMPTY_STRING)
                .body(xmlIn2)
                .contentType("application/atom+xml")
                .put("/dvn/api/data-deposit/v1.1/swordv2/edit/study/" + globalId2);
//        System.out.println("edit dataset response: " + editDatasetResponse.body().asString());
        assertEquals(200, editDatasetResponse.getStatusCode());

    }

    @Test
    public void testSwordDeleteFiles() throws IOException {
        // create dataverse
        JsonArrayBuilder contactArrayBuilder = Json.createArrayBuilder();
        contactArrayBuilder.add(Json.createObjectBuilder().add("contactEmail", "tom@mailinator.com"));
        JsonArrayBuilder subjectArrayBuilder = Json.createArrayBuilder();
        subjectArrayBuilder.add("Other");
        JsonObject dvData = Json.createObjectBuilder().add("alias", dvAlias3).add("name", dvAlias3).add("dataverseContacts", contactArrayBuilder).add("dataverseSubjects", subjectArrayBuilder).build();

        Response createDataverseResponse = given().body(dvData.toString()).contentType(ContentType.JSON).when().post("/api/dataverses/:root?key=" + apiToken);
        assertEquals(201, createDataverseResponse.getStatusCode());
        JsonPath jsonPath = JsonPath.from(createDataverseResponse.body().asString());
        File datasetXml = new File("src/test/java/org/dataverse/apitester/sword/data/dataset-trees1.xml");
        String xmlIn = Files.toString(datasetXml, StandardCharsets.UTF_8);

        Response createDatasetResponse = given()
                .auth().basic(apiToken, EMPTY_STRING)
                .body(xmlIn)
                .contentType("application/atom+xml")
                .post("/dvn/api/data-deposit/v1.1/swordv2/collection/dataverse/" + dvAlias3);
        String xml = createDatasetResponse.body().asString();
        assertEquals(201, createDatasetResponse.getStatusCode());
        datasetSwordIdUrl = from(xml).get("entry.id");
        /**
         * @todo stop assuming the last 22 characters are the doi/globalId
         */
        globalId3 = datasetSwordIdUrl.substring(datasetSwordIdUrl.length() - 22);
    }

    @AfterClass
    public static void cleanUp() {

        boolean cleanup = true;
        if (!cleanup) {
            return;
        }

        Response deleteDataset1Response = deleteDataset(globalId1);
        assertEquals(204, deleteDataset1Response.getStatusCode());
        Response deleteDataverse1Response = deleteDataverse(dvAlias1);
        assertEquals(200, deleteDataverse1Response.getStatusCode());

        Response deleteDataset2Response = deleteDataset(globalId2);
        assertEquals(204, deleteDataset2Response.getStatusCode());
        Response deleteDataverse2Response = deleteDataverse(dvAlias2);
        assertEquals(200, deleteDataverse2Response.getStatusCode());

        Response deleteDataset3Response = deleteDataset(globalId3);
        assertEquals(204, deleteDataset3Response.getStatusCode());
        Response deleteDataverse3Response = deleteDataverse(dvAlias3);
        assertEquals(200, deleteDataverse3Response.getStatusCode());

        Response deleteUserResponse = given().delete("/api/admin/authenticatedUsers/" + username + "/");
        assertEquals(200, deleteUserResponse.getStatusCode());
    }

    private static Response deleteDataset(String globalId) {
        return given()
                .auth().basic(apiToken, EMPTY_STRING)
                .relaxedHTTPSValidation()
                .delete("/dvn/api/data-deposit/v1.1/swordv2/edit/study/" + globalId);
    }

    private static Response deleteDataverse(String dvAlias) {
        return given().when().delete("/api/dataverses/" + dvAlias + "?key=" + apiToken);
    }
}
