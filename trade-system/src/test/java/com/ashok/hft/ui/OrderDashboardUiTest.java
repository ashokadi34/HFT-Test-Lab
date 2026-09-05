package com.ashok.hft.ui;

import com.ashok.hft.ui.pages.OrderDashboardPage;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import org.junit.jupiter.api.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
class OrderDashboardUiTest {

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

        playwright =
                Playwright.create();
    }


    @BeforeEach
    void setUp() {

        browser =
                playwright.chromium()
                        .launch(
                                new BrowserType
                                        .LaunchOptions()
                                        .setHeadless(true)
                        );

        page =
                browser.newPage();

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

//    private static final int UI_ACTION_DELAY =
//            Integer.parseInt(
//                    System.getProperty("ui.delay", "1500")
//            );
//
//    private void pauseForUi() {
//        page.waitForTimeout(UI_ACTION_DELAY);
//    }

    private void createOrder(
            String symbol,
            double price,
            int quantity,
            String side) {

        dashboard.enterSymbol(symbol);

        dashboard.enterPrice(price);

        dashboard.enterQuantity(quantity);

        dashboard.selectSide(side);

        dashboard.submitOrder();

        dashboard.waitForOrderMessage();

        String message =
                dashboard.getOrderMessage();

        assertTrue(
                message.contains("created successfully"),
                "Order should be created successfully: "
                        + message
        );
    }

    // ---------------------------------------------------------
    // UI-001: Dashboard should load successfully
    // ---------------------------------------------------------

    @Test
    void shouldDisplayTradingDashboard() {

        assertEquals(
                "HFT Test Lab",
                page.locator("h1")
                        .textContent()
        );

        assertTrue(
                page.locator("#orderForm")
                        .isVisible(),
                "Order form should be visible"
        );

        assertTrue(
                page.locator("#ordersTable")
                        .isVisible(),
                "Orders table should be visible"
        );

        assertTrue(
                page.locator("#orderBookTable")
                        .isVisible(),
                "Order book table should be visible"
        );
    }


    // ---------------------------------------------------------
    // UI-002: Create BUY order
    // ---------------------------------------------------------

    @Test
    void shouldCreateBuyOrderThroughUi() {

        double testPrice =
                100000 + (System.currentTimeMillis() % 10000);

        dashboard.enterSymbol("INFY");

        dashboard.enterPrice(testPrice);

        dashboard.enterQuantity(10);

        dashboard.selectSide("BUY");

        dashboard.submitOrder();

        dashboard.waitForOrderMessage();

        String message =
                dashboard.getOrderMessage();

        assertTrue(
                message.contains("created successfully"),
                "Unexpected order message: [" + message + "]"
        );
    }


    // ---------------------------------------------------------
    // UI-003: Create SELL order
    // ---------------------------------------------------------

    @Test
    void shouldCreateSellOrderThroughUi() {

        double testPrice =
                110000 + (System.currentTimeMillis() % 10000);

        dashboard.enterSymbol("INFY");

        dashboard.enterPrice(testPrice);

        dashboard.enterQuantity(5);

        dashboard.selectSide("SELL");

        dashboard.submitOrder();

        dashboard.waitForOrderMessage();

        String message =
                dashboard.getOrderMessage();

        assertTrue(
                message.contains("created successfully"),
                "Unexpected order message: [" + message + "]"
        );
    }


    // ---------------------------------------------------------
    // UI-004: Created order should appear in Orders table
    // ---------------------------------------------------------

    @Test
    void shouldDisplayCreatedOrderInOrdersTable() {

        double testPrice =
                120000 + (System.currentTimeMillis() % 10000);

        dashboard.enterSymbol("TCS");

        dashboard.enterPrice(testPrice);

        dashboard.enterQuantity(20);

        dashboard.selectSide("BUY");

        dashboard.submitOrder();

        dashboard.waitForOrderMessage();

        String message =
                dashboard.getOrderMessage();

        System.out.println(
                "UI Order Message: [" + message + "]"
        );

        assertTrue(
                message.contains("created successfully"),
                "Unexpected order message: [" + message + "]"
        );

        /*
         * Expected message:
         *
         * Order <ID> created successfully
         *
         * Extract the generated order ID instead of
         * hardcoding an ID.
         */
        String orderId =
                message.replaceAll(
                        ".*Order\\s+(\\d+)\\s+created successfully.*",
                        "$1"
                );

        assertTrue(
                orderId.matches("\\d+"),
                "Generated order ID should be present in success message"
        );

        /*
         * refreshOrders() is triggered by the application
         * after successful order creation.
         *
         * Locate the newly-created order using its ID.
         */
        dashboard.waitForOrderInTable(orderId);

        var orderRows =
                page.locator("#ordersTable tbody tr");

        assertTrue(
                orderRows
                        .filter(
                                new com.microsoft.playwright.Locator.FilterOptions()
                                        .setHasText(orderId)
                        )
                        .count() > 0,
                "Created order should appear in Orders table"
        );
    }


    // ---------------------------------------------------------
    // UI-005: Order Book should be displayed
    // ---------------------------------------------------------

    @Test
    void shouldDisplayOrderBook() {

        assertTrue(
                page.locator("#orderBookTable")
                        .isVisible(),
                "Order Book table should be visible"
        );

        assertTrue(
                page.locator("#orderBookTable thead")
                        .isVisible(),
                "Order Book table header should be visible"
        );
    }

    // ---------------------------------------------------------
    // UI-006: Load Order Book
    // ---------------------------------------------------------

    @Test
    void shouldLoadOrderBookForSymbol() {

        createOrder(
                "BATCH2",
                100000,
                10,
                "BUY"
        );

        dashboard.enterOrderBookSymbol("BATCH2");

        dashboard.clickRefreshOrderBook();

        dashboard.waitForOrderBookToLoad();

        String orderBook =
                dashboard.getOrderBookText();

        assertFalse(
                orderBook.contains(
                        "Unable to load order book"
                ),
                "Order book should load successfully"
        );
    }

    // ---------------------------------------------------------
    // UI-007: Filter by Symbol
    // ---------------------------------------------------------

    @Test
    void shouldFilterOrderBookBySymbol() {

        createOrder("TESTA", 100000, 10, "BUY");
        createOrder("TESTB", 110000, 5, "SELL");

        dashboard.enterOrderBookSymbol("TESTA");
        dashboard.clickRefreshOrderBook();
        dashboard.waitForOrderBookToLoad();

        String orderBook =
                dashboard.getOrderBookText();

        assertTrue(
                orderBook.contains("1,00,000"),
                "TESTA order-book price should be displayed"
        );

        assertFalse(
                orderBook.contains("1,10,000"),
                "TESTB price should not be displayed"
        );
    }

    // ---------------------------------------------------------
    // UI-008: BUY/SELL side filter
    // ---------------------------------------------------------

    @Test
    void shouldFilterOrderBookBySide() {

        createOrder("TESTC", 100000, 10, "BUY");
        createOrder("TESTC", 110000, 5, "SELL");

        dashboard.enterOrderBookSymbol("TESTC");
        dashboard.clickRefreshOrderBook();
        dashboard.waitForOrderBookToLoad();


        // =========================
        // BUY FILTER
        // =========================

        dashboard.selectOrderBookSide("BUY");

        dashboard.waitForOrderBookToContain("1,00,000");

        String buyBook =
                dashboard.getOrderBookText();

        assertTrue(
                buyBook.contains("1,00,000"),
                "BUY order-book price should be displayed"
        );

        assertFalse(
                buyBook.contains("1,10,000"),
                "SELL price should not be displayed when BUY filter is selected"
        );


        // =========================
        // SELL FILTER
        // =========================

        dashboard.selectOrderBookSide("SELL");

        dashboard.waitForOrderBookToContain("1,10,000");

        String sellBook =
                dashboard.getOrderBookText();

        assertTrue(
                sellBook.contains("1,10,000"),
                "SELL order-book price should be displayed"
        );

        assertFalse(
                sellBook.contains("1,00,000"),
                "BUY price should not be displayed when SELL filter is selected"
        );
    }

    // ---------------------------------------------------------
    // UI-009: Minimum price
    // ---------------------------------------------------------

    @Test
    void shouldFilterOrderBookByMinimumPrice() {

        createOrder(
                "TESTD",
                100000,
                10,
                "BUY"
        );

        createOrder(
                "TESTD",
                110000,
                5,
                "SELL"
        );

        dashboard.enterOrderBookSymbol(
                "TESTD"
        );

        dashboard.clickRefreshOrderBook();

        dashboard.waitForOrderBookToLoad();

        dashboard.enterOrderBookMinPrice(
                105000
        );

        dashboard.waitForOrderBookNotToContain(
                "1,00,000"
        );

        dashboard.waitForOrderBookToContain(
                "1,10,000"
        );

        String orderBook =
                dashboard.getOrderBookText();

        assertFalse(
                orderBook.contains("1,00,000"),
                "Price below minimum should not be displayed"
        );

        assertTrue(
                orderBook.contains("1,10,000"),
                "Price above minimum should be displayed"
        );
    }

    // ---------------------------------------------------------
// UI-010: Maximum price
// ---------------------------------------------------------

    @Test
    void shouldFilterOrderBookByMaximumPrice() {

        createOrder(
                "TESTE",
                100000,
                10,
                "BUY"
        );

        createOrder(
                "TESTE",
                110000,
                5,
                "SELL"
        );

        dashboard.enterOrderBookSymbol(
                "TESTE"
        );

        dashboard.clickRefreshOrderBook();

        dashboard.waitForOrderBookToLoad();

        dashboard.enterOrderBookMaxPrice(
                105000
        );

        dashboard.waitForOrderBookNotToContain(
                "1,10,000"
        );

        dashboard.waitForOrderBookToContain(
                "1,00,000"
        );

        String orderBook =
                dashboard.getOrderBookText();

        assertTrue(
                orderBook.contains("1,00,000"),
                "Price below maximum should be displayed"
        );

        assertFalse(
                orderBook.contains("1,10,000"),
                "Price above maximum should not be displayed"
        );
    }

    // ---------------------------------------------------------
// UI-011: Clear Order Book filters
// ---------------------------------------------------------

    @Test
    void shouldClearOrderBookFilters() {

        createOrder(
                "TESTG",
                100000,
                10,
                "BUY"
        );

        createOrder(
                "TESTG",
                110000,
                5,
                "SELL"
        );

        dashboard.enterOrderBookSymbol(
                "TESTG"
        );

        dashboard.clickRefreshOrderBook();

        dashboard.waitForOrderBookToLoad();

        // Apply filters
        dashboard.selectOrderBookSide("SELL");

        dashboard.enterOrderBookMinPrice(
                105000
        );

        dashboard.enterOrderBookMaxPrice(
                115000
        );

        // Verify filtered state
        dashboard.waitForOrderBookNotToContain(
                "1,00,000"
        );

        dashboard.waitForOrderBookToContain(
                "1,10,000"
        );

        // Clear filters
        dashboard.clearOrderBookFilters();

        // Verify both prices are displayed again
        dashboard.waitForOrderBookToContain(
                "1,00,000"
        );

        dashboard.waitForOrderBookToContain(
                "1,10,000"
        );

        String orderBook =
                dashboard.getOrderBookText();

        assertTrue(
                orderBook.contains("1,00,000"),
                "BUY price should be displayed after clearing filters"
        );

        assertTrue(
                orderBook.contains("1,10,000"),
                "SELL price should be displayed after clearing filters"
        );
    }

}