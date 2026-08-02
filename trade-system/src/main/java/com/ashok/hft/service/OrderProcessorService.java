package com.ashok.hft.service;

import com.ashok.hft.entity.Order;
import com.ashok.hft.enums.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class OrderProcessorService {

    /*
     * Supported exchange symbols
     */
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
            "GOOGLE"
    );

    public void process(Order order) {

        /*
         * Validate symbol
         */
        String symbol = order.getSymbol().trim().toUpperCase();
        if (!VALID_SYMBOLS.contains(symbol)) {
            order.setStatus(OrderStatus.REJECTED);
            return;
        }

        /*
         * Validate price
         */
        if (order.getPrice() <= 0) {

            order.setStatus(OrderStatus.REJECTED);
            return;
        }

        /*
         * Validate quantity
         */
        if (order.getQuantity() <= 0) {

            order.setStatus(OrderStatus.REJECTED);
            return;
        }

        /*
         * Order accepted
         */
        order.setStatus(OrderStatus.ACCEPTED);
    }
}