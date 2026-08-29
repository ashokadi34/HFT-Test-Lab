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


    @Test
    void shouldDisplayTradingDashboard() {

        assertEquals(
                "HFT Test Lab",
                page.locator("h1")
                        .textContent()
        );

        assertTrue(
                page.locator("#orderForm")
                        .isVisible()
        );

        assertTrue(
                page.locator("#ordersTable")
                        .isVisible()
        );

        assertTrue(
                page.locator("#orderBookTable")
                        .isVisible()
        );
    }


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

        assertTrue(
                dashboard.getOrderMessage()
                        .contains("created successfully"),
                "Order creation success message should be displayed"
        );
    }
}