package BankingApp.database;

import BankingApp.model.Customer;

import java.sql.*;

public class CustomerDAO {
    private final Database database;

    public CustomerDAO(Database database) {
        this.database = database;
    }

    public void add(Customer customer, Double initialDeposit) throws SQLException {
        try (Connection connection = database.getConnection()) {
            if (isUsernameTaken(connection, customer.getUserName())) {
                System.out.println("Username '" + customer.getUserName() + "' already exists. Skipping insert.");
                return;
            }
            int userId = insertUser(connection, customer);
            if (userId != -1) {
                insertAccount(connection, userId, customer, initialDeposit);
                System.out.println("Customer and account created successfully.");

            }
        }
    }
    private boolean isUsernameTaken(Connection connection, String username) throws SQLException {
        String userQuery = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (PreparedStatement userStmt = connection.prepareStatement(userQuery)) {
            userStmt.setString(1, username);
            try (ResultSet result = userStmt.executeQuery()) {
                return result.next() && result.getInt(1) > 0;
            }
        }
    }
    private int insertUser(Connection connection, Customer customer) throws SQLException {
        String insertQuery = "INSERT INTO users (first_name, last_name, username, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getUserName());
            stmt.setString(4, customer.getPassword());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }
    private void insertAccount(Connection connection, int userId, Customer customer, double initialDeposit) throws SQLException {
        String insertQuery = "INSERT INTO accounts (account_number, user_id, fund_amount, loan_amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
            stmt.setString(1, customer.getAccountNumber());
            stmt.setInt(2, userId);
            stmt.setDouble(3, initialDeposit);
            stmt.setDouble(4, 0.0);
            stmt.executeUpdate();
        }
    }


}
