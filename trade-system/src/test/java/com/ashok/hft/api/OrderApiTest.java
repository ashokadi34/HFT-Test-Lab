package com.ashok.hft.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.InputStream;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderApiTest {

    @LocalServerPort
    private int port;

    private List<OrderTestData> orders;

    @BeforeEach
    void setup() throws Exception {

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        ObjectMapper objectMapper = new ObjectMapper();

        InputStream inputStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("testdata/orders.json");

        orders = objectMapper.readValue(
                inputStream,
                new TypeReference<List<OrderTestData>>() {}
        );
    }

    @Test
    void shouldCreateOrdersFromTestData() {

        for (OrderTestData order : orders) {

            Response response =
                    given()
                            .port(port)
                            .header("Content-Type", "application/json")
                            .body(order)
                            .when()
                            .post("/api/orders")
                            .then()
                            .statusCode(200)
                            .body("id", notNullValue())
                            .body("symbol", equalTo(order.getSymbol()))
                            .body("price", equalTo((float) order.getPrice()))
                            .body("quantity", greaterThanOrEqualTo(0))
                            .body("side", equalTo(order.getSide()))
                            .body("status", notNullValue())
                            .extract()
                            .response();

            System.out.println(
                    "Created Order ID: " +
                            response.jsonPath().getLong("id") +
                            " | " +
                            order.getSide() +
                            " | " +
                            order.getSymbol()
            );
        }
    }
}