import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AuthIntegrationTest {

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOkWthValidToken(){
        //1.arrange-prepare resource
        //2.act-call methods
        //3.assert-chack condition

        String payload= """
                {
                   "email": "testuser@test.com",
                   "password": "password123"
                 }
                """;

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", Matchers.notNullValue())
                .extract()
                .response();

        System.out.println("Generated Token: " + response.jsonPath().getString("token"));

    }



    @Test
    public void shouldReturnUnAuthorizedWithInvalidLogin(){
        //1.arrange-prepare resource
        //2.act-call methods
        //3.assert-chack condition

        String payload= """
                {
                   "email": "invalid_user@test.com",
                   "password": "password123"
                 }
                """;

       RestAssured.given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);


    }
}
