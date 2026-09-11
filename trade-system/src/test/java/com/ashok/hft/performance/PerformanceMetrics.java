package com.ashok.hft.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PerformanceMetrics {

    private final List<Long> responseTimes =
            new ArrayList<>();

    private int totalRequests;

    private int successfulRequests;

    private int failedRequests;

    private long totalDurationMillis;


    // ---------------------------------------------------------
    // Record response time
    // ---------------------------------------------------------

    public void recordResponse(
            long responseTimeMillis,
            boolean successful) {

        responseTimes.add(responseTimeMillis);

        totalRequests++;

        if (successful) {
            successfulRequests++;
        } else {
            failedRequests++;
        }
    }


    // ---------------------------------------------------------
    // Record total test duration
    // ---------------------------------------------------------

    public void setTotalDurationMillis(
            long totalDurationMillis) {

        this.totalDurationMillis =
                totalDurationMillis;
    }


    // ---------------------------------------------------------
    // Average response time
    // ---------------------------------------------------------

    public double getAverage() {

        if (responseTimes.isEmpty()) {
            return 0;
        }

        return responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }


    // ---------------------------------------------------------
    // Minimum response time
    // ---------------------------------------------------------

    public long getMin() {

        return responseTimes.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);
    }


    // ---------------------------------------------------------
    // Maximum response time
    // ---------------------------------------------------------

    public long getMax() {

        return responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);
    }


    // ---------------------------------------------------------
    // Percentile calculation
    // ---------------------------------------------------------

    public long getPercentile(
            double percentile) {

        if (responseTimes.isEmpty()) {
            return 0;
        }

        List<Long> sorted =
                new ArrayList<>(
                        responseTimes
                );

        Collections.sort(sorted);

        int index =
                (int) Math.ceil(
                        percentile * sorted.size()
                ) - 1;

        index =
                Math.max(
                        0,
                        Math.min(
                                index,
                                sorted.size() - 1
                        )
                );

        return sorted.get(index);
    }


    // ---------------------------------------------------------
    // Throughput
    // ---------------------------------------------------------

    public double getThroughput() {

        if (totalDurationMillis <= 0) {
            return 0;
        }

        return (
                successfulRequests * 1000.0
        ) / totalDurationMillis;
    }


    // ---------------------------------------------------------
    // Getters
    // ---------------------------------------------------------

    public int getTotalRequests() {
        return totalRequests;
    }

    public int getSuccessfulRequests() {
        return successfulRequests;
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    public long getTotalDurationMillis() {
        return totalDurationMillis;
    }


    // ---------------------------------------------------------
    // Print report
    // ---------------------------------------------------------

    public void printReport(
            String testName) {

        System.out.println();

        System.out.println(
                "========================================"
        );

        System.out.println(
                testName
        );

        System.out.println(
                "========================================"
        );

        System.out.printf(
                "Total Requests       : %d%n",
                totalRequests
        );

        System.out.printf(
                "Successful Requests  : %d%n",
                successfulRequests
        );

        System.out.printf(
                "Failed Requests      : %d%n",
                failedRequests
        );

        System.out.printf(
                "Total Duration       : %d ms%n",
                totalDurationMillis
        );

        System.out.printf(
                "Average Response     : %.2f ms%n",
                getAverage()
        );

        System.out.printf(
                "Minimum Response     : %d ms%n",
                getMin()
        );

        System.out.printf(
                "Maximum Response     : %d ms%n",
                getMax()
        );

        System.out.printf(
                "P50 Response         : %d ms%n",
                getPercentile(0.50)
        );

        System.out.printf(
                "P95 Response         : %d ms%n",
                getPercentile(0.95)
        );

        System.out.printf(
                "P99 Response         : %d ms%n",
                getPercentile(0.99)
        );

        System.out.printf(
                "Throughput           : %.2f req/sec%n",
                getThroughput()
        );

        System.out.println(
                "========================================"
        );

        System.out.println();
    }
}