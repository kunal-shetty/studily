package com.studily.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides JDBC connections to MySQL. Every DAO obtains a short-lived
 * connection from here and closes it in a finally block.
 */
public final class DBConnection {

    private static final String URL = AppConfig.get("db.url");
    private static final String USER = AppConfig.get("db.user");
    private static final String PASSWORD = AppConfig.get("db.password");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("PostgreSQL JDBC driver not found: " + e.getMessage());
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
