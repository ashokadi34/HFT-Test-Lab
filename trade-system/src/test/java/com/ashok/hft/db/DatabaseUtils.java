package com.ashok.hft.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/hftdb";

    private static final String USERNAME = "postgres";

    private static final String PASSWORD = "postgres";

    public static ResultSet getOrderById(Long orderId) throws Exception {

        Connection connection =
                DriverManager.getConnection(
                        URL,
                        USERNAME,
                        PASSWORD
                );

        String query =
                "SELECT id, symbol, price, quantity, side, status " +
                        "FROM orders WHERE id = ?";

        PreparedStatement statement =
                connection.prepareStatement(query);

        statement.setLong(1, orderId);

        return statement.executeQuery();
    }

    public static List<String> getOrderStatusHistory(Long orderId)
            throws Exception {

        Connection connection =
                DriverManager.getConnection(
                        URL,
                        USERNAME,
                        PASSWORD
                );

        String query = """
            SELECT status
            FROM order_status_history
            WHERE order_id = ?
            ORDER BY updated_time ASC
            """;

        PreparedStatement statement =
                connection.prepareStatement(query);

        statement.setLong(1, orderId);

        ResultSet resultSet =
                statement.executeQuery();

        List<String> statuses = new ArrayList<>();

        while (resultSet.next()) {

            statuses.add(
                    resultSet.getString("status")
            );
        }

        resultSet.close();
        statement.close();
        connection.close();

        return statuses;
    }

    public static ResultSet getTradeByOrderIds(
            Long buyOrderId,
            Long sellOrderId) throws Exception {

        Connection connection =
                DriverManager.getConnection(
                        URL,
                        USERNAME,
                        PASSWORD
                );

        String query = """
            SELECT id,
                   buy_order_id,
                   sell_order_id,
                   symbol,
                   price,
                   quantity,
                   executed_time
            FROM trades
            WHERE buy_order_id = ?
              AND sell_order_id = ?
            ORDER BY executed_time DESC
            LIMIT 1
            """;

        PreparedStatement statement =
                connection.prepareStatement(query);

        statement.setLong(1, buyOrderId);
        statement.setLong(2, sellOrderId);

        return statement.executeQuery();
    }

}