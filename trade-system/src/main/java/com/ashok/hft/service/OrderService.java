package com.ashok.hft.service;

import com.ashok.hft.entity.Order;
import com.ashok.hft.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order createOrder(Order order) {

        order.setStatus("NEW");
        order.setCreatedTime(LocalDateTime.now());

        return repository.save(order);
    }

    public List<Order> getOrders() {
        return repository.findAll();
    }
}