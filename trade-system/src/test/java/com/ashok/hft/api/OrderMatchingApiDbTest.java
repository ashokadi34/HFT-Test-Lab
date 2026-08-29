package com.ashok.hft.api;

import com.ashok.hft.db.DatabaseUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.sql.ResultSet;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class OrderMatchingApiDbTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void shouldMatchBuyAndSellOrdersAndPersistTrade()
            throws Exception {

        // -------------------------------------------------
        // 1. Create BUY order
        // -------------------------------------------------

        String buyRequest = """
                {
                    "symbol": "INFY",
                    "price": 1950,
                    "quantity": 100,
                    "side": "BUY"
                }
                """;

        var buyResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(buyRequest)
                        .when()
                        .post("/api/orders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        Long buyOrderId =
                buyResponse.jsonPath().getLong("id");

        assertNotNull(
                buyOrderId,
                "BUY order ID should not be null"
        );

        System.out.println(
                "BUY Order ID: " + buyOrderId
        );

        // -------------------------------------------------
        // 2. Create SELL order
        // -------------------------------------------------

        String sellRequest = """
                {
                    "symbol": "INFY",
                    "price": 1950,
                    "quantity": 50,
                    "side": "SELL"
                }
                """;

        var sellResponse =
                given()
                        .contentType(ContentType.JSON)
                        .body(sellRequest)
                        .when()
                        .post("/api/orders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        Long sellOrderId =
                sellResponse.jsonPath().getLong("id");

        assertNotNull(
                sellOrderId,
                "SELL order ID should not be null"
        );

        System.out.println(
                "SELL Order ID: " + sellOrderId
        );

        // -------------------------------------------------
        // 3. Verify final API statuses
        // -------------------------------------------------

//        String buyStatus =
//                sellResponse.jsonPath()
//                        .getString("status");
//
//        System.out.println(
//                "SELL API Status: " + buyStatus
//        );

        // -------------------------------------------------
        // 4. Verify BUY order in database
        // -------------------------------------------------

        ResultSet buyResult =
                DatabaseUtils.getOrderById(buyOrderId);

        assertTrue(
                buyResult.next(),
                "BUY order should exist in database"
        );

        assertEquals(
                buyOrderId,
                buyResult.getLong("id")
        );

        assertEquals(
                "INFY",
                buyResult.getString("symbol")
        );

        assertEquals(
                1950.0,
                buyResult.getDouble("price")
        );

        assertEquals(
                50,
                buyResult.getInt("quantity")
        );

        assertEquals(
                "BUY",
                buyResult.getString("side")
        );

        assertEquals(
                "PARTIALLY_FILLED",
                buyResult.getString("status")
        );

        buyResult.close();

        // -------------------------------------------------
        // 5. Verify SELL order in database
        // -------------------------------------------------

        ResultSet sellResult =
                DatabaseUtils.getOrderById(sellOrderId);

        assertTrue(
                sellResult.next(),
                "SELL order should exist in database"
        );

        assertEquals(
                sellOrderId,
                sellResult.getLong("id")
        );

        assertEquals(
                "INFY",
                sellResult.getString("symbol")
        );

        assertEquals(
                1950.0,
                sellResult.getDouble("price")
        );

        assertEquals(
                0,
                sellResult.getInt("quantity")
        );

        assertEquals(
                "SELL",
                sellResult.getString("side")
        );

        assertEquals(
                "FILLED",
                sellResult.getString("status")
        );

        sellResult.close();

        // -------------------------------------------------
        // 6. Verify trade in database
        // -------------------------------------------------

        ResultSet tradeResult =
                DatabaseUtils.getTradeByOrderIds(
                        buyOrderId,
                        sellOrderId
                );

        assertTrue(
                tradeResult.next(),
                "Trade should exist in database"
        );

        assertEquals(
                buyOrderId,
                tradeResult.getLong("buy_order_id")
        );

        assertEquals(
                sellOrderId,
                tradeResult.getLong("sell_order_id")
        );

        assertEquals(
                "INFY",
                tradeResult.getString("symbol")
        );

        assertEquals(
                1950.0,
                tradeResult.getDouble("price")
        );

        assertEquals(
                50,
                tradeResult.getInt("quantity")
        );

        assertNotNull(
                tradeResult.getTimestamp("executed_time"),
                "Trade execution time should not be null"
        );

        tradeResult.close();

        // -------------------------------------------------
        // 7. Verify BUY status history
        // -------------------------------------------------

        List<String> buyStatuses =
                DatabaseUtils.getOrderStatusHistory(
                        buyOrderId
                );

        System.out.println(
                "BUY Status History: " + buyStatuses
        );

        assertTrue(
                buyStatuses.contains("PARTIALLY_FILLED"),
                "BUY order should contain PARTIALLY_FILLED status"
        );

        // -------------------------------------------------
        // 8. Verify SELL status history
        // -------------------------------------------------

        List<String> sellStatuses =
                DatabaseUtils.getOrderStatusHistory(
                        sellOrderId
                );

        System.out.println(
                "SELL Status History: " + sellStatuses
        );

        assertTrue(
                sellStatuses.contains("FILLED"),
                "SELL order should contain FILLED status"
        );
    }
}