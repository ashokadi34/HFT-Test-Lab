package com.ashok.hft.dto;

import java.time.LocalDateTime;

public class TradeResponse {

    private Long id;
    private Long buyOrderId;
    private Long sellOrderId;
    private String symbol;
    private double price;
    private int quantity;
    private LocalDateTime executedTime;

    public TradeResponse() {
    }

    public TradeResponse(
            Long id,
            Long buyOrderId,
            Long sellOrderId,
            String symbol,
            double price,
            int quantity,
            LocalDateTime executedTime) {

        this.id = id;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.executedTime = executedTime;
    }

    public Long getId() {
        return id;
    }

    public Long getBuyOrderId() {
        return buyOrderId;
    }

    public Long getSellOrderId() {
        return sellOrderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getExecutedTime() {
        return executedTime;
    }
}