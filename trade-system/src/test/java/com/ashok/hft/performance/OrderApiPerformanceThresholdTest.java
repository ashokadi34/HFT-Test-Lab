package com.ashok.hft.performance;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * PERF-005
 *
 * Validates that the Order API stays within the
 * established response-time performance threshold.
 */
@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
class OrderApiPerformanceThresholdTest {

    @LocalServerPort
    private int port;


    // ---------------------------------------------------------
    // Performance threshold
    // ---------------------------------------------------------

    private static final int TOTAL_REQUESTS = 50;

    private static final long P95_THRESHOLD_MILLIS = 300;


    // ---------------------------------------------------------
    // PERF-005: Validate P95 latency threshold
    // ---------------------------------------------------------

    @Test
    void shouldMeetOrderApiPerformanceThreshold() throws Exception {

        OrderApiPerformanceClient client =
                new OrderApiPerformanceClient(
                        "http://localhost:" + port
                );


        List<Long> responseTimes =
                new ArrayList<>();


        int successfulRequests = 0;


        long testStart =
                System.nanoTime();


        // -----------------------------------------------------
        // Send sequential requests
        // -----------------------------------------------------

        for (int i = 0;
             i < TOTAL_REQUESTS;
             i++) {

            double price =
                    200.00
                            + (i * 0.001);


            OrderApiPerformanceClient.OrderResponse
                    response =
                    client.createOrder(
                            "TESTC",
                            price,
                            10,
                            "BUY"
                    );


            assertEquals(
                    200,
                    response.statusCode(),
                    "Order API should return HTTP 200"
            );


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


            responseTimes.add(
                    response.responseTimeMillis()
            );


            successfulRequests++;
        }


        long testEnd =
                System.nanoTime();


        long totalDurationMillis =
                (
                        testEnd - testStart
                ) / 1_000_000;


        // -----------------------------------------------------
        // Calculate latency metrics
        // -----------------------------------------------------

        Collections.sort(
                responseTimes
        );


        long minimum =
                responseTimes.get(0);


        long maximum =
                responseTimes.get(
                        responseTimes.size() - 1
                );


        double average =
                responseTimes.stream()
                        .mapToLong(Long::longValue)
                        .average()
                        .orElse(0);


        long p50 =
                calculatePercentile(
                        responseTimes,
                        50
                );


        long p95 =
                calculatePercentile(
                        responseTimes,
                        95
                );


        long p99 =
                calculatePercentile(
                        responseTimes,
                        99
                );


        double throughput =
                TOTAL_REQUESTS
                        / (totalDurationMillis / 1000.0);


        // -----------------------------------------------------
        // Print performance report
        // -----------------------------------------------------

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                "PERF-005 ORDER API PERFORMANCE THRESHOLD"
        );
        System.out.println(
                "========================================"
        );

        System.out.println(
                "Total Requests       : "
                        + TOTAL_REQUESTS
        );

        System.out.println(
                "Successful Requests  : "
                        + successfulRequests
        );

        System.out.println(
                "Failed Requests      : "
                        + (
                        TOTAL_REQUESTS
                                - successfulRequests
                )
        );

        System.out.println(
                "Total Duration       : "
                        + totalDurationMillis
                        + " ms"
        );

        System.out.printf(
                "Average Response     : %.2f ms%n",
                average
        );

        System.out.println(
                "Minimum Response     : "
                        + minimum
                        + " ms"
        );

        System.out.println(
                "Maximum Response     : "
                        + maximum
                        + " ms"
        );

        System.out.println(
                "P50 Response         : "
                        + p50
                        + " ms"
        );

        System.out.println(
                "P95 Response         : "
                        + p95
                        + " ms"
        );

        System.out.println(
                "P99 Response         : "
                        + p99
                        + " ms"
        );

        System.out.printf(
                "Throughput           : %.2f req/sec%n",
                throughput
        );

        System.out.println(
                "P95 Threshold        : "
                        + P95_THRESHOLD_MILLIS
                        + " ms"
        );

        System.out.println(
                "Performance Gate     : "
                        + (
                        p95 <= P95_THRESHOLD_MILLIS
                                ? "PASS"
                                : "FAIL"
                )
        );

        System.out.println(
                "========================================"
        );


        // -----------------------------------------------------
        // Final performance assertions
        // -----------------------------------------------------

        assertEquals(
                TOTAL_REQUESTS,
                successfulRequests,
                "All performance test requests should succeed"
        );


        assertTrue(
                p95 <= P95_THRESHOLD_MILLIS,
                "P95 response time should be <= "
                        + P95_THRESHOLD_MILLIS
                        + " ms, but was "
                        + p95
                        + " ms"
        );
    }


    // ---------------------------------------------------------
    // Percentile calculation
    // ---------------------------------------------------------

    private long calculatePercentile(
            List<Long> sortedValues,
            int percentile) {

        if (sortedValues.isEmpty()) {
            return 0;
        }


        int index =
                (int) Math.ceil(
                        percentile / 100.0
                                * sortedValues.size()
                ) - 1;


        index =
                Math.max(
                        0,
                        Math.min(
                                index,
                                sortedValues.size() - 1
                        )
                );


        return sortedValues.get(
                index
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