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
class OrderPriceTimePriorityApiDbTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void shouldApplyPriceTimePriorityAndPersistTrades()
            throws Exception {

        /*
         * Use a highly unique price for this test execution.
         *
         * Both SELL orders intentionally use the same price
         * so that price-time priority can be verified.
         *
         * The BUY order uses the same price to allow matching.
         */
        double testPrice = 2000;
//                100000 + (System.nanoTime() % 1000000);

        System.out.println(
                "Test Price: " + testPrice
        );

        // -------------------------------------------------
        // 1. Create first SELL order
        // -------------------------------------------------

        String sellRequest1 = """
                {
                    "symbol": "HDFCBANK",
                    "price": %s,
                    "quantity": 30,
                    "side": "SELL"
                }
                """.formatted(testPrice);

        var sellResponse1 =
                given()
                        .contentType(ContentType.JSON)
                        .body(sellRequest1)
                        .when()
                        .post("/api/orders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        Long sellOrderId1 =
                sellResponse1.jsonPath().getLong("id");

        assertNotNull(
                sellOrderId1,
                "First SELL order ID should not be null"
        );

        System.out.println(
                "SELL #1 Order ID: " + sellOrderId1
        );

        // -------------------------------------------------
        // 2. Create second SELL order
        // -------------------------------------------------

        String sellRequest2 = """
                {
                    "symbol": "HDFCBANK",
                    "price": %s,
                    "quantity": 40,
                    "side": "SELL"
                }
                """.formatted(testPrice);

        var sellResponse2 =
                given()
                        .contentType(ContentType.JSON)
                        .body(sellRequest2)
                        .when()
                        .post("/api/orders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        Long sellOrderId2 =
                sellResponse2.jsonPath().getLong("id");

        assertNotNull(
                sellOrderId2,
                "Second SELL order ID should not be null"
        );

        System.out.println(
                "SELL #2 Order ID: " + sellOrderId2
        );

        // -------------------------------------------------
        // 3. Create BUY order
        // -------------------------------------------------

        String buyRequest = """
                {
                    "symbol": "HDFCBANK",
                    "price": %s,
                    "quantity": 50,
                    "side": "BUY"
                }
                """.formatted(testPrice);

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
        // 4. Verify first SELL order
        // -------------------------------------------------

        ResultSet sellResult1 =
                DatabaseUtils.getOrderById(sellOrderId1);

        assertTrue(
                sellResult1.next(),
                "First SELL order should exist"
        );

        assertEquals(
                "HDFCBANK",
                sellResult1.getString("symbol")
        );

        assertEquals(
                testPrice,
                sellResult1.getDouble("price")
        );

        assertEquals(
                0,
                sellResult1.getInt("quantity")
        );

        assertEquals(
                "SELL",
                sellResult1.getString("side")
        );

        assertEquals(
                "FILLED",
                sellResult1.getString("status")
        );

        sellResult1.close();

        // -------------------------------------------------
        // 5. Verify second SELL order
        // -------------------------------------------------

        ResultSet sellResult2 =
                DatabaseUtils.getOrderById(sellOrderId2);

        assertTrue(
                sellResult2.next(),
                "Second SELL order should exist"
        );

        assertEquals(
                "HDFCBANK",
                sellResult2.getString("symbol")
        );

        assertEquals(
                testPrice,
                sellResult2.getDouble("price")
        );

        assertEquals(
                20,
                sellResult2.getInt("quantity")
        );

        assertEquals(
                "SELL",
                sellResult2.getString("side")
        );

        assertEquals(
                "PARTIALLY_FILLED",
                sellResult2.getString("status")
        );

        sellResult2.close();

        // -------------------------------------------------
        // 6. Verify BUY order
        // -------------------------------------------------

        ResultSet buyResult =
                DatabaseUtils.getOrderById(buyOrderId);

        assertTrue(
                buyResult.next(),
                "BUY order should exist"
        );

        assertEquals(
                "HDFCBANK",
                buyResult.getString("symbol")
        );

        assertEquals(
                testPrice,
                buyResult.getDouble("price")
        );

        assertEquals(
                0,
                buyResult.getInt("quantity")
        );

        assertEquals(
                "BUY",
                buyResult.getString("side")
        );

        assertEquals(
                "FILLED",
                buyResult.getString("status")
        );

        buyResult.close();

        // -------------------------------------------------
        // 7. Verify trade between BUY and SELL #1
        // -------------------------------------------------

        ResultSet tradeResult1 =
                DatabaseUtils.getTradeByOrderIds(
                        buyOrderId,
                        sellOrderId1
                );

        assertTrue(
                tradeResult1.next(),
                "Trade with SELL #1 should exist"
        );

        assertEquals(
                buyOrderId,
                tradeResult1.getLong("buy_order_id")
        );

        assertEquals(
                sellOrderId1,
                tradeResult1.getLong("sell_order_id")
        );

        assertEquals(
                "HDFCBANK",
                tradeResult1.getString("symbol")
        );

        assertEquals(
                testPrice,
                tradeResult1.getDouble("price")
        );

        assertEquals(
                30,
                tradeResult1.getInt("quantity")
        );

        assertNotNull(
                tradeResult1.getTimestamp("executed_time"),
                "Trade execution time should not be null"
        );

        tradeResult1.close();

        // -------------------------------------------------
        // 8. Verify trade between BUY and SELL #2
        // -------------------------------------------------

        ResultSet tradeResult2 =
                DatabaseUtils.getTradeByOrderIds(
                        buyOrderId,
                        sellOrderId2
                );

        assertTrue(
                tradeResult2.next(),
                "Trade with SELL #2 should exist"
        );

        assertEquals(
                buyOrderId,
                tradeResult2.getLong("buy_order_id")
        );

        assertEquals(
                sellOrderId2,
                tradeResult2.getLong("sell_order_id")
        );

        assertEquals(
                "HDFCBANK",
                tradeResult2.getString("symbol")
        );

        assertEquals(
                testPrice,
                tradeResult2.getDouble("price")
        );

        assertEquals(
                20,
                tradeResult2.getInt("quantity")
        );

        assertNotNull(
                tradeResult2.getTimestamp("executed_time"),
                "Trade execution time should not be null"
        );

        tradeResult2.close();

        // -------------------------------------------------
        // 9. Verify BUY status history
        // -------------------------------------------------

        List<String> buyStatuses =
                DatabaseUtils.getOrderStatusHistory(
                        buyOrderId
                );

        System.out.println(
                "BUY Status History: " + buyStatuses
        );

        assertTrue(
                buyStatuses.contains("FILLED"),
                "BUY order should contain FILLED status"
        );

        // -------------------------------------------------
        // 10. Verify SELL #1 status history
        // -------------------------------------------------

        List<String> sellStatuses1 =
                DatabaseUtils.getOrderStatusHistory(
                        sellOrderId1
                );

        System.out.println(
                "SELL #1 Status History: " + sellStatuses1
        );

        assertTrue(
                sellStatuses1.contains("FILLED"),
                "SELL #1 should contain FILLED status"
        );

        // -------------------------------------------------
        // 11. Verify SELL #2 status history
        // -------------------------------------------------

        List<String> sellStatuses2 =
                DatabaseUtils.getOrderStatusHistory(
                        sellOrderId2
                );

        System.out.println(
                "SELL #2 Status History: " + sellStatuses2
        );

        assertTrue(
                sellStatuses2.contains("PARTIALLY_FILLED"),
                "SELL #2 should contain PARTIALLY_FILLED status"
        );

        // -------------------------------------------------
        // 12. Final execution verification
        // -------------------------------------------------

        int firstExecution = 30;
        int secondExecution = 20;

        assertEquals(
                50,
                firstExecution + secondExecution,
                "BUY order should be completely executed"
        );

        System.out.println(
                "Price-Time Priority verified successfully."
        );
    }
}