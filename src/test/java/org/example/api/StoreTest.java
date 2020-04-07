package org.example.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.Pet;
import org.example.model.Store;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class StoreTest {
    public static final Logger logger = LogManager.getLogger(StoreTest.class);
    static RequestSpecification requestSpec;
    static ResponseSpecification responseSpec;
    static ResponseSpecification responseSpecAfterDelete;

    Store order = new Store();
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
        order.setId(id);
    }

    @Test(priority = 0)
    public void testPost() {
        logger.info("Begin TestPost()");
        given()
                .spec(requestSpec)
                .body(order)
                .when()
                .post("/store/order")
                .then()
                .spec(responseSpec);

        Store actualOrder = given()
                .spec(requestSpec)
                .pathParam("orderId", id)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .spec(responseSpec)
                .extract().body().as(Store.class);
        Assert.assertEquals(actualOrder.getId(), order.getId());
        logger.info("End TestPost()");
    }

    @Test(priority = 5)
    public void testDelete() {
        logger.info("Begin TestDelete()");
        given()
                .spec(requestSpec)
                .pathParam("orderId", id)
                .body(order)
                .when()
                .delete("/store/order/{orderId}")
                .then()
                .spec(responseSpec);
        logger.info("End TestDelete()");
    }

    //будем проверять после delete уже, когда не будет элемента. вроде невалид
    @Test(priority = 10)
    public void testGetDeleted() {
        logger.info("Begin TestGetDeleted()");
        given()
                .spec(requestSpec)
                .pathParam("orderId", id)
                .when()
                .get("/store/order/{orderId}")
                .then()
                .spec(responseSpecAfterDelete);
        logger.info("End TestGetDeleted()");
    }
}
