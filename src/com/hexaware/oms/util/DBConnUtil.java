package com.hexaware.oms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.hexaware.oms.exception.DatabaseConnectionException;

public class DBConnUtil {

    public static Connection getConnection() throws DatabaseConnectionException {
        Connection conn = null;
        try {
            Properties props = DBPropertyUtil.loadProperties("db.properties");

            String driver = props.getProperty("db.driverClassName");
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new DatabaseConnectionException("Database connection failed: " + e.getMessage());
        }
        return conn;
    }
}
