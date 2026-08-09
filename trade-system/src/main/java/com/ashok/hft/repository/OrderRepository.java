package com.ashok.hft.repository;

import com.ashok.hft.entity.Order;
import com.ashok.hft.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatusOrderByCreatedTimeDesc(OrderStatus status);

    List<Order> findBySymbolIgnoreCaseOrderByCreatedTimeDesc(String symbol);

}