package com.ashok.hft.service;

import com.ashok.hft.entity.Order;
import com.ashok.hft.entity.Trade;
import com.ashok.hft.enums.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExchangeSimulatorService {

    private final OrderStatusService statusService;
    private final MatchingEngineService matchingEngineService;

    public ExchangeSimulatorService(
            OrderStatusService statusService,
            MatchingEngineService matchingEngineService) {

        this.statusService = statusService;
        this.matchingEngineService = matchingEngineService;
    }

    @Transactional
    public void send(Order order) {

        statusService.updateStatus(
                order,
                OrderStatus.SENT_TO_EXCHANGE
        );

        List<Order> matchingOrders =
                matchingEngineService.findMatchingOrders(order);

        if (matchingOrders.isEmpty()) {

            statusService.updateStatus(
                    order,
                    OrderStatus.ACCEPTED
            );

            return;
        }

        for (Order oppositeOrder : matchingOrders) {

            if (order.getQuantity() <= 0) {
                break;
            }

            if (!matchingEngineService.canMatch(
                    order,
                    oppositeOrder)) {

                break;
            }

            Trade trade =
                    matchingEngineService.executeMatch(
                            order,
                            oppositeOrder
                    );

            if (trade == null) {
                continue;
            }
        }

        if (order.getQuantity() > 0
                && order.getStatus() != OrderStatus.PARTIALLY_FILLED) {

            statusService.updateStatus(
                    order,
                    OrderStatus.ACCEPTED
            );
        }
    }
}