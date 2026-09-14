package com.ashok.hft.performance;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ConcurrentOrderSubmissionTest {

    @LocalServerPort
    private int port;


    // ---------------------------------------------------------
    // PERF-003: Concurrent Order Submission
    // ---------------------------------------------------------

    @ParameterizedTest(
            name = "PERF-003 - {0} concurrent requests"
    )
    @ValueSource(ints = {10, 25, 50})
    void shouldMeasureConcurrentOrderSubmission(
            int concurrentRequests)
            throws Exception {

        runConcurrentScenario(
                concurrentRequests
        );
    }


    // ---------------------------------------------------------
    // Execute one concurrency scenario
    // ---------------------------------------------------------

    private void runConcurrentScenario(
            int concurrentRequests)
            throws Exception {

        PerformanceMetrics metrics =
                new PerformanceMetrics();


        OrderApiPerformanceClient client =
                new OrderApiPerformanceClient(
                        "http://localhost:" + port
                );


        ExecutorService executor =
                Executors.newFixedThreadPool(
                        concurrentRequests
                );


        try {

            CountDownLatch ready =
                    new CountDownLatch(
                            concurrentRequests
                    );


            CountDownLatch start =
                    new CountDownLatch(1);


            List<Future<
                    OrderApiPerformanceClient.OrderResponse>>
                    futures =
                    new ArrayList<>();


            // -------------------------------------------------
            // Create concurrent workers
            // -------------------------------------------------

            for (int i = 0;
                 i < concurrentRequests;
                 i++) {

                final int requestNumber = i;


                Future<
                        OrderApiPerformanceClient.OrderResponse>
                        future =
                        executor.submit(
                                () -> {

                                    // ---------------------------------
                                    // Signal that this worker is ready
                                    // ---------------------------------

                                    ready.countDown();


                                    // ---------------------------------
                                    // Wait for common start signal
                                    // ---------------------------------

                                    start.await();


                                    // ---------------------------------
                                    // Create unique price
                                    // ---------------------------------

                                    double price =
                                            50.00
                                                    + (
                                                    concurrentRequests
                                                            * 0.01
                                            )
                                                    + (
                                                    requestNumber
                                                            * 0.001
                                            );


                                    // ---------------------------------
                                    // Submit order
                                    // ---------------------------------

                                    return client.createOrder(
                                            "TESTA",
                                            price,
                                            10,
                                            "BUY"
                                    );
                                }
                        );


                futures.add(
                        future
                );
            }


            // -----------------------------------------------------
            // Wait until ALL workers are ready
            // -----------------------------------------------------

            ready.await();


            // -----------------------------------------------------
            // IMPORTANT:
            //
            // Start timing ONLY after all workers are ready.
            // This excludes thread creation / worker preparation
            // time from the actual concurrency measurement.
            // -----------------------------------------------------

            long testStart =
                    System.nanoTime();


            // -----------------------------------------------------
            // Release ALL workers at approximately the same time
            // -----------------------------------------------------

            start.countDown();


            // -----------------------------------------------------
            // Collect results
            // -----------------------------------------------------

            Set<String> orderIds =
                    Collections.synchronizedSet(
                            new HashSet<>()
                    );


            int successfulRequests = 0;


            for (
                    Future<
                            OrderApiPerformanceClient.OrderResponse>
                            future : futures) {

                OrderApiPerformanceClient.OrderResponse
                        response =
                        future.get();


                boolean successful =
                        response.statusCode() == 200;


                // ---------------------------------------------
                // Record individual response metrics
                // ---------------------------------------------

                metrics.recordResponse(
                        response.responseTimeMillis(),
                        successful
                );


                if (successful) {

                    successfulRequests++;


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


                    assertTrue(
                            orderIds.add(
                                    orderId
                            ),
                            "Order ID should be unique: "
                                    + orderId
                    );
                }


                assertEquals(
                        200,
                        response.statusCode(),
                        "Order API should return HTTP 200"
                );
            }


            // -----------------------------------------------------
            // Stop timing after ALL requests have completed
            // -----------------------------------------------------

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
            // Validate successful requests
            // -----------------------------------------------------

            assertEquals(
                    concurrentRequests,
                    successfulRequests,
                    "All concurrent requests should succeed"
            );


            // -----------------------------------------------------
            // Validate unique order IDs
            // -----------------------------------------------------

            assertEquals(
                    successfulRequests,
                    orderIds.size(),
                    "Every successful request should have "
                            + "a unique order ID"
            );


            // -----------------------------------------------------
            // Print performance report
            // -----------------------------------------------------

            metrics.printReport(
                    "PERF-003 CONCURRENT ORDER SUBMISSION - "
                            + concurrentRequests
                            + " REQUESTS"
            );

        } finally {

            // -----------------------------------------------------
            // Always shut down executor
            // -----------------------------------------------------

            executor.shutdown();
        }
    }


    // ---------------------------------------------------------
    // Extract ID from Order API response
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