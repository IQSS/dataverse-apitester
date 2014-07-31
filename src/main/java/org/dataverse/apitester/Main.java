package org.dataverse.apitester;

import static com.jayway.restassured.RestAssured.*;
import com.jayway.restassured.response.ValidatableResponse;
import static org.hamcrest.Matchers.*;

public class Main {

    public static void main(String[] args) {

        test_getVersion_inDatasets();
        test_listVersions_inDatasets();
        test_getDataset_inDatasets();

    }

    public static void test_getDataset_inDatasets() {

        /* PETE GOOD TESTING */
        get("http://localhost:8080/api/datasets/10?key=pete").then().assertThat()
                .body("data.latestVersion.versionState", equalTo("DRAFT"));

        /* UMA GOOD TESTING */
        final ValidatableResponse uma = get("http://localhost:8080/api/datasets/10?key=uma")
                .then();
        uma.assertThat().body("data.latestVersion.versionState", equalTo("RELEASED"));
        uma.assertThat().body("data.latestVersion.versionNumber", equalTo(1));
        uma.assertThat().body("data.latestVersion.versionMinorNumber", equalTo(1));

        /* BAD TESTING */
        get("http://localhost:8080/api/datasets/10?key=badkey")
                .then().assertThat().statusCode(401);

        get("http://localhost:8080/api/datasets/baddataset?key=pete")
                .then().assertThat().statusCode(404);

    }

    public static void test_listVersions_inDatasets() {

        /* PETE GOOD TESTING */
        final ValidatableResponse pete = get("http://localhost:8080/api/datasets/10/versions?key=pete")
                .then();
        pete.assertThat().body("data[0].versionState", equalTo("DRAFT"));
        pete.assertThat().body("data[1].versionState", equalTo("RELEASED"));
        pete.assertThat().body("data[1].versionNumber", equalTo(1));
        pete.assertThat().body("data[1].versionMinorNumber", equalTo(1));
        pete.assertThat().body("data[2].versionState", equalTo("RELEASED"));
        pete.assertThat().body("data[2].versionNumber", equalTo(1));
        pete.assertThat().body("data[2].versionMinorNumber", equalTo(0));

        /* UMA GOOD TESTING */
        final ValidatableResponse uma = get("http://localhost:8080/api/datasets/10/versions?key=uma")
                .then();
        uma.assertThat().body("data[0].versionState", equalTo("RELEASED"));
        uma.assertThat().body("data[0].versionNumber", equalTo(1));
        uma.assertThat().body("data[0].versionMinorNumber", equalTo(1));
        uma.assertThat().body("data[1].versionState", equalTo("RELEASED"));
        uma.assertThat().body("data[1].versionNumber", equalTo(1));
        uma.assertThat().body("data[1].versionMinorNumber", equalTo(0));

        /* BAD TESTING */
        get("http://localhost:8080/api/datasets/10/versions?key=badkey")
                .then().assertThat().statusCode(401);

        get("http://localhost:8080/api/datasets/baddataset/versions?key=pete")
                .then().assertThat().statusCode(404);

    }

    public static void test_getVersion_inDatasets() {

        /* PETE GOOD TESTING */
        get("http://localhost:8080/api/datasets/10/versions/:latest?key=pete")
                .then().assertThat().body("data.versionState", equalTo("DRAFT"));

        final ValidatableResponse peteLP = get("http://localhost:8080/api/datasets/10/versions/:latest-published?key=pete")
                .then();
        peteLP.assertThat().body("data.versionState", equalTo("RELEASED"));
        peteLP.assertThat().body("data.versionNumber", equalTo(1));
        peteLP.assertThat().body("data.versionMinorNumber", equalTo(1));

        get("http://localhost:8080/api/datasets/10/versions/:draft?key=pete")
                .then().assertThat().body("data.versionState", equalTo("DRAFT"));

        final ValidatableResponse pete1 = get("http://localhost:8080/api/datasets/10/versions/1?key=pete")
                .then();
        pete1.assertThat().body("data.versionState", equalTo("RELEASED"));
        pete1.assertThat().body("data.versionNumber", equalTo(1));
        pete1.assertThat().body("data.versionMinorNumber", equalTo(0));

        final ValidatableResponse pete1point0 = get("http://localhost:8080/api/datasets/10/versions/1.0?key=pete")
                .then();
        pete1point0.assertThat().body("data.versionState", equalTo("RELEASED"));
        pete1point0.assertThat().body("data.versionNumber", equalTo(1));
        pete1point0.assertThat().body("data.versionMinorNumber", equalTo(0));

        final ValidatableResponse pete1point1 = get("http://localhost:8080/api/datasets/10/versions/1.1?key=pete")
                .then();
        pete1point1.assertThat().body("data.versionState", equalTo("RELEASED"));
        pete1point1.assertThat().body("data.versionNumber", equalTo(1));
        pete1point1.assertThat().body("data.versionMinorNumber", equalTo(1));

        /* UMA GOOD TESTING */
        final ValidatableResponse umaL = get("http://localhost:8080/api/datasets/10/versions/:latest?key=uma")
                .then();
        umaL.assertThat().body("data.versionState", equalTo("RELEASED"));
        umaL.assertThat().body("data.versionNumber", equalTo(1));
        umaL.assertThat().body("data.versionMinorNumber", equalTo(1));

        final ValidatableResponse umaLP = get("http://localhost:8080/api/datasets/10/versions/:latest-published?key=uma")
                .then();
        umaLP.assertThat().body("data.versionState", equalTo("RELEASED"));
        umaLP.assertThat().body("data.versionNumber", equalTo(1));
        umaLP.assertThat().body("data.versionMinorNumber", equalTo(1));

        get("http://localhost:8080/api/datasets/10/versions/:draft?key=uma")
                .then().assertThat().statusCode(401);

        final ValidatableResponse uma1 = get("http://localhost:8080/api/datasets/10/versions/1?key=uma")
                .then();
        uma1.assertThat().body("data.versionState", equalTo("RELEASED"));
        uma1.assertThat().body("data.versionNumber", equalTo(1));
        uma1.assertThat().body("data.versionMinorNumber", equalTo(0));

        final ValidatableResponse uma1point0 = get("http://localhost:8080/api/datasets/10/versions/1.0?key=uma")
                .then();
        uma1point0.assertThat().body("data.versionState", equalTo("RELEASED"));
        uma1point0.assertThat().body("data.versionNumber", equalTo(1));
        uma1point0.assertThat().body("data.versionMinorNumber", equalTo(0));

        final ValidatableResponse uma1point1 = get("http://localhost:8080/api/datasets/10/versions/1.1?key=uma")
                .then();
        uma1point1.assertThat().body("data.versionState", equalTo("RELEASED"));
        uma1point1.assertThat().body("data.versionNumber", equalTo(1));
        uma1point1.assertThat().body("data.versionMinorNumber", equalTo(1));

        /* BAD TESTING */
        get("http://localhost:8080/api/datasets/10/versions/:notworking?key=pete")
                .then().assertThat().statusCode(400);

        get("http://localhost:8080/api/datasets/10/versions/A.B?key=pete")
                .then().assertThat().statusCode(400);

        get("http://localhost:8080/api/datasets/10/versions/1.2.3?key=pete")
                .then().assertThat().statusCode(400);

        get("http://localhost:8080/api/datasets/10/versions/1.2.3?key=uma")
                .then().assertThat().statusCode(400);

        get("http://localhost:8080/api/datasets/10/versions/1.1?key=badkey")
                .then().assertThat().statusCode(401);

        get("http://localhost:8080/api/datasets/baddataset/versions/1.1?key=pete")
                .then().assertThat().statusCode(404);

    }
}
