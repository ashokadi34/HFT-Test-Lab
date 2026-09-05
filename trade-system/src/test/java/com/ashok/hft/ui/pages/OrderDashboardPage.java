package com.ashok.hft.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class OrderDashboardPage {

    private final Page page;

    public OrderDashboardPage(Page page) {
        this.page = page;
    }

    public void open(String baseUrl) {

        page.navigate(baseUrl + "/");
    }

    public void enterSymbol(String symbol) {

        page.locator("#symbol")
                .fill(symbol);
    }

    public void enterPrice(double price) {

        page.locator("#price")
                .fill(String.valueOf(price));
    }

    public void enterQuantity(int quantity) {

        page.locator("#quantity")
                .fill(String.valueOf(quantity));
    }

    public void selectSide(String side) {

        page.locator("#side")
                .selectOption(side);
    }

    public void submitOrder() {

        page.locator("#orderForm")
                .locator("button[type='submit']")
                .click();

        page.waitForTimeout(1500);
    }

    public void waitForOrderMessage() {

        page.waitForTimeout(1000);
    }

    public String getOrderMessage() {

        return page.locator("#orderMessage")
                .innerText();
    }

    public void waitForOrderInTable(String orderId) {

        page.locator("#ordersTable tbody tr")
                .filter(
                        new com.microsoft.playwright.Locator.FilterOptions()
                                .setHasText(orderId)
                )
                .first()
                .waitFor();
    }

    public void refreshOrders() {

        page.locator("#refreshOrders")
                .click();
    }

    public void refreshOrderBook(String symbol) {

        page.locator("#bookSymbol")
                .fill(symbol);

        page.locator("#refreshBook")
                .click();
    }

    public int getOrderRowCount() {

        return page.locator(
                "#ordersBody tr"
        ).count();
    }

    public void enterOrderBookSymbol(String symbol) {

        page.locator("#bookSymbol")
                .fill(symbol);
    }


    public void selectOrderBookSide(String side) {

        page.locator("#orderBookSideFilter")
                .selectOption(side);
    }


    public void enterOrderBookMinPrice(double price) {

        page.locator("#orderBookMinPrice")
                .fill(String.valueOf(price));
    }


    public void enterOrderBookMaxPrice(double price) {

        page.locator("#orderBookMaxPrice")
                .fill(String.valueOf(price));
    }


    public void clickRefreshOrderBook() {

        page.locator("#refreshBook")
                .click();
    }


    public void waitForOrderBookToLoad() {

        page.locator("#orderBookBody")
                .waitFor();

        page.waitForFunction(
                "() => document.querySelector('#orderBookBody')?.innerText.trim().length > 0"
        );
    }


    public int getOrderBookRowCount() {

        return page.locator(
                "#orderBookBody tr"
        ).count();
    }


    public String getOrderBookText() {

        return page.locator(
                "#orderBookBody"
        ).innerText();
    }


    public void clearOrderBookFilters() {

        page.locator("#orderBookSideFilter")
                .selectOption("");

        page.locator("#orderBookMinPrice")
                .fill("");

        page.locator("#orderBookMaxPrice")
                .fill("");
    }


    public Locator getOrderBookRows() {

        return page.locator(
                "#orderBookBody tr"
        );
    }

    public String getOrderBookApiResponse(String symbol) {

        return (String) page.evaluate(
                """
                async (symbol) => {
                    const response =
                        await fetch(
                            `/api/order-book?symbol=${encodeURIComponent(symbol)}`
                        );
    
                    return await response.text();
                }
                """,
                symbol
        );
    }

    public void waitForOrderBookToContain(String text) {

        page.waitForFunction(
                """
                (expected) => {
                    const body =
                        document.querySelector("#orderBookBody");
    
                    return body &&
                           body.innerText.includes(expected);
                }
                """,
                text
        );
    }

    public void waitForOrderBookNotToContain(String text) {

        page.waitForFunction(
                """
                (unexpected) => {
                    const body =
                        document.querySelector("#orderBookBody");
    
                    return body &&
                           !body.innerText.includes(unexpected);
                }
                """,
                text
        );
    }

}