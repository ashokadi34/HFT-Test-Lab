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
class OrderStatusHistoryApiDbTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void shouldCreateOrderAndPersistStatusHistory()
            throws Exception {

        String requestBody = """
        {
            "symbol": "HDFCBANK",
            "price": 1000,
            "quantity": 10,
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

        assertNotNull(
                orderId,
                "Order ID should not be null"
        );

        System.out.println(
                "Created Order ID: " + orderId
        );

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

        assertTrue(
                statuses.contains("ACCEPTED"),
                "ACCEPTED status should exist"
        );

        assertTrue(
                statuses.contains("SENT_TO_EXCHANGE"),
                "SENT_TO_EXCHANGE status should exist"
        );

        assertEquals(
                "ACCEPTED",
                statuses.get(statuses.size() - 1),
                "Unmatched order should remain ACCEPTED"
        );
    }
}