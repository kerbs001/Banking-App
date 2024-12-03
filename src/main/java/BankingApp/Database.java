package BankingApp;

import org.h2.tools.Server;

import java.sql.*;

public class Database {
    String databasePath;

    public Database(String databasePath) throws SQLException {
        this.databasePath = databasePath;
        generateDatabase(databasePath);
    }

    public void add(Customer customer, Double initialDeposit) throws SQLException {
        try (Connection connection = createConnectionAndEnsureDatabase(databasePath)) {
            // Check for uniqueness of username
            String userQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement userStmt = connection.prepareStatement(userQuery)) {
                userStmt.setString(1, customer.getUserName());
                try (ResultSet result = userStmt.executeQuery()) {
                    if (result.next() && result.getInt(1) > 0) {
                        System.out.println("Username '" + customer.getUserName() + "' already exists. Skipping insert.");
                        return; // Early exit for duplicates
                    }
                }
            }

            String insertQuery = "INSERT INTO users (first_name, last_name, username, password) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, customer.getFirstName());
                insertStmt.setString(2, customer.getLastName());
                insertStmt.setString(3, customer.getUserName());
                insertStmt.setString(4, customer.getPassword());
                insertStmt.executeUpdate();
                System.out.println("User '" + customer.getUserName() + "' added successfully.");

                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        String insertAccountQuery = "INSERT INTO accounts (account_number, user_id, fund_amount, loan_amount) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement accountStmt = connection.prepareStatement(insertAccountQuery)) {
                            accountStmt.setString(1, customer.getAccountNumber());
                            accountStmt.setInt(2, userId);
                            accountStmt.setDouble(3, initialDeposit);
                            accountStmt.setDouble(4, 0.0);
                            accountStmt.executeUpdate();
                            System.out.println("Customer and account created successfully.");
                        }
                    } else {
                        System.out.println("Failed to retrieve user ID. Account creation aborted.");
                    }
                }
            }
        }
    }

    public void createAccount(String username, String password) throws SQLException {
        try (Connection connection = createConnectionAndEnsureDatabase(databasePath)) {
            String userQuery = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement accountStmt = connection.prepareStatement(userQuery)) {
                accountStmt.setString(1, username);
                accountStmt.setString(2, password);
                try (ResultSet result = accountStmt.executeQuery()) {
                    if (result.next() && result.getInt(1) == 0) {
                        System.out.println("User not found. Retry input.");
                        return;
                    }
                }
                System.out.println("Account found. Generating account number...");
            }
        }
    }


    private Connection createConnectionAndEnsureDatabase(String databasePath) throws SQLException {
        String user = "admin";
        String password = "1234";

        Connection connection = DriverManager.getConnection(databasePath, user, password);

        return connection;
    }

    /**
     * Generate H2 Database to store credentials and fund amount of customers
     * @throws SQLException
     */
    public static void generateDatabase(String databasePath) throws SQLException {
        String user = "admin";
        String password = "1234";

        Server console = null;

        try {
            // Start H2 Console Server
            console = Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9092").start();
            Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.println("H2 Console started at: http://localhost:8082");

            // Connect to database
            try (Connection connection = DriverManager.getConnection(databasePath, user, password)) {
                System.out.println("Connected to H2 Database.");

                try (Statement statement = connection.createStatement()) {

                    // Create Users Table
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
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
