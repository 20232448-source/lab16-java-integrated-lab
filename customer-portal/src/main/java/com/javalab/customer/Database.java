package com.javalab.customer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class Database {
    private Database() {}
    static Connection open() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }
        return DriverManager.getConnection(
                System.getProperty("db.url", "jdbc:mysql://localhost:3306/java_integrated_lab?serverTimezone=UTC"),
                System.getProperty("db.user", "root"), System.getProperty("db.password", ""));
    }
}
