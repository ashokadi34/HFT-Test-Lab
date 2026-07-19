package com.ashok.hft.controller;

import com.ashok.hft.dto.OrderRequest;
import com.ashok.hft.dto.OrderResponse;
import com.ashok.hft.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return service.createOrder(request);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return service.getOrders();
    }
}