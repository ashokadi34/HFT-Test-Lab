package com.ashok.hft.service;

import com.ashok.hft.entity.Order;
import com.ashok.hft.entity.OrderStatusHistory;
import com.ashok.hft.enums.OrderStatus;
import com.ashok.hft.repository.OrderRepository;
import com.ashok.hft.repository.OrderStatusHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    public OrderStatusService(OrderRepository orderRepository,
                              OrderStatusHistoryRepository historyRepository) {

        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
    }

    public void updateStatus(Order order, OrderStatus status) {

        // Update current order status
        order.setStatus(status);
        orderRepository.save(order);

        // Create audit history
        OrderStatusHistory history = new OrderStatusHistory();

        history.setOrderId(order.getId());
        history.setStatus(status);
        history.setUpdatedTime(LocalDateTime.now());

        historyRepository.save(history);
    }
}