package com.ashok.hft.service;

import com.ashok.hft.dto.OrderBookLevelResponse;
import com.ashok.hft.entity.Order;
import com.ashok.hft.enums.OrderSide;
import com.ashok.hft.enums.OrderStatus;
import com.ashok.hft.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderBookService {

    private final OrderRepository repository;

    public OrderBookService(OrderRepository repository) {
        this.repository = repository;
    }

    public List<OrderBookLevelResponse> getOrderBook(String symbol) {

        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.ACCEPTED,
                OrderStatus.PARTIALLY_FILLED
        );

        List<Order> orders =
                repository.findBySymbolIgnoreCaseAndStatusIn(
                        symbol,
                        activeStatuses
                );

        return orders.stream()
                .collect(Collectors.groupingBy(
                        order -> new PriceSideKey(
                                order.getPrice(),
                                order.getSide()
                        ),
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> {

                    PriceSideKey key = entry.getKey();

                    List<Order> groupedOrders = entry.getValue();

                    int totalQuantity = groupedOrders.stream()
                            .mapToInt(Order::getQuantity)
                            .sum();

                    long orderCount = groupedOrders.size();

                    return new OrderBookLevelResponse(
                            key.price(),
                            totalQuantity,
                            orderCount,
                            key.side()
                    );
                })
                .sorted((a, b) -> {

                    if (a.getSide() != b.getSide()) {
                        return a.getSide() == OrderSide.BUY ? -1 : 1;
                    }

                    return Double.compare(
                            b.getPrice(),
                            a.getPrice()
                    );
                })
                .collect(Collectors.toList());
    }

    private record PriceSideKey(
            Double price,
            OrderSide side
    ) {
    }
}