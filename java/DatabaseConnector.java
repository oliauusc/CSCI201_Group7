//package com.usc.foodlocator.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Database Connector for USC Food Locator
 * Handles MySQL database connections with HikariCP connection pooling
 */
public class DatabaseConnector {
    
    // Database Configuration - UPDATE THESE VALUES
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "usc_food_locator";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "your_password_here";
    
    // Connection Pool
    private static HikariDataSource dataSource;
    
    // Initialize connection pool
    static {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + 
                            "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);
            
            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            // Performance optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            dataSource = new HikariDataSource(config);
            
            System.out.println("Database connection pool initialized successfully");
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found");
            e.printStackTrace();
            throw new RuntimeException("Failed to load database driver", e);
        }
    }
    
    /**
     * Get a connection from the pool
     * @return Database connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Close connection and resources safely
     */
    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            System.err.println("Error closing ResultSet: " + e.getMessage());
        }
        
        try {
            if (ps != null) ps.close();
        } catch (SQLException e) {
            System.err.println("Error closing PreparedStatement: " + e.getMessage());
        }
        
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing Connection: " + e.getMessage());
        }
    }
    
    /**
     * Close connection safely (for auto-commit operations)
     */
    public static void close(Connection conn) {
        close(conn, null, null);
    }
    
    /**
     * Shutdown the connection pool (call on application shutdown)
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database connection pool shut down");
        }
    }
    
    /**
     * Test database connection
     * @return true if connection successful
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        } finally {
            close(conn);
        }
    }
}