package com.ashok.hft.performance;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OrderApiPerformanceClient {

    private final HttpClient httpClient;

    private final String baseUrl;


    public OrderApiPerformanceClient(
            String baseUrl) {

        this.baseUrl = baseUrl;

        this.httpClient =
                HttpClient.newHttpClient();
    }


    // ---------------------------------------------------------
    // Send Order API request
    // ---------------------------------------------------------

    public OrderResponse createOrder(
            String symbol,
            double price,
            int quantity,
            String side)
            throws Exception {

        String requestBody =
                """
                {
                    "symbol": "%s",
                    "price": %s,
                    "quantity": %d,
                    "side": "%s"
                }
                """.formatted(
                        symbol,
                        price,
                        quantity,
                        side
                );


        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        baseUrl
                                                + "/api/orders"
                                )
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(
                                                requestBody
                                        )
                        )
                        .build();


        long start =
                System.nanoTime();


        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers
                                .ofString()
                );


        long end =
                System.nanoTime();


        long responseTimeMillis =
                (
                        end - start
                ) / 1_000_000;


        return new OrderResponse(
                response.statusCode(),
                response.body(),
                responseTimeMillis
        );
    }


    // ---------------------------------------------------------
    // Response model
    // ---------------------------------------------------------

    public record OrderResponse(
            int statusCode,
            String body,
            long responseTimeMillis) {
    }
}