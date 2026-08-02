package com.ashok.hft.service;

import com.ashok.hft.entity.Order;
import com.ashok.hft.enums.OrderStatus;
import org.springframework.stereotype.Service;

@Service
public class ExchangeSimulatorService {

    private final OrderStatusService statusService;

    public ExchangeSimulatorService(OrderStatusService statusService) {
        this.statusService = statusService;
    }

    public void send(Order order) {

        try {

            statusService.updateStatus(order, OrderStatus.SENT_TO_EXCHANGE);

            Thread.sleep(1000);

            statusService.updateStatus(order, OrderStatus.PARTIALLY_FILLED);

            Thread.sleep(1000);

            statusService.updateStatus(order, OrderStatus.FILLED);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            throw new RuntimeException("Exchange simulation interrupted", e);

        }
    }
}