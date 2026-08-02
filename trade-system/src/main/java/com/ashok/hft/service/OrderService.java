package com.ashok.hft.service;

import com.ashok.hft.dto.OrderRequest;
import com.ashok.hft.dto.OrderResponse;
import com.ashok.hft.entity.Order;
import com.ashok.hft.enums.OrderStatus;
import com.ashok.hft.repository.OrderRepository;
import com.ashok.hft.validator.OrderValidator;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final ModelMapper modelMapper;
    private final OrderProcessorService processor;

    public OrderService(OrderRepository repository,
                        ModelMapper modelMapper,
                        OrderProcessorService processor) {

        this.repository = repository;
        this.modelMapper = modelMapper;
        this.processor = processor;
    }

    public OrderResponse createOrder(OrderRequest request) {

       // validator.validate(request);
        Order order = modelMapper.map(request, Order.class);

        order.setStatus(OrderStatus.NEW);
        order.setCreatedTime(LocalDateTime.now());
        processor.process(order);
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