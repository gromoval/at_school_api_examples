package org.example.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.model.Pet;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class PetTest {
    static RequestSpecification requestSpec;
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

        id = new Random().nextInt(500000);
        String name = "Pet_" + UUID.randomUUID().toString();
        pet.setId(id);
        pet.setName(name);
    }

    //будем проверять после delete уже, когда не будет pet. вроде невалид
    @Test(priority = 10)
    public void testGetDeleted() {
        Response response = given()
                .spec(requestSpec)
                .pathParam("petId", id)
                .get("/pet/{petId}");

        switch (response.getStatusCode()) {
            case 200:
                System.out.println("Все хорошо. запрос выполнен");
                break;
            case 404:
                System.out.println("Элемент не найден!");
                break;
            case 405:
                System.out.println("Неверный метод применен (Get вместо Post и т.п.)");
            case 500:
                System.out.println("Внутренняя ошибка сервера!");
            case 502:
                System.out.println("Вышестоящий сервер вернул какую-то ерунду...");
            default:
                System.out.println("Вообще не понятно, что произошло...");
                break;
        }
    }

    @Test(priority = 0)
    public void testPost() {
        given()
                .spec(requestSpec)
                .body(pet)
                .when()
                .post("/pet")
                .then()
                .statusCode(200);

        Pet actualPet = given()
                .spec(requestSpec)
                .pathParam("petId", id)
                .when()
                .get("/pet/{petId}")
                .then()
                .statusCode(200)
                .extract().body().as(Pet.class);
        Assert.assertEquals(actualPet.getName(), pet.getName());
    }

    @Test(priority = 1)
    public void testPut() {
        pet.setName("Штуша кутуша");
        given()
                .spec(requestSpec)
                .body(pet)
                .when()
                .put("/pet")
                .then()
                .statusCode(200);

        Pet actualPet = given()
                .spec(requestSpec)
                .pathParam("petId", id)
                .when()
                .get("/pet/{petId}")
                .then()
                .statusCode(200)
                .extract().body().as(Pet.class);
        Assert.assertEquals(actualPet.getName(), pet.getName());
    }

    @Test(priority = 5)
    public void testDelete() {
        Response response = given()
                .spec(requestSpec)
                .pathParam("petId", id)
                .body(pet)
                .delete("/pet/{petId}");

        System.out.printf("Response: %s\n", response.asString());
        System.out.printf("Status Code: %s\n", response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200);
    }
}
