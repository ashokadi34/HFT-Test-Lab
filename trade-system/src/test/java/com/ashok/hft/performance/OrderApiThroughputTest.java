package com.ashok.hft.performance;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.RANDOM_PORT
)
class OrderApiThroughputTest {

    @LocalServerPort
    private int port;


    private final HttpClient httpClient =
            HttpClient.newHttpClient();


    // ---------------------------------------------------------
    // PERF-002: Sequential Order API Throughput
    // ---------------------------------------------------------

    @Test
    void shouldMeasureSequentialOrderApiThroughput()
            throws Exception {

        runThroughputScenario(100);

        runThroughputScenario(500);

        runThroughputScenario(1000);
    }


    // ---------------------------------------------------------
    // Execute one sequential load scenario
    // ---------------------------------------------------------

    private void runThroughputScenario(
            int totalRequests)
            throws Exception {

        PerformanceMetrics metrics =
                new PerformanceMetrics();


        long testStart =
                System.nanoTime();


        for (int i = 1;
             i <= totalRequests;
             i++) {

            double price =
                    10.00
                            + (totalRequests * 0.001)
                            + (i * 0.01);


            String requestBody =
                    """
                    {
                        "symbol": "TESTA",
                        "price": %s,
                        "quantity": 10,
                        "side": "BUY"
                    }
                    """.formatted(price);


            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            "http://localhost:"
                                                    + port
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


            long requestStart =
                    System.nanoTime();


            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );


            long requestEnd =
                    System.nanoTime();


            long responseTimeMillis =
                    (
                            requestEnd - requestStart
                    ) / 1_000_000;


            boolean successful =
                    response.statusCode() == 200;


            metrics.recordResponse(
                    responseTimeMillis,
                    successful
            );


            assertEquals(
                    200,
                    response.statusCode(),
                    "Order API should return HTTP 200"
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


        metrics.printReport(
                "PERF-002 SEQUENTIAL THROUGHPUT - "
                        + totalRequests
                        + " REQUESTS"
        );
    }
}