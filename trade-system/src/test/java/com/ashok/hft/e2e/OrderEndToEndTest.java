package com.ashok.hft.e2e;

import com.ashok.hft.db.DatabaseUtils;
import com.ashok.hft.ui.pages.OrderDashboardPage;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.sql.ResultSet;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class OrderEndToEndTest {

    @LocalServerPort
    private int port;

    private static Playwright playwright;

    private Browser browser;

    private Page page;

    private OrderDashboardPage dashboard;


    // ---------------------------------------------------------
    // Test Setup
    // ---------------------------------------------------------

    @BeforeAll
    static void beforeAll() {

        playwright = Playwright.create();
    }


    @BeforeEach
    void setUp() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        browser =
                playwright.chromium()
                        .launch(
                                new BrowserType
                                        .LaunchOptions()
                                        .setHeadless(true)
                        );

        page = browser.newPage();

        dashboard =
                new OrderDashboardPage(page);

        dashboard.open(
                "http://localhost:" + port
        );
    }


    @AfterEach
    void tearDown() {

        browser.close();
    }


    @AfterAll
    static void afterAll() {

        playwright.close();
    }


    // ---------------------------------------------------------
    // E2E-001
    //
    // API → Database → Dashboard → Order Book
    // ---------------------------------------------------------

    @Test
    void shouldCreateOrderAndVerifyAcrossApiDatabaseAndUi()
            throws Exception {

        String symbol = "TESTM";

        /*
         * Use a very high price for the SELL order.
         *
         * This makes accidental matching with existing BUY
         * orders extremely unlikely.
         */
        double testPrice =
                9_000_000
                        + (System.nanoTime() % 1000) / 100.0;

        int quantity = 7;

        String requestBody = """
                {
                    "symbol": "%s",
                    "price": %s,
                    "quantity": %d,
                    "side": "SELL"
                }
                """.formatted(
                symbol,
                testPrice,
                quantity
        );


        // -----------------------------------------------------
        // 1. Create order through REST API
        // -----------------------------------------------------

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
                response.jsonPath()
                        .getLong("id");


        assertNotNull(
                orderId,
                "API should return a generated order ID"
        );


        assertEquals(
                symbol,
                response.jsonPath()
                        .getString("symbol")
        );


        assertEquals(
                "SELL",
                response.jsonPath()
                        .getString("side")
        );


        System.out.println(
                "E2E Order ID: " + orderId
        );

        System.out.println(
                "E2E Test Price: " + testPrice
        );


        // -----------------------------------------------------
        // 2. Verify order persistence in PostgreSQL
        // -----------------------------------------------------

        ResultSet orderResult =
                DatabaseUtils.getOrderById(orderId);


        assertTrue(
                orderResult.next(),
                "Created order should exist in PostgreSQL"
        );


        assertEquals(
                orderId,
                orderResult.getLong("id")
        );


        assertEquals(
                symbol,
                orderResult.getString("symbol")
        );


        assertEquals(
                testPrice,
                orderResult.getDouble("price"),
                0.001
        );


        assertEquals(
                quantity,
                orderResult.getInt("quantity")
        );


        assertEquals(
                "SELL",
                orderResult.getString("side")
        );


        assertNotNull(
                orderResult.getString("status"),
                "Order status should be persisted"
        );


        String databaseStatus =
                orderResult.getString("status");


        orderResult.close();


        System.out.println(
                "Database Status: " + databaseStatus
        );


        // -----------------------------------------------------
        // 3. Verify order through Dashboard UI
        // -----------------------------------------------------
        System.out.println("Starting - 3. Verify order through Dashboard UI");
        dashboard.refreshOrders();


        dashboard.waitForOrdersTableToContain(
                String.valueOf(orderId)
        );


        String ordersTable =
                dashboard.getOrdersTableText();


        assertTrue(
                ordersTable.contains(
                        String.valueOf(orderId)
                ),
                "Dashboard should display the created order"
        );


        assertTrue(
                ordersTable.contains(symbol),
                "Dashboard should display the order symbol"
        );

        System.out.println("Completed - 3. Verify order through Dashboard UI");


        // -----------------------------------------------------
        // 4. Verify order through Order Book
        // -----------------------------------------------------
        System.out.println("Starting - 4. Verify order through Order Book");
        dashboard.enterOrderBookSymbol(symbol);
        System.out.println("************* Entered - enterOrderBookSymbol");
        // Use an exact price range so our unique E2E order
        // is isolated from existing TESTM test data.
        dashboard.enterOrderBookMinPrice(testPrice);
        System.out.println("************* Entered - enterOrderBookMinPrice");
        dashboard.enterOrderBookMaxPrice(testPrice);
        System.out.println("************* Entered - enterOrderBookMaxPrice");

        dashboard.clickRefreshOrderBook();
        System.out.println("************* Clicked - clickRefreshOrderBook");

        dashboard.waitForOrderBookToLoad();
        System.out.println("************* Waited - waitForOrderBookToLoad");

        // Dashboard uses en-IN number formatting.
        // Example: 9000000 -> 90,00,000
        String priceString = String.valueOf((long) testPrice);

        String expectedPrice;

        if (priceString.length() <= 3) {

            expectedPrice = priceString;

        } else {

            String lastThree =
                    priceString.substring(priceString.length() - 3);

            String remaining =
                    priceString.substring(0, priceString.length() - 3);

            StringBuilder formatted =
                    new StringBuilder();

            while (remaining.length() > 2) {

                formatted.insert(
                        0,
                        "," + remaining.substring(remaining.length() - 2)
                );

                remaining =
                        remaining.substring(0, remaining.length() - 2);
            }

            formatted.insert(0, remaining);

            expectedPrice =
                    formatted + "," + lastThree;
        }
        System.out.println("************* expectedPrice :"+ expectedPrice);

        dashboard.waitForOrderBookToContain(
                expectedPrice
        );
        System.out.println("************* waitForOrderBookToContain: "+ expectedPrice);

        String orderBookText =
                dashboard.getOrderBookText();
        System.out.println("************* orderBookText :"+ orderBookText);

        assertTrue(
                orderBookText.contains(expectedPrice),
                "Order Book should display the created SELL order price: "
                        + expectedPrice
        );

        System.out.println("Completed - 4. Verify order through Order Book");


        // -----------------------------------------------------
        // E2E Summary
        // -----------------------------------------------------

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "E2E-001 PASSED"
        );

        System.out.println(
                "Order ID       : " + orderId
        );

        System.out.println(
                "Symbol         : " + symbol
        );

        System.out.println(
                "Side           : SELL"
        );

        System.out.println(
                "Quantity       : " + quantity
        );

        System.out.println(
                "Database Status: " + databaseStatus
        );

        System.out.println(
                "API            : PASS"
        );

        System.out.println(
                "Database       : PASS"
        );

        System.out.println(
                "Dashboard      : PASS"
        );

        System.out.println(
                "Order Book     : PASS"
        );

        System.out.println(
                "=========================================="
        );
    }
}