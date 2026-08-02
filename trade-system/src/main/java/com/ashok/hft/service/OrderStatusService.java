package com.ashok.hft.service;

import com.ashok.hft.entity.Order;
import com.ashok.hft.enums.OrderStatus;
import com.ashok.hft.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusService {

    private final OrderRepository repository;

    public OrderStatusService(OrderRepository repository) {
        this.repository = repository;
    }

    public void updateStatus(Order order, OrderStatus status) {

        order.setStatus(status);
        repository.save(order);

    }
}