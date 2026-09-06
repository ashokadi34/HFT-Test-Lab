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
// UI-008: Filter Order Book by BUY/SELL side
// ---------------------------------------------------------

    @Test
    void shouldFilterOrderBookBySide() {

        // ---------------------------------------------------------
        // Create unique prices for this test execution
        // ---------------------------------------------------------

        int lowPrice =
                130000
                        + (int)
                        (System.currentTimeMillis() % 5000);

        int highPrice =
                lowPrice + 5000;


        // ---------------------------------------------------------
        // Create BUY and SELL orders
        // ---------------------------------------------------------

        createOrder(
                "TESTK",
                lowPrice,
                10,
                "BUY"
        );

        createOrder(
                "TESTK",
                highPrice,
                5,
                "SELL"
        );


        // ---------------------------------------------------------
        // Load TESTK Order Book
        // ---------------------------------------------------------

        dashboard.enterOrderBookSymbol("TESTK");

        dashboard.clickRefreshOrderBook();

        dashboard.waitForOrderBookToLoad();

        // ---------------------------------------------------------
        // BUY FILTER
        // ---------------------------------------------------------

        dashboard.selectOrderBookSide("BUY");

        dashboard.waitForOrderBookToShowOnlySide("BUY");

        // ---------------------------------------------------------
        // SELL FILTER
        // ---------------------------------------------------------

        dashboard.selectOrderBookSide("SELL");

        dashboard.waitForOrderBookToShowOnlySide("SELL");

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

    // ---------------------------------------------------------
    // UI-012: Orders table Symbol filter
    // ---------------------------------------------------------

    @Test
    void shouldFilterOrdersTableBySymbol() {

        createOrder(
                "TESTG",
                100000,
                10,
                "BUY"
        );

        createOrder(
                "TESTH",
                110000,
                5,
                "SELL"
        );

        // Select TESTG from Orders table Symbol filter
        dashboard.selectOrderSymbolFilter("TESTG");

        // Wait until filtered table contains TESTG
        dashboard.waitForOrdersTableToContain("TESTG");

        // Wait until TESTH is removed from the filtered table
        dashboard.waitForOrdersTableNotToContain("TESTH");

        String ordersTable =
                dashboard.getOrdersTableText();

        assertTrue(
                ordersTable.contains("TESTG"),
                "TESTG order should be displayed after symbol filter"
        );

        assertFalse(
                ordersTable.contains("TESTH"),
                "TESTH order should not be displayed after TESTG symbol filter"
        );

        // Verify every displayed row belongs to TESTG
        var orderRows =
                page.locator("#ordersTable tbody tr");

        assertTrue(
                orderRows.count() > 0,
                "At least one order should be displayed"
        );

        for (int i = 0; i < orderRows.count(); i++) {

            String symbol =
                    orderRows
                            .nth(i)
                            .locator("td")
                            .nth(1)
                            .innerText();

            assertEquals(
                    "TESTG",
                    symbol,
                    "Every displayed order should belong to TESTG"
            );
        }
    }

    // ---------------------------------------------------------
// UI-013: Orders table Price sorting
// ---------------------------------------------------------

    @Test
    void shouldSortOrdersTableByPrice() {

        long uniqueBase =
                130000 + (System.currentTimeMillis() % 5000);

        int lowPrice =
                (int) uniqueBase;

        int highPrice =
                lowPrice + 5000;


        // Create two unique TESTI orders

        createOrder(
                "TESTI",
                lowPrice,
                10,
                "BUY"
        );

        createOrder(
                "TESTI",
                highPrice,
                5,
                "SELL"
        );


        // Filter by TESTI

        dashboard.selectOrderSymbolFilter("TESTI");

        dashboard.waitForOrdersTableToContainPrice(
                lowPrice
        );

        dashboard.waitForOrdersTableToContainPrice(
                highPrice
        );


        // ---------------------------------------------------------
        // Ascending
        // ---------------------------------------------------------

        dashboard.selectOrderPriceSort();

        dashboard.waitForPriceOrder(
                lowPrice,
                highPrice,
                true
        );


        // Verify actual positions

        int lowAscending =
                dashboard.findOrderPriceRow(lowPrice);

        int highAscending =
                dashboard.findOrderPriceRow(highPrice);


        assertTrue(
                lowAscending < highAscending,
                "Lower price should appear before higher price "
                        + "in ascending order"
        );


        // ---------------------------------------------------------
        // Descending
        // ---------------------------------------------------------

        dashboard.selectOrderPriceSort();

        dashboard.waitForPriceOrder(
                lowPrice,
                highPrice,
                false
        );


        int lowDescending =
                dashboard.findOrderPriceRow(lowPrice);

        int highDescending =
                dashboard.findOrderPriceRow(highPrice);


        assertTrue(
                highDescending < lowDescending,
                "Higher price should appear before lower price "
                        + "in descending order"
        );
    }

    // ---------------------------------------------------------
    // UI-014: Summary Cards + Order Analytics
    // ---------------------------------------------------------

    @Test
    void shouldUpdateSummaryCardsAndOrderAnalytics() {

        // ---------------------------------------------------------
        // Wait for initial dashboard data
        // ---------------------------------------------------------

        dashboard.waitForDashboardDataLoaded();

        int totalBefore =
                dashboard.getTotalOrders();

        int activeBefore =
                dashboard.getActiveOrders();

        // ---------------------------------------------------------
        // Create two valid TESTB orders
        // ---------------------------------------------------------

        createOrder(
                "TESTB",
                100000,
                10,
                "BUY"
        );

        createOrder(
                "TESTB",
                110000,
                5,
                "SELL"
        );

        // ---------------------------------------------------------
        // Explicitly refresh dashboard data
        // ---------------------------------------------------------

        dashboard.refreshOrders();

        page.waitForFunction(
                """
                (expected) => {
                    const element =
                        document.querySelector("#totalOrders");
    
                    return element &&
                           Number(element.innerText.trim()) >= expected;
                }
                """,
                totalBefore + 2
        );

        // ---------------------------------------------------------
        // Read updated summary cards
        // ---------------------------------------------------------

        int totalAfter =
                dashboard.getTotalOrders();

        int activeAfter =
                dashboard.getActiveOrders();

        // ---------------------------------------------------------
        // Summary Cards
        // ---------------------------------------------------------

        assertEquals(
                totalBefore + 2,
                totalAfter,
                "Total Orders should increase by 2"
        );

        assertEquals(
                activeBefore + 2,
                activeAfter,
                "Active Orders should increase by 2"
        );

        // ---------------------------------------------------------
        // Verify remaining summary cards are displayed
        // ---------------------------------------------------------

        assertTrue(
                dashboard.getFilledOrders() >= 0,
                "Filled Orders card should be displayed"
        );

        assertTrue(
                dashboard.getPartialOrders() >= 0,
                "Partial Orders card should be displayed"
        );

        assertTrue(
                dashboard.getRejectedOrders() >= 0,
                "Rejected Orders card should be displayed"
        );
    }
    // ---------------------------------------------------------
    // --------------- END -------------------
    // ---------------------------------------------------------

}