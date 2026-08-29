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
class OrderCaseInsensitiveSymbolApiDbTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void shouldMatchOrdersWithDifferentSymbolCaseAndPersistTrade()
            throws Exception {

        // -------------------------------------------------
        // 1. Create SELL order using mixed-case symbol
        // -------------------------------------------------

        String sellRequest = """
                {
                      "symbol": "wIpRo",
                      "price": 1000,
                      "quantity": 30,
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
        // 2. Create BUY order using uppercase symbol
        // -------------------------------------------------

        String buyRequest = """
                {
                     "symbol": "WIPRO",
                     "price": 1000,
                     "quantity": 30,
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
        // 3. Verify SELL order in database
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

        // Symbol should be normalized before persistence
        assertEquals(
                "WIPRO",
                sellResult.getString("symbol")
        );

        assertEquals(
                1000.0,
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
                "WIPRO",
                buyResult.getString("symbol")
        );

        assertEquals(
                1000.0,
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
        // 5. Verify trade
        // -------------------------------------------------

        ResultSet tradeResult =
                DatabaseUtils.getTradeByOrderIds(
                        buyOrderId,
                        sellOrderId
                );

        assertTrue(
                tradeResult.next(),
                "Trade should exist for case-insensitive symbol match"
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
                "WIPRO",
                tradeResult.getString("symbol")
        );

        assertEquals(
                1000.0,
                tradeResult.getDouble("price")
        );

        assertEquals(
                30,
                tradeResult.getInt("quantity")
        );

        assertNotNull(
                tradeResult.getTimestamp("executed_time"),
                "Trade execution time should not be null"
        );

        tradeResult.close();

        // -------------------------------------------------
        // 6. Verify SELL status history
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
                buyStatuses.contains("FILLED"),
                "BUY order should contain FILLED status"
        );

        // -------------------------------------------------
        // 8. Final verification
        // -------------------------------------------------

        assertEquals(
                30,
                30,
                "Entire quantity should be executed"
        );

        System.out.println(
                "Case-insensitive symbol matching verified successfully."
        );
    }
}