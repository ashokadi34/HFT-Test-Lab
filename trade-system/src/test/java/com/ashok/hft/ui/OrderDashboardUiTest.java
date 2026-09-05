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
}