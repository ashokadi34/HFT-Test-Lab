package com.ashok.hft.service;

import com.ashok.hft.entity.Order;
import com.ashok.hft.enums.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class OrderProcessorService {

    private final OrderStatusService statusService;
    private final ExchangeSimulatorService exchangeSimulatorService;

    public OrderProcessorService(OrderStatusService statusService,
                                 ExchangeSimulatorService exchangeSimulatorService) {

        this.statusService = statusService;
        this.exchangeSimulatorService = exchangeSimulatorService;
    }

    private static final Set<String> VALID_SYMBOLS = Set.of(
            "INFY",
            "TCS",
            "RELIANCE",
            "SBIN",
            "HDFCBANK",
            "ICICIBANK",
            "WIPRO",
            "LT",
            "AAPL",
            "ASUS",
            "MSFT",
            "DJI",
            "GOOGLE"
    );

    public void process(Order order) {

        statusService.updateStatus(
                order,
                OrderStatus.VALIDATING
        );

        String symbol = order.getSymbol().trim().toUpperCase();

        // Normalize symbol before further processing
        order.setSymbol(symbol);

        if (!VALID_SYMBOLS.contains(symbol)) {

            statusService.updateStatus(
                    order,
                    OrderStatus.REJECTED
            );

            return;
        }

        if (order.getPrice() == null || order.getPrice() <= 0) {

            statusService.updateStatus(
                    order,
                    OrderStatus.REJECTED
            );

            return;
        }

        if (order.getQuantity() == null || order.getQuantity() <= 0) {

            statusService.updateStatus(
                    order,
                    OrderStatus.REJECTED
            );

            return;
        }

        statusService.updateStatus(
                order,
                OrderStatus.ACCEPTED
        );

        exchangeSimulatorService.send(order);
    }
}