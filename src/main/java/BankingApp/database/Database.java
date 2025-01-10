package BankingApp.database;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private final HikariDataSource dataSource;
    private static Server consoleServer;
    private static final String USER = "admin";
    private static final String PASSWORD = "1234";

    /**
     * Initializes H2 Database with HikariCP pooling
     */
    public Database(String databasePath) {
        try {
            // Start H2 Console Server
            consoleServer = Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9092").start();
            Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("H2 Console started at: http://localhost:8082");
        } catch (Exception e) {
            System.out.println("Error starting H2 Console: " + e.getMessage());
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databasePath);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(10);
        config.setIdleTimeout(30000);
        config.setMaximumPoolSize(30000);

        this.dataSource = new HikariDataSource(config);

        generateDatabase();
    }

    /**
     * Generate H2 Database to store credentials and fund amount of customers
     */
    public void generateDatabase() {

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    first_name VARCHAR(255) NOT NULL,
                    last_name VARCHAR(255) NOT NULL,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL
                );
                """;
            statement.execute(createUsersTable);

            // Create Accounts Table
            String createAccountsTable = """
                CREATE TABLE IF NOT EXISTS accounts (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    account_number VARCHAR(20) NOT NULL UNIQUE,
                    user_id INT NOT NULL,
                    fund_amount DECIMAL(15, 2) NOT NULL,
                    loan_amount DECIMAL(15, 2) DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
                """;
            statement.execute(createAccountsTable);

            System.out.println("Tables created successfully!");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Retrieves a database connection from dataSource.
     * @return Connection
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }


    public void stopDatabase() {
        if (consoleServer != null) {
            consoleServer.stop();
            System.out.println("H2 Console stopped.");
        }
        if (dataSource != null) {
            dataSource.close();
            System.out.println("HikariCP DataSource closed.");
        }
    }
}
