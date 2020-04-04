package org.example.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import org.example.model.Pet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class ApiTest {
    @BeforeClass
    public void prepare() throws IOException {
        System.getProperties().load(ClassLoader.getSystemResourceAsStream("application.properties"));
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://petstore.swagger.io/v2/")
                .addHeader("api_key", System.getProperty("api.key"))
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }

    @Test
    public void checkObjectSave() {
        Pet pet = new Pet();
        int id = new Random().nextInt(500000);
        String name = "Pet_" + UUID.randomUUID().toString();
        pet.setId(id);
        pet.setName(name);

        given()
                .body(pet)
                .when().post("/pet")
                .then().statusCode(200);

        Pet actual = given()
                .pathParam("petId", id)
                .when().get("/pet/{petId}")
                .then().statusCode(200)
                .extract().body()
                .as(Pet.class);
        Assert.assertEquals(actual.getName(), pet.getName());

    }
}
