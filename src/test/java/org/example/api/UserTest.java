package org.example.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.User;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class UserTest {
    private Logger logger = LogManager.getLogger(UserTest.class);
    static RequestSpecification requestSpec;
    static ResponseSpecification responseSpec;
    static ResponseSpecification responseSpecAfterDelete;

    User user = new User();
    static int id;

    @BeforeSuite
    public void config() throws IOException {
        System.getProperties().load(ClassLoader.getSystemResourceAsStream("application.properties"));
        requestSpec = new RequestSpecBuilder()
                .setBaseUri("https://petstore.swagger.io/v2/")
                .addHeader("api_key", System.getProperty("api.key"))
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
        responseSpec = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .log(LogDetail.ALL)
                .build();
        responseSpecAfterDelete = new ResponseSpecBuilder()
                .expectStatusCode(404)
                .log(LogDetail.ALL)
                .build();

        id = new Random().nextInt(500000);
        String name = "User_" + UUID.randomUUID().toString();
        user.setId(id);
        user.setUsername(name);
    }

//    чтобы новых юзеров добавлять, нужно залогиниться
    @Test(priority = 0)
    public void testLogin() {
        logger.info("Begin TestLogin()");
        given()
                .spec(requestSpec)
                .when()
                .get("/user/login")
                .then()
                .spec(responseSpec);
        logger.info("End TestLogin()");
    }

    @Test(priority = 1)
    public void testPost() {
        logger.info("Begin TestPost()");
        given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/user")
                .then()
                .spec(responseSpec);

        User actualUser = given()
                .spec(requestSpec)
                .pathParam("username", user.getUsername())
                .when()
                .get("/user/{username}")
                .then()
                .spec(responseSpec)
                .extract().body().as(User.class);
        Assert.assertEquals(actualUser.getUsername(), user.getUsername());
        logger.info("End TestPost()");
    }

    @Test(priority = 2)
    public void testPut() {
        logger.info("Begin TestPut()");
        user.setFirstName("Вася");
        given()
                .spec(requestSpec)
                .body(user)
                .pathParam("username", user.getUsername())
                .when()
                .put("/user/{username}")
                .then()
                .spec(responseSpec);

        User actualUser = given()
                .spec(requestSpec)
                .pathParam("username", user.getUsername())
                .when()
                .get("/user/{username}")
                .then()
                .spec(responseSpec)
                .extract().body().as(User.class);
        Assert.assertEquals(actualUser.getFirstName(), user.getFirstName());
        logger.info("End TestPut()");
    }

    @Test(priority = 5)
    public void testDelete() {
        logger.info("Begin TestDelete()");
        given()
                .spec(requestSpec)
                .pathParam("username", user.getUsername())
                .body(user)
                .when()
                .delete("/user/{username}")
                .then()
                .spec(responseSpec);
        logger.info("End TestDelete()");
    }

    //будем проверять после delete уже, когда не будет pet. вроде невалид
    @Test(priority = 10)
    public void testGetDeleted() {
        logger.info("Begin TestGetDeleted()");
        given()
                .spec(requestSpec)
                .pathParam("username", user.getUsername())
                .when()
                .get("/user/{username}")
                .then()
                .spec(responseSpecAfterDelete);
        logger.info("End TestGetDeleted()");
    }
}
