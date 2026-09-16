package com.ashok.hft.service;

import com.ashok.hft.entity.Order;
import com.ashok.hft.entity.Trade;
import com.ashok.hft.enums.OrderSide;
import com.ashok.hft.enums.OrderStatus;
import com.ashok.hft.repository.OrderRepository;
import com.ashok.hft.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class MatchingEngineService {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final OrderStatusService statusService;

    public MatchingEngineService(
            OrderRepository orderRepository,
            TradeRepository tradeRepository,
            OrderStatusService statusService) {

        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.statusService = statusService;
    }

    public List<Order> findMatchingOrders(Order incomingOrder) {

        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.ACCEPTED,
                OrderStatus.PARTIALLY_FILLED
        );

        List<Order> matchingOrders;

        if (incomingOrder.getSide() == OrderSide.BUY) {

            matchingOrders =
                    orderRepository
                            .findBySymbolIgnoreCaseAndSideAndStatusInOrderByPriceAscCreatedTimeAscIdAsc(
                                    incomingOrder.getSymbol(),
                                    OrderSide.SELL,
                                    activeStatuses
                            );

        } else {

            matchingOrders =
                    orderRepository
                            .findBySymbolIgnoreCaseAndSideAndStatusInOrderByPriceDescCreatedTimeAscIdAsc(
                                    incomingOrder.getSymbol(),
                                    OrderSide.BUY,
                                    activeStatuses
                            );
        }

        /*
         * Enforce price-time priority explicitly in Java as well.
         *
         * BUY:
         *   Best SELL = lowest price first
         *
         * SELL:
         *   Best BUY = highest price first
         *
         * If price and createdTime are equal,
         * lower order ID wins as the deterministic FIFO fallback.
         */
        if (incomingOrder.getSide() == OrderSide.BUY) {

            matchingOrders.sort(
                    Comparator
                            .comparing(Order::getPrice)
                            .thenComparing(Order::getCreatedTime)
                            .thenComparing(Order::getId)
            );

        } else {

            matchingOrders.sort(
                    Comparator
                            .comparing(Order::getPrice)
                            .reversed()
                            .thenComparing(Order::getCreatedTime)
                            .thenComparing(Order::getId)
            );
        }

        return matchingOrders;

    }

    public boolean canMatch(
            Order incomingOrder,
            Order oppositeOrder) {

        return isPriceMatch(
                incomingOrder,
                oppositeOrder
        );
    }

    private boolean isPriceMatch(
            Order incomingOrder,
            Order oppositeOrder) {

        if (incomingOrder.getSide() == OrderSide.BUY) {

            return incomingOrder.getPrice()
                    >= oppositeOrder.getPrice();
        }

        return incomingOrder.getPrice()
                <= oppositeOrder.getPrice();
    }

    private int calculateMatchQuantity(
            Order incomingOrder,
            Order oppositeOrder) {

        return Math.min(
                incomingOrder.getQuantity(),
                oppositeOrder.getQuantity()
        );
    }

    public int getMatchQuantity(
            Order incomingOrder,
            Order oppositeOrder) {

        return calculateMatchQuantity(
                incomingOrder,
                oppositeOrder
        );
    }

    private Trade createTrade(
            Order incomingOrder,
            Order oppositeOrder,
            int matchQuantity) {

        Order buyOrder;
        Order sellOrder;

        if (incomingOrder.getSide() == OrderSide.BUY) {

            buyOrder = incomingOrder;
            sellOrder = oppositeOrder;

        } else {

            buyOrder = oppositeOrder;
            sellOrder = incomingOrder;
        }

        return new Trade(
                buyOrder.getId(),
                sellOrder.getId(),
                incomingOrder.getSymbol(),
                oppositeOrder.getPrice(),
                matchQuantity,
                java.time.LocalDateTime.now()
        );
    }

    public Trade buildTrade(
            Order incomingOrder,
            Order oppositeOrder) {

        if (!isPriceMatch(
                incomingOrder,
                oppositeOrder)) {

            return null;
        }

        int matchQuantity =
                calculateMatchQuantity(
                        incomingOrder,
                        oppositeOrder
                );

        if (matchQuantity <= 0) {
            return null;
        }

        return createTrade(
                incomingOrder,
                oppositeOrder,
                matchQuantity
        );
    }

    private void updateOrderAfterTrade(
            Order order,
            int executedQuantity) {

        int remainingQuantity =
                order.getQuantity()
                        - executedQuantity;

        order.setQuantity(
                remainingQuantity
        );

        if (remainingQuantity == 0) {

            order.setStatus(
                    OrderStatus.FILLED
            );

        } else {

            order.setStatus(
                    OrderStatus.PARTIALLY_FILLED
            );
        }
    }

    public Trade executeMatch(
            Order incomingOrder,
            Order oppositeOrder) {

        if (!isPriceMatch(
                incomingOrder,
                oppositeOrder)) {

            return null;
        }

        int matchQuantity =
                calculateMatchQuantity(
                        incomingOrder,
                        oppositeOrder
                );

        if (matchQuantity <= 0) {
            return null;
        }

        Trade trade =
                createTrade(
                        incomingOrder,
                        oppositeOrder,
                        matchQuantity
                );

        // Persist trade
        tradeRepository.save(trade);

        // Update remaining quantities and statuses
        updateOrderAfterTrade(
                incomingOrder,
                matchQuantity
        );

        updateOrderAfterTrade(
                oppositeOrder,
                matchQuantity
        );

        // Persist updated orders
        orderRepository.save(
                incomingOrder
        );

        orderRepository.save(
                oppositeOrder
        );

        // Persist status changes + audit history
        statusService.updateStatus(
                incomingOrder,
                incomingOrder.getStatus()
        );

        statusService.updateStatus(
                oppositeOrder,
                oppositeOrder.getStatus()
        );

        return trade;
    }
}