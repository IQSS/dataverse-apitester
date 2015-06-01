package org.dataverse.apitester.sword;

import com.google.common.io.Files;
import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.xml.XmlPath;
import static com.jayway.restassured.path.xml.XmlPath.from;
import com.jayway.restassured.response.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import junit.framework.Assert;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

        boolean enabled = true;
        if (!enabled) {
            return;
        }

        File userJson = new File("src/test/java/org/dataverse/apitester/sword/data/sworduser.json");
        String jsonStr = new Scanner(userJson).useDelimiter("\\Z").next();
        Response response = given().body(jsonStr).contentType(ContentType.JSON).when().post("/api/builtin-users?key=burrito&password=" + password);
        Assert.assertEquals(200, response.getStatusCode());
        JsonPath jsonPath = JsonPath.from(response.body().asString());
        username = jsonPath.get("data.user.userName");
        apiToken = jsonPath.get("data.apiToken");
        Response makeSuperUserResponse = makeSuperuser(username);
//        makeSuperUserResponse.body().prettyPrint();
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

    /**
     * For https://github.com/IQSS/dataverse/issues/2222 but disabled for now
     * because this test requires a way to discover the dataset id based on a
     * DOI. See also https://github.com/IQSS/dataverse/issues/1837
     */
    @Ignore
    @Test
    public void testSwordDeleteFiles2222() throws IOException {
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
        String xmlFromCreate = createDatasetResponse.body().asString();
        assertEquals(201, createDatasetResponse.getStatusCode());
        System.out.println("BEGIN CREATE");
        from(xmlFromCreate).prettyPrint();
        System.out.println("END CREATE");
        datasetSwordIdUrl = from(xmlFromCreate).get("entry.id");
        String datasetEntityId = from(xmlFromCreate).get("entry.datasetEntityId").toString();

        /**
         * @todo stop assuming the last 22 characters are the doi/globalId
         */
        globalId3 = datasetSwordIdUrl.substring(datasetSwordIdUrl.length() - 22);

        Process uploadZipFileProcess = uploadZipFile(globalId3, "file1.zip");
        printCommandOutput(uploadZipFileProcess);

        Response dataset3Statement = getSwordStatement();
        dataset3Statement.body().prettyPrint();

        String xml = dataset3Statement.body().asString();
        // http://www.jayway.com/2013/04/12/whats-new-in-rest-assured-1-8/
        XmlPath xmlPath = new XmlPath(xml);
        String fileUrl = xmlPath.get("feed.entry[0].id").toString();
        String[] parts = fileUrl.split("/");
        String fileId = parts[10];

        Response listFilesFromDraftBeforeDelete = listFilesFromVersionUsingEntityId(datasetEntityId, ":draft");
        listFilesFromDraftBeforeDelete.prettyPrint();

        System.out.println("file1.txt is deleted through SWORD.");
        Response deleteFileResponse = deleteFile(Integer.parseInt(fileId));
        deleteFileResponse.body().prettyPrint();

        Response listFilesFromDraftAfterDelete = listFilesFromVersionUsingEntityId(datasetEntityId, ":draft");
        listFilesFromDraftAfterDelete.prettyPrint();

        Response tryToDeleteInvalidFileIdResponse = deleteFile(Integer.MAX_VALUE);
        System.out.println("code: " + tryToDeleteInvalidFileIdResponse.getStatusCode());
        assertEquals(400, tryToDeleteInvalidFileIdResponse.getStatusCode());
    }

    /**
     * @todo Finish reproducing this bug (and fix it!): Unintuitive behaviors
     * when deleting files via SWORD API -
     * https://github.com/IQSS/dataverse/issues/1784
     */
    @Ignore
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

        if (true) {
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

        Process uploadZipFileProcess = uploadZipFile(globalId3, "file1.zip");
        printCommandOutput(uploadZipFileProcess);

        Response dataset3Statement = getSwordStatement();
//        dataset3Statement.body().prettyPrint();

        String xml = dataset3Statement.body().asString();
        // http://www.jayway.com/2013/04/12/whats-new-in-rest-assured-1-8/
        XmlPath xmlPath = new XmlPath(xml);
        String fileUrl = xmlPath.get("feed.entry[0].id").toString();
        String[] parts = fileUrl.split("/");
        String fileId = parts[10];

        Response publishDataverseResponse = publishDataverse(dvAlias3);
        publishDataverseResponse.body().prettyPrint();

        System.out.println("A unpublished dataset is published with file1.txt");
        Response publishDatasetResponse = publishDataset();
//        publishDatasetResponse.body().prettyPrint();
        System.out.println("statement:");
        getSwordStatement().prettyPrint();

        String file2 = "file2.zip";
        System.out.println("uploading " + file2);
        Process uploadFile2 = uploadZipFile(globalId3, file2);
        printCommandOutput(uploadFile2);
        System.out.println("statement:");
        getSwordStatement().prettyPrint();

        System.out.println("The dataset is published again with file1.txt unchanged, and file2.txt which was just added.");
        Response publishDatasetResponse2 = publishDataset();
//        publishDatasetResponse2.body().prettyPrint();
        System.out.println("statement:");
        getSwordStatement().prettyPrint();

        System.out.println("file1.txt is deleted through SWORD. (file2.txt is not deletable, which may be a separate issue)");
        Response deleteFileResponse = deleteFile(Integer.parseInt(fileId));
//        deleteFileResponse.body().prettyPrint();
        System.out.println("statement:");
        getSwordStatement().prettyPrint();

        System.out.println("expecting to still see file1.txt for version 1 after delete:");
        Response listFilesFromVersion1AfterDelete = listFilesFromVersion(globalId3, "1");
        listFilesFromVersion1AfterDelete.prettyPrint();
        System.out.println("files from latest version");
        Response listFilesFromLatestVersionAfterDelete = listFilesFromVersion(globalId3, ":latest");
        listFilesFromLatestVersionAfterDelete.prettyPrint();
    }

    private Process uploadZipFile(String globalId, String zipfilename) throws IOException {
        File fileObject = new File("src/test/java/org/dataverse/apitester/sword/data/" + zipfilename);
        byte[] bytes = Files.toByteArray(fileObject);
        String mimeType = "application/zip";
        /**
         * curl -u $API_TOKEN: --data-binary @path/to/example.zip -H
         * "Content-Disposition: filename=example.zip" -H "Content-Type:
         * application/zip" -H "Packaging:
         * http://purl.org/net/sword/package/SimpleZip"
         * https://$HOSTNAME/dvn/api/data-deposit/v1.1/swordv2/edit-media/study/doi:TEST/12345
         *
         *
         * curl -s --insecure --data-binary
         *
         * @scripts/search/data/binary/trees.zip -H "Content-Disposition:
         * filename=trees.zip" -H "Content-Type: application/zip" -H "Packaging:
         * http://purl.org/net/sword/package/SimpleZip" -u
         * b6beb656-1d27-4c1e-bd83-7d04b060334a:
         * https://localhost:8181/dvn/api/data-deposit/v1.1/swordv2/edit-media/study/doi:10.5072/FK2/LLJQYF
         */
        if (false) {
            Response uploadWorkingButZipIsNotUnpacked = given()
                    .auth().basic(apiToken, EMPTY_STRING)
                    .multiPart(zipfilename, fileObject, mimeType)
                    .header("Content-Disposition", "filename=" + zipfilename)
                    .header("Packaging", "http://purl.org/net/sword/package/SimpleZip")
                    .post("/dvn/api/data-deposit/v1.1/swordv2/edit-media/study/" + globalId3);
        }
        if (false) {
            Response uploadFile1Response = given()
                    .auth().basic(apiToken, EMPTY_STRING)
                    //                .body(Files.toByteArray(file1))
                    //                    .multiPart("file1.zip", file1, "application/zip")
                    //                    .multiPart("file1.zip", file1)
                    .contentType(ContentType.ANY)
                    //                    .contentType("application/zip")
                    .content(fileObject)
                    //                    .header("Content-Disposition", "filename=file1.zip")
                    //                    .header("Content-Type", "application/octet-stream")
                    //                    .header("Content-Type", "application/zip")
                    //                    .header("Packaging", "http://purl.org/net/sword/package/SimpleZip")
                    .post("/dvn/api/data-deposit/v1.1/swordv2/edit-media/study/" + globalId3);
            uploadFile1Response.body().prettyPrint();
        }

        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "curl -s --insecure --data-binary @src/test/java/org/dataverse/apitester/sword/data/" + zipfilename + " -H \"Content-Disposition: filename=trees.zip\" -H \"Content-Type: application/zip\" -H \"Packaging: http://purl.org/net/sword/package/SimpleZip\" -u " + apiToken + ": https://localhost:8181/dvn/api/data-deposit/v1.1/swordv2/edit-media/study/" + globalId});
        return p;
    }

    private Response publishDataverse(String dataverseToPublish) {
        /**
         * cat /dev/null | curl -u $API_TOKEN: -X POST -H "In-Progress: false"
         * --data-binary @-
         * https://$HOSTNAME/dvn/api/data-deposit/v1.1/swordv2/edit/dataverse/$DATAVERSE_ALIAS
         */
        return given()
                .auth().basic(apiToken, EMPTY_STRING)
                .header("In-Progress", "false")
                .post("/dvn/api/data-deposit/v1.1/swordv2/edit/dataverse/" + dataverseToPublish);
    }

    private Response deleteFile(int i) {
        return given()
                .auth().basic(apiToken, EMPTY_STRING)
                .delete("/dvn/api/data-deposit/v1.1/swordv2/edit-media/file/" + i);
    }

    private Response publishDataset() {
        /**
         * cat /dev/null | curl -u $API_TOKEN: -X POST -H "In-Progress: false"
         * --data-binary @-
         * https://$HOSTNAME/dvn/api/data-deposit/v1.1/swordv2/edit/study/doi:TEST/12345
         */
        return given()
                .auth().basic(apiToken, EMPTY_STRING)
                .header("In-Progress", "false")
                .post("/dvn/api/data-deposit/v1.1/swordv2/edit/study/" + globalId3);
    }

    private Response getSwordStatement() {
        /**
         * curl -u $API_TOKEN:
         * https://$HOSTNAME/dvn/api/data-deposit/v1.1/swordv2/statement/study/doi:TEST/12345
         */
        Response getDataset3Statement = given()
                .auth().basic(apiToken, EMPTY_STRING)
                .get("/dvn/api/data-deposit/v1.1/swordv2/statement/study/" + globalId3);
        return getDataset3Statement;
    }

    private void printCommandOutput(Process p) throws IOException {
        boolean actuallyPrint = false;
        try {
            p.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(SwordTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (actuallyPrint) {
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            input.close();
        }
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

//        Response deleteDataset3Response = deleteDataset(globalId3);
//        assertEquals(204, deleteDataset3Response.getStatusCode());
//        Response deleteDataverse3Response = deleteDataverse(dvAlias3);
//        assertEquals(200, deleteDataverse3Response.getStatusCode());
        // expect an exception if we don't remove all of the user's stuff first
        Response deleteUserResponse = given().delete("/api/admin/authenticatedUsers/" + username + "/");
        assertEquals(200, deleteUserResponse.getStatusCode());
    }

    private static Response deleteDataset(String globalId) {
        return given()
                .auth().basic(apiToken, EMPTY_STRING)
                .relaxedHTTPSValidation()
                .delete("/dvn/api/data-deposit/v1.1/swordv2/edit/study/" + globalId);
    }

    private static Response destroyDataset(String globalId) {
        int id = findDatasetIdFromGlobalId(globalId);
        return given().param("key", apiToken).when().delete("/api/datasets/" + id + "/destroy");
    }

    private static int findDatasetIdFromGlobalId(String globalId) {
        /**
         *
         * Assumes you have turned on experimental non-public search
         * https://github.com/IQSS/dataverse/issues/1299
         *
         * curl -X PUT -d true
         * http://localhost:8080/api/admin/settings/:SearchApiNonPublicAllowed
         *
         * curl -s
         * "http://localhost:8080/api/search?key=$ADMINKEY&q=dsPersistentId:doi\:10.5072/FK2/LLJQYF&show_entity_ids=true"
         * | jq '.data.items[0].entity_id'
         */
        Response searchForGlobalId = given()
                .get("api/search?key=" + apiToken
                        + "&q=dsPersistentId:\""
                        + globalId.replace(":", "\\:")
                        + "\"&show_entity_ids=true");
//        searchForGlobalId.body().prettyPrint();
        JsonPath jsonPath = JsonPath.from(searchForGlobalId.body().asString());
        int id = jsonPath.get("data.items[0].entity_id");
        return id;
    }

    private static Response listFilesFromVersion(String globalId, String version) {
        /**
         * curl -s
         * "http://localhost:8080/api/datasets/121/versions/1/files?key=$ADMINKEY"
         */
        int id = findDatasetIdFromGlobalId(globalId);
        return given()
                .param("key", apiToken)
                .when()
                .get("/api/datasets/" + id + "/versions/" + version + "/files");
    }

    private static Response listFilesFromVersionUsingEntityId(String entityId, String version) {
        return given()
                .param("key", apiToken)
                .when()
                .get("/api/datasets/" + entityId + "/versions/" + version + "/files");
    }

    private static Response makeSuperuser(String userToMakeSuperuser) {
        Response response = given().post("/api/admin/superuser/" + userToMakeSuperuser);
        return response;
    }

    private static Response deleteDataverse(String dvAlias) {
        return given().when().delete("/api/dataverses/" + dvAlias + "?key=" + apiToken);
    }
}
