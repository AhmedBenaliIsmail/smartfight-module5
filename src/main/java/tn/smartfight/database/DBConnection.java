package tn.smartfight.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/smartfight?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&maxAllowedPacket=67108864";
    private static final String USER = "root";
    private static final String PASS = "";

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        connect();
        applyGlobalPacketSize();
        reconnect(); // reconnect so the new global limit applies to this connection
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("[DB] Connected");
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            throw new RuntimeException("Unable to connect to database", e);
        }
    }

    private void applyGlobalPacketSize() {
        try (java.sql.Statement st = connection.createStatement()) {
            st.execute("SET GLOBAL max_allowed_packet = 67108864"); // 64 MB
        } catch (SQLException ignored) {
            // Requires SUPER privilege; harmless if unavailable
        }
    }

    private void reconnect() {
        try {
            connection.close();
        } catch (SQLException ignored) {}
        connect();
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }
}
