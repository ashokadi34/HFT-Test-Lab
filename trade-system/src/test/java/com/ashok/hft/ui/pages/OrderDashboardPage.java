package com.ashok.hft.ui.pages;

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

        page.locator("#submitOrder")
                .click();
    }

    public void waitForOrderMessage() {

        page.locator("#orderMessage")
                .waitFor();

        page.waitForFunction(
                "() => document.querySelector('#orderMessage')?.textContent.includes('created successfully')"
        );
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

    public int getOrderBookRowCount() {

        return page.locator(
                "#orderBookBody tr"
        ).count();
    }
}