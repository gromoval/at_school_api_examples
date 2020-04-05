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
        // Читаем конфигурационный файл в System.properties -- простейшее HashMap хранилище
        System.getProperties().load(ClassLoader.getSystemResourceAsStream("application.properties"));
        // здесь мы задаём глобальные преднастройки для каждого запроса. Аналогично можно задавать их
        // перед каждым запросом отдельно, либо создать поле RequestSpecification и задавать весь пакет настроек
        // в конкретных запросах. Подробнее тут: https://habr.com/ru/post/421005/
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://petstore.swagger.io/v2/") // задаём базовый адрес каждого ресурса
                .addHeader("api_key", System.getProperty("api.key")) // задаём заголовок с токеном для авторизации
                // обязательно учитывайте, что любые приватные данные необходимо хранить в отдельных файлах, которые НЕ публикуютя
                // в открытых репозиториях (в закрытых тоже лучше не публиковать)
                .setAccept(ContentType.JSON) // задаём заголовок accept
                .setContentType(ContentType.JSON) // задаём заголовок content-type
                .log(LogDetail.ALL) // дополнительная инструкция полного логгирования для RestAssured
                .build(); // после этой команды происходит формирование стандартной "шапки" запроса.
        // Подробнее о билдерах можно почитать https://refactoring.guru/ru/design-patterns/builder
        // но интереснее в книжке Effective Java
    }

    /**
     * Простейшая проверка: создаём объект, сохраняем на сервере и проверяем, что при запросе возвращается
     * "тот же" объект
     */
    @Test
    public void checkObjectSave() {
        Pet pet = new Pet();
        int id = new Random().nextInt(500000); // просто нужно создать произвольный айди
        String name = "Pet_" + UUID.randomUUID().toString(); // UUID гарантирует уникальность строки
        pet.setId(id);
        pet.setName(name);

        given()  // часть стандартного синтаксиса BDD. Означает предварительные данные. Иначе говоря ДАНО:
                .body(pet) // указываем что  помещаем в тело запроса. Поскольку у нас подключен Gson, он преобразуется в JSON
            .when()   // КОГДА:
                .post("/pet") // выполняем запрос методом POST к ресурсу /pet, при этом используется ранее
                // созданная "шапка". Именно в этом методе создаётся "текстовый файл" запроса, он отправляется
                // посредством HTTP к серверу. Затем в этом же методе получается ответ. После этого метода мы
                // работаем с ОТВЕТОМ
            .then() // ТОГДА: (указывает, что после этой части будут выполняться проверки-утверждения)
                .statusCode(200); // например проверка кода ответа.он просто выдёргивается из текста ответа

        /*
         * Подобный стиль написания кода называется fluent -- текучим. Мы последовательно вызываем методы, при этом
         * объект у которого вызываются методы может меняться по ходу вызовов. метод возвращает какой-то объект,
         * следующий в цепочке метод вызывается у него, он возвращает ещё какой-то объект (или тот же), и следующий
         * метод вызывается уже у него.
         * Подобный подход характерен для билдеров (например похоже работают Stream, Fluent Wait и многие другие).
         * Он позволяет сократить количество кода, однако может вызывать некоторое непонимание у новичков. Idea
         * помогает понять какой объект возвращается после метода... а остальное дело привычки
         * */

        Pet actual =
                given()
                        .pathParam("petId", id) // заранее задаём переменную petId
                    .when()
                        .get("/pet/{petId}") // которая подставится в путь ресурса перед выполнением запроса.
                        // после этого метода мы так же будем иметь уже ОТВЕТ от сервера.
                    .then()
                        .statusCode(200)
                        .extract().body() // у полученного ответа мы можем взять тело
                        .as(Pet.class); // и распарсить его как объект Pet. Всё это получается автоматически, так как
                        // у нас подключена библиотека для работы с JSON и мы дополнительно указали в общей "шапке"
                        // что хотим получать и отправлять объекты в формате JSON
        // Здесь сравниваем только имя, поскольку многие поля у наших объектов не совпадают: поскольку
        // мы не задали список тэгов животного, в объекте pet он будет null, тогда как в объекте actual Gson присвоит
        // ему пустой список. Это происходит потому что в ответ всегда приходит полный JSON модели
        // (как описано в Swagger.io), даже если мы отправляли не полную модель.
        // TODO можно переопределить методы equals у объектов Pet и других, чтобы происходило корректное сравнение
        // не заданных полей с пустыми
        Assert.assertEquals(actual.getName(), pet.getName());

    }
}
