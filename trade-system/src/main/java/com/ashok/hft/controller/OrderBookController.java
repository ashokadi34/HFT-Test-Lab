package com.ashok.hft.controller;

import com.ashok.hft.dto.OrderBookLevelResponse;
import com.ashok.hft.service.OrderBookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order-book")
public class OrderBookController {

    private final OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @GetMapping
    public List<OrderBookLevelResponse> getOrderBook(
            @RequestParam String symbol) {

        return orderBookService.getOrderBook(symbol);
    }
}