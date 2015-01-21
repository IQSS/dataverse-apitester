package org.dataverse.apitester.sword;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import static com.jayway.restassured.path.xml.XmlPath.from;
import com.jayway.restassured.response.Response;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
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
    final String expectedSwordSpecVersion = "2.0";

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
        String EMPTY_STRING = "";
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

    @AfterClass
    public static void deleteUser() {
        Response response = given().delete("/api/s/authenticatedUsers/" + username + "/");
        assertEquals(response.getStatusCode(), 200);
    }
}
