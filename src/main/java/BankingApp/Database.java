package BankingApp;

import org.h2.tools.Server;

import java.sql.*;

public class Database {
    String databasePath;
    private static Server consoleServer;
    String user = "admin";
    String password = "1234";

    public Database(String databasePath)  {
        this.databasePath = databasePath;
        generateDatabase(databasePath);
    }

    private Connection createConnectionAndEnsureDatabase(String databasePath) throws SQLException {

        return DriverManager.getConnection(databasePath, user, password);
    }

    /**
     * Generate H2 Database to store credentials and fund amount of customers
     */
    public void generateDatabase(String databasePath) {

        try {
            // Start H2 Console Server
            consoleServer = Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9092").start();
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

    public void stopDatabase() {
        if (consoleServer != null) {
            try {
                if (consoleServer.isRunning(false)) {
                    consoleServer.stop();
                    System.out.println("H2 Console stopped.");
                } else {
                    System.out.println("H2 Console is not running.");
                }
            } catch (Exception e) {
                System.err.println("Error while stopping H2 Console: " + e.getMessage());
            }
        } else {
            System.out.println("H2 Console server instance is null. Nothing to stop.");
        }
    }

    public boolean checkAccountExistence(String username, String password) throws SQLException {
        try (Connection connection = createConnectionAndEnsureDatabase(databasePath)) {
            String userQuery = "Select COUNT(*) FROM users WHERE username = ? AND password = ?";

            try (PreparedStatement accountStmt = connection.prepareStatement(userQuery)) {
                accountStmt.setString(1, username);
                accountStmt.setString(2, password);

                try (ResultSet result = accountStmt.executeQuery()) {
                    if (result.next() && result.getInt(1) == 0) {
                        System.out.println("User not found. Retry input.");
                        return false;
                    }
                }
                System.out.println("Account found. Generating new account number...");
                return true;
            }
        }
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

    public void generateBankAccount(String username, Double initialDeposit) throws SQLException {
        try (Connection connection = createConnectionAndEnsureDatabase(databasePath)) {
            String userQuery = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement accountStmt = connection.prepareStatement(userQuery)) {
                accountStmt.setString(1, username);
                try (ResultSet result = accountStmt.executeQuery()) {
                    if (result.next()) {
                        int userId = result.getInt("id");
                        Account newBankAccount = new Account();
                        String insertAccountQuery = "INSERT INTO accounts (account_number, user_id, fund_amount, loan_amount) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement insertAccountStmt = connection.prepareStatement(insertAccountQuery)) {
                            insertAccountStmt.setString(1, newBankAccount.getBankAccountNumber());
                            insertAccountStmt.setInt(2, userId);
                            insertAccountStmt.setDouble(3, initialDeposit);
                            insertAccountStmt.setDouble(4, 0.0);
                            insertAccountStmt.executeUpdate();
                            System.out.println("Bank account for " + username + " created successfully.");
                        }
                    } else {
                        System.out.println("User with username '" + username + "' does not exist.");
                    }
                }
            }
        }
    }

    public void hardReset() throws SQLException {
        String userQuery1 = "TRUNCATE TABLE users";
        String userQuery2 = "TRUNCATE TABLE accounts";
        String resetUserIdSequence = "ALTER TABLE users ALTER COLUMN id RESTART WITH 1";
        String resetAccountIdSequence = "ALTER TABLE accounts ALTER COLUMN id RESTART WITH 1";

        try (Connection connection = createConnectionAndEnsureDatabase(databasePath)) {
            Statement stmt = connection.createStatement();

            // Start a transaction
            connection.setAutoCommit(false);

            try {
                stmt.executeUpdate("SET REFERENTIAL_INTEGRITY FALSE");
                stmt.executeUpdate(userQuery1);
                stmt.executeUpdate(userQuery2);
                stmt.executeUpdate(resetUserIdSequence);
                stmt.executeUpdate(resetAccountIdSequence);
                stmt.executeUpdate("SET REFERENTIAL_INTEGRITY TRUE");

                connection.commit();
                System.out.println("Tables 'users' and 'accounts' truncated successfully");

            } catch (Exception e) {
                connection.rollback();
                System.out.println("Error in truncating tables, transaction rolled back");
                throw e;
            }
        }
    }

}
