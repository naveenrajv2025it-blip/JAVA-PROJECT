package com.campus.lostfound.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Reusable JDBC Connection Factory.
 * Manages connection pooling via HikariCP with transparent fallback to DriverManager.
 * Supports external configuration via db.properties and environment variables.
 */
public class DBConnection {
    private static HikariDataSource dataSource;
    private static final Properties props = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load db.properties: " + e.getMessage());
        }

        try {
            String driver = getSetting("db.driver", "com.mysql.cj.jdbc.Driver");
            String url = getSetting("db.url", "jdbc:mysql://localhost:3306/campus_lost_found_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            String user = getSetting("db.user", "root");
            String password = getSetting("db.password", "Naveen@123");

            HikariConfig config = new HikariConfig();
            config.setDriverClassName(driver);
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);

            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("pool.maxSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("pool.minIdle", "2")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("pool.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("pool.idleTimeout", "600000")));

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("Notice: HikariCP pool init deferred, will use direct connection fallback: " + e.getMessage());
        }
    }

    private static String getSetting(String key, String defaultValue) {
        // 1. Environment variable (e.g., DB_URL, DB_USER, DB_PASSWORD)
        String envKey = key.toUpperCase().replace('.', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }

        // 2. Properties file
        String propVal = props.getProperty(key);
        if (propVal != null && !propVal.trim().isEmpty()) {
            return propVal.trim();
        }

        return defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource.getConnection();
        }

        // Direct JDBC fallback
        try {
            String driver = getSetting("db.driver", "com.mysql.cj.jdbc.Driver");
            String url = getSetting("db.url", "jdbc:mysql://localhost:3306/campus_lost_found_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            String user = getSetting("db.user", "root");
            String password = getSetting("db.password", "Naveen@123");

            Class.forName(driver);
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found: " + e.getMessage(), e);
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
