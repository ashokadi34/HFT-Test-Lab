package com.ashok.hft.service;

import com.ashok.hft.dto.OrderRequest;
import com.ashok.hft.dto.OrderResponse;
import com.ashok.hft.entity.Order;
import com.ashok.hft.enums.OrderStatus;
import com.ashok.hft.repository.OrderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final ModelMapper modelMapper;

    public OrderService(OrderRepository repository,
                        ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public OrderResponse createOrder(OrderRequest request) {

        Order order = modelMapper.map(request, Order.class);

        order.setStatus(OrderStatus.NEW);
        order.setCreatedTime(LocalDateTime.now());

        Order saved = repository.save(order);

        return modelMapper.map(saved, OrderResponse.class);
    }

    public List<OrderResponse> getOrders() {

        return repository.findAll()
                .stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
    }
}