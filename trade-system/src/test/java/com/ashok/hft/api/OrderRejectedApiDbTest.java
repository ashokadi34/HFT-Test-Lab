package com.ashok.hft.api;

import com.ashok.hft.db.DatabaseUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class OrderRejectedApiDbTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void shouldRejectInvalidSymbolAndPersistRejection()
            throws Exception {

        String requestBody = """
                {
                    "symbol": "INVALIDXYZ",
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

        String status =
                response.jsonPath().getString("status");

        assertNotNull(
                orderId,
                "Order ID should not be null"
        );

        assertEquals(
                "REJECTED",
                status,
                "Invalid symbol order should be REJECTED"
        );

        System.out.println(
                "Created Order ID: " + orderId
        );

        System.out.println(
                "API Order Status: " + status
        );

        // -------------------------------------------------
        // Verify orders table
        // -------------------------------------------------

        var resultSet =
                DatabaseUtils.getOrderById(orderId);

        assertTrue(
                resultSet.next(),
                "Rejected order should exist in database"
        );

        assertEquals(
                orderId,
                resultSet.getLong("id")
        );

        assertEquals(
                "INVALIDXYZ",
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
                "REJECTED",
                resultSet.getString("status")
        );

        resultSet.close();

        // -------------------------------------------------
        // Verify order status history
        // -------------------------------------------------

        List<String> statuses =
                DatabaseUtils.getOrderStatusHistory(orderId);

        System.out.println(
                "Status History: " + statuses
        );

        assertFalse(
                statuses.isEmpty(),
                "Status history should not be empty"
        );

        assertEquals(
                "NEW",
                statuses.get(0)
        );

        assertTrue(
                statuses.contains("VALIDATING"),
                "VALIDATING status should exist"
        );

        assertEquals(
                "REJECTED",
                statuses.get(statuses.size() - 1),
                "Final order status should be REJECTED"
        );
    }
}