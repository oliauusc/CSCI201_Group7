package com.foodlocator.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection utility class for managing MySQL connections
 * Singleton pattern to ensure single instance across the application
 */
public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private String url;
    private String username;
    private String password;
    private String driver;
    
    /**
     * Private constructor to prevent instantiation
     * Loads database configuration from properties file
     */
    private DatabaseConnection() {
        try {
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
            
            if (input == null) {
                System.err.println("Unable to find db.properties");
                // Fallback to default values
                this.url = "jdbc:mysql://localhost:3306/foodLocator";
                this.username = "root";
                this.password = "";
                this.driver = "com.mysql.cj.jdbc.Driver";
            } else {
                props.load(input);
                this.url = props.getProperty("db.url");
                this.username = props.getProperty("db.username");
                this.password = props.getProperty("db.password");
                this.driver = props.getProperty("db.driver");
                input.close();
            }
            
            // Load MySQL JDBC driver
            Class.forName(driver);
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Database configuration error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get singleton instance of DatabaseConnection
     * @return DatabaseConnection instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Get a database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
    
    /**
     * Close database connection safely
     * @param conn Connection to close
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}
