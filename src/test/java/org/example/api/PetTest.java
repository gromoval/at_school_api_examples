package org.example.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.Pet;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class PetTest {
    public final Logger logger = LogManager.getLogger(getClass());
    static RequestSpecification requestSpec;
    static ResponseSpecification responseSpec;
    static ResponseSpecification responseSpecAfterDelete;

    Pet pet = new Pet();
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
        String name = "Pet_" + UUID.randomUUID().toString();
        pet.setId(id);
        pet.setName(name);
    }

    @Test(priority = 0)
    public void testPost() {
        logger.info("Begin TestPost()");
        given()
                .spec(requestSpec)
                .body(pet)
                .when()
                .post("/pet")
                .then()
                .spec(responseSpec);

        Pet actualPet = given()
                .spec(requestSpec)
                .pathParam("petId", id)
                .when()
                .get("/pet/{petId}")
                .then()
                .spec(responseSpec)
                .extract().body().as(Pet.class);
        Assert.assertEquals(actualPet.getName(), pet.getName());
        logger.info("End TestPost()");
    }

    @Test(priority = 1)
    public void testPut() {
        logger.info("Begin TestPut()");
        pet.setName("Штуша кутуша");
        given()
                .spec(requestSpec)
                .body(pet)
                .when()
                .put("/pet")
                .then()
                .spec(responseSpec);

        Pet actualPet = given()
                .spec(requestSpec)
                .pathParam("petId", id)
                .when()
                .get("/pet/{petId}")
                .then()
                .spec(responseSpec)
                .extract().body().as(Pet.class);
        Assert.assertEquals(actualPet.getName(), pet.getName());
        logger.info("End TestPut()");
    }

    @Test(priority = 5)
    public void testDelete() {
        logger.info("Begin TestDelete()");
        given()
                .spec(requestSpec)
                .pathParam("petId", id)
                .body(pet)
                .when()
                .delete("/pet/{petId}")
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
                .pathParam("petId", id)
                .when()
                .get("/pet/{petId}")
                .then()
                .spec(responseSpecAfterDelete);
        logger.info("End TestGetDeleted()");
    }
}
