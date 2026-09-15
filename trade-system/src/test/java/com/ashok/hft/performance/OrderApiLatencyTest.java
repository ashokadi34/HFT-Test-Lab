package com.ashok.hft.performance;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * PERF-004
 *
 * Measures sequential Order API response latency
 * and establishes a baseline for API performance.
 */
@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
class OrderApiLatencyTest {

    @LocalServerPort
    private int port;


    // ---------------------------------------------------------
    // PERF-004: Order API Latency Benchmark
    // ---------------------------------------------------------

    @Test
    void shouldMeasureOrderApiLatency() throws Exception {

        int totalRequests = 50;


        PerformanceMetrics metrics =
                new PerformanceMetrics();


        OrderApiPerformanceClient client =
                new OrderApiPerformanceClient(
                        "http://localhost:" + port
                );


        long testStart =
                System.nanoTime();


        // -----------------------------------------------------
        // Send requests sequentially
        // -----------------------------------------------------

        for (int i = 0;
             i < totalRequests;
             i++) {

            double price =
                    100.00
                            + (i * 0.001);


            OrderApiPerformanceClient.OrderResponse
                    response =
                    client.createOrder(
                            "TESTB",
                            price,
                            10,
                            "BUY"
                    );


            boolean successful =
                    response.statusCode() == 200;


            metrics.recordResponse(
                    response.responseTimeMillis(),
                    successful
            );


            // -------------------------------------------------
            // Validate HTTP response
            // -------------------------------------------------

            assertEquals(
                    200,
                    response.statusCode(),
                    "Order API should return HTTP 200"
            );


            // -------------------------------------------------
            // Validate Order ID
            // -------------------------------------------------

            String orderId =
                    extractOrderId(
                            response.body()
                    );


            assertTrue(
                    orderId != null
                            && !orderId.isBlank(),
                    "Successful response should contain "
                            + "an order ID"
            );
        }


        long testEnd =
                System.nanoTime();


        long totalDurationMillis =
                (
                        testEnd - testStart
                ) / 1_000_000;


        metrics.setTotalDurationMillis(
                totalDurationMillis
        );


        // -----------------------------------------------------
        // Print latency report
        // -----------------------------------------------------

        metrics.printReport(
                "PERF-004 ORDER API LATENCY BENCHMARK - "
                        + totalRequests
                        + " REQUESTS"
        );
    }


    // ---------------------------------------------------------
    // Extract Order ID from API response
    // ---------------------------------------------------------

    private String extractOrderId(
            String responseBody) {

        String marker =
                "\"id\"";


        int markerIndex =
                responseBody.indexOf(
                        marker
                );


        if (markerIndex < 0) {
            return null;
        }


        int colonIndex =
                responseBody.indexOf(
                        ":",
                        markerIndex
                );


        if (colonIndex < 0) {
            return null;
        }


        int start =
                colonIndex + 1;


        int end =
                responseBody.indexOf(
                        ",",
                        start
                );


        if (end < 0) {

            end =
                    responseBody.indexOf(
                            "}",
                            start
                    );
        }


        if (end < 0) {
            return null;
        }


        return responseBody
                .substring(
                        start,
                        end
                )
                .trim()
                .replace(
                        "\"",
                        ""
                );
    }
}