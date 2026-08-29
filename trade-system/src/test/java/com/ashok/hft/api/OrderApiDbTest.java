package com.ashok.hft.api;

import com.ashok.hft.db.DatabaseUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.sql.ResultSet;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class OrderApiDbTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void shouldCreateOrderAndPersistInDatabase() throws Exception {

        String requestBody = """
                {
                    "symbol": "INFY",
                    "price": 1800,
                    "quantity": 100,
                    "side": "BUY"
                }
                """;

        var response =
                given()
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .when()
                        .post("/api/orders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        Long orderId =
                response.jsonPath().getLong("id");

        assertNotNull(orderId);

        System.out.println(
                "Created Order ID: " + orderId
        );

        ResultSet resultSet =
                DatabaseUtils.getOrderById(orderId);

        assertTrue(
                resultSet.next(),
                "Order was not found in database"
        );

        assertEquals(
                orderId,
                resultSet.getLong("id")
        );

        assertEquals(
                "INFY",
                resultSet.getString("symbol")
        );

        assertEquals(
                1800.0,
                resultSet.getDouble("price")
        );

        assertEquals(
                100,
                resultSet.getInt("quantity")
        );

        assertEquals(
                "BUY",
                resultSet.getString("side")
        );

        assertEquals(
                "ACCEPTED",
                resultSet.getString("status")
        );
    }
}