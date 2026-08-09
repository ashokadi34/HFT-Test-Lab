package com.ashok.hft.dto;

import com.ashok.hft.enums.OrderSide;

public class OrderBookLevelResponse {

    private Double price;

    private Integer quantity;

    private Long orderCount;

    private OrderSide side;

    public OrderBookLevelResponse() {
    }

    public OrderBookLevelResponse(Double price,
                                  Integer quantity,
                                  Long orderCount,
                                  OrderSide side) {
        this.price = price;
        this.quantity = quantity;
        this.orderCount = orderCount;
        this.side = side;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }
}