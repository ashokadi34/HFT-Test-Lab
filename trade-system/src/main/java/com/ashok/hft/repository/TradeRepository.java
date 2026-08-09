package com.ashok.hft.repository;

import com.ashok.hft.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findBySymbolIgnoreCaseOrderByExecutedTimeDesc(String symbol);

    List<Trade> findByBuyOrderIdOrSellOrderIdOrderByExecutedTimeDesc(
            Long buyOrderId,
            Long sellOrderId
    );
}