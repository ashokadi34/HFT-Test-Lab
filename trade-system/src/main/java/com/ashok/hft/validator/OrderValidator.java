package com.ashok.hft.validator;

import com.ashok.hft.dto.OrderRequest;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OrderValidator {

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

    public void validate(OrderRequest request) {

        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        if (request.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (!VALID_SYMBOLS.contains(request.getSymbol().toUpperCase())) {
            throw new IllegalArgumentException(
                    "Unsupported trading symbol: " + request.getSymbol()
            );
        }
    }
}