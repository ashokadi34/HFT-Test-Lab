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
class OrderMultiMatchApiDbTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void shouldMatchBuyOrderAgainstMultipleSellOrders()
            throws Exception {

        // -------------------------------------------------
        // 1. Create SELL order #1
        // AAPL @ 1990 -> 30
        // -------------------------------------------------

        String sellRequest1 = """
                {
                    "symbol": "AAPL",
                    "price": 1990,
                    "quantity": 30,
                    "side": "SELL"
                }
                """;

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

        assertNotNull(sellOrderId1);

        System.out.println(
                "SELL Order 1 ID: " + sellOrderId1
        );

        // -------------------------------------------------
        // 2. Create SELL order #2
        // AAPL @ 2000 -> 40
        // -------------------------------------------------

        String sellRequest2 = """
                {
                    "symbol": "AAPL",
                    "price": 2000,
                    "quantity": 40,
                    "side": "SELL"
                }
                """;

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

        assertNotNull(sellOrderId2);

        System.out.println(
                "SELL Order 2 ID: " + sellOrderId2
        );

        // -------------------------------------------------
        // 3. Create SELL order #3
        // AAPL @ 2000 -> 50
        // -------------------------------------------------

        String sellRequest3 = """
                {
                    "symbol": "AAPL",
                    "price": 2000,
                    "quantity": 50,
                    "side": "SELL"
                }
                """;

        var sellResponse3 =
                given()
                        .contentType(ContentType.JSON)
                        .body(sellRequest3)
                        .when()
                        .post("/api/orders")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        Long sellOrderId3 =
                sellResponse3.jsonPath().getLong("id");

        assertNotNull(sellOrderId3);

        System.out.println(
                "SELL Order 3 ID: " + sellOrderId3
        );

        // -------------------------------------------------
        // 4. Create BUY order
        // AAPL @ 2000 -> 100
        // -------------------------------------------------

        String buyRequest = """
                {
                    "symbol": "AAPL",
                    "price": 2000,
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

        assertNotNull(buyOrderId);

        System.out.println(
                "BUY Order ID: " + buyOrderId
        );

        // -------------------------------------------------
        // 5. Verify BUY order in database
        // -------------------------------------------------

        ResultSet buyResult =
                DatabaseUtils.getOrderById(buyOrderId);

        assertTrue(
                buyResult.next(),
                "BUY order should exist in database"
        );

        assertEquals(
                "AAPL",
                buyResult.getString("symbol")
        );

        assertEquals(
                2000.0,
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
        // 6. Verify SELL order #1
        // Expected: completely filled
        // -------------------------------------------------

        ResultSet sellResult1 =
                DatabaseUtils.getOrderById(sellOrderId1);

        assertTrue(
                sellResult1.next(),
                "SELL order #1 should exist"
        );

        assertEquals(
                "AAPL",
                sellResult1.getString("symbol")
        );

        assertEquals(
                1990.0,
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
        // 7. Verify SELL order #2
        // Expected: completely filled
        // -------------------------------------------------

        ResultSet sellResult2 =
                DatabaseUtils.getOrderById(sellOrderId2);

        assertTrue(
                sellResult2.next(),
                "SELL order #2 should exist"
        );

        assertEquals(
                "AAPL",
                sellResult2.getString("symbol")
        );

        assertEquals(
                2000.0,
                sellResult2.getDouble("price")
        );

        assertEquals(
                0,
                sellResult2.getInt("quantity")
        );

        assertEquals(
                "SELL",
                sellResult2.getString("side")
        );

        assertEquals(
                "FILLED",
                sellResult2.getString("status")
        );

        sellResult2.close();

        // -------------------------------------------------
        // 8. Verify SELL order #3
        // Expected: partially filled
        // -------------------------------------------------

        ResultSet sellResult3 =
                DatabaseUtils.getOrderById(sellOrderId3);

        assertTrue(
                sellResult3.next(),
                "SELL order #3 should exist"
        );

        assertEquals(
                "AAPL",
                sellResult3.getString("symbol")
        );

        assertEquals(
                2000.0,
                sellResult3.getDouble("price")
        );

        assertEquals(
                20,
                sellResult3.getInt("quantity")
        );

        assertEquals(
                "SELL",
                sellResult3.getString("side")
        );

        assertEquals(
                "PARTIALLY_FILLED",
                sellResult3.getString("status")
        );

        sellResult3.close();

        // -------------------------------------------------
        // 9. Verify Trade #1
        // SELL #1 @ 1990
        // Quantity = 30
        // -------------------------------------------------

        ResultSet tradeResult1 =
                DatabaseUtils.getTradeByOrderIds(
                        buyOrderId,
                        sellOrderId1
                );

        assertTrue(
                tradeResult1.next(),
                "Trade #1 should exist"
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
                "AAPL",
                tradeResult1.getString("symbol")
        );

        assertEquals(
                1990.0,
                tradeResult1.getDouble("price")
        );

        assertEquals(
                30,
                tradeResult1.getInt("quantity")
        );

        tradeResult1.close();

        // -------------------------------------------------
        // 10. Verify Trade #2
        // SELL #2 @ 2000
        // Quantity = 40
        // -------------------------------------------------

        ResultSet tradeResult2 =
                DatabaseUtils.getTradeByOrderIds(
                        buyOrderId,
                        sellOrderId2
                );

        assertTrue(
                tradeResult2.next(),
                "Trade #2 should exist"
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
                "AAPL",
                tradeResult2.getString("symbol")
        );

        assertEquals(
                2000.0,
                tradeResult2.getDouble("price")
        );

        assertEquals(
                40,
                tradeResult2.getInt("quantity")
        );

        tradeResult2.close();

        // -------------------------------------------------
        // 11. Verify Trade #3
        // SELL #3 @ 2000
        // Quantity = 30
        // -------------------------------------------------

        ResultSet tradeResult3 =
                DatabaseUtils.getTradeByOrderIds(
                        buyOrderId,
                        sellOrderId3
                );

        assertTrue(
                tradeResult3.next(),
                "Trade #3 should exist"
        );

        assertEquals(
                buyOrderId,
                tradeResult3.getLong("buy_order_id")
        );

        assertEquals(
                sellOrderId3,
                tradeResult3.getLong("sell_order_id")
        );

        assertEquals(
                "AAPL",
                tradeResult3.getString("symbol")
        );

        assertEquals(
                2000.0,
                tradeResult3.getDouble("price")
        );

        assertEquals(
                30,
                tradeResult3.getInt("quantity")
        );

        tradeResult3.close();

        // -------------------------------------------------
        // 12. Verify BUY status history
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
                "BUY should contain FILLED status"
        );

        // -------------------------------------------------
        // 13. Verify SELL #1 status history
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
        // 14. Verify SELL #2 status history
        // -------------------------------------------------

        List<String> sellStatuses2 =
                DatabaseUtils.getOrderStatusHistory(
                        sellOrderId2
                );

        System.out.println(
                "SELL #2 Status History: " + sellStatuses2
        );

        assertTrue(
                sellStatuses2.contains("FILLED"),
                "SELL #2 should contain FILLED status"
        );

        // -------------------------------------------------
        // 15. Verify SELL #3 status history
        // -------------------------------------------------

        List<String> sellStatuses3 =
                DatabaseUtils.getOrderStatusHistory(
                        sellOrderId3
                );

        System.out.println(
                "SELL #3 Status History: " + sellStatuses3
        );

        assertTrue(
                sellStatuses3.contains("PARTIALLY_FILLED"),
                "SELL #3 should contain PARTIALLY_FILLED status"
        );
    }
}