package BankingApp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.Server;

import java.sql.*;
import java.util.ArrayList;

public class Database {
    private final HikariDataSource dataSource;
    private static Server consoleServer;
    private static final String USER = "admin";
    private static final String PASSWORD = "1234";


    // Database Initialization
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

    // ***********************

    // Main Methods - Bank Operations



    // Adding User in Database
    public void add(Customer customer, Double initialDeposit) throws SQLException {
        try (Connection connection = getConnection()) {
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

    public void generateBankAccount(String username, Double initialDeposit) throws SQLException {
        try (Connection connection = getConnection()) {
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

    public void depositFunds(String accountNumber, Double deposit) throws SQLException {
        try (Connection connection = getConnection()) {
            String depositQuery = "UPDATE accounts SET fund_amount = fund_amount + ? WHERE account_number = ?";

            try (PreparedStatement updateStmt = connection.prepareStatement(depositQuery)) {
                updateStmt.setDouble(1, deposit);
                updateStmt.setString(2, accountNumber);
                int rowsUpdated = updateStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Deposit successful");
                } else {
                    System.out.println("Account not found");
                }
            }
        }
    }

    public void withdrawFunds(String accountNumber, Double withdraw) throws SQLException {

        if (isWithdrawValid(accountNumber, withdraw)) {
            try (Connection connection = getConnection()) {
                String withdrawQuery = "UPDATE accounts SET fund_amount = fund_amount - ? WHERE account_number = ?";
                try (PreparedStatement withdrawStmt = connection.prepareStatement(withdrawQuery)) {
                    withdrawStmt.setDouble(1, withdraw);
                    withdrawStmt.setString(2, accountNumber);
                    int rowsUpdated = withdrawStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        System.out.println("Withdraw successful.");
                    } else {
                        System.out.println("Withdraw unsuccessful.");
                    }
                }
            }
        } else {
            System.out.println("Invalid withdraw amount. Existing funds not sufficient.");
        }
    }

    public void loanApplication(String accountNumber, Double loan) throws SQLException {
        try (Connection connection = getConnection()) {
            String loanQuery = "UPDATE accounts SET loan_amount = loan_amount + ? WHERE account_number = ?";
            try (PreparedStatement loanStmt = connection.prepareStatement(loanQuery)) {
                loanStmt.setDouble(1, loan);
                loanStmt.setString(2, accountNumber);
                int rowsUpdated = loanStmt.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Loan successful.");
                }
            }
        }
    }

    // Main Methods - Admin Operations
    public ArrayList<String> getAllUsernames() throws SQLException {
        ArrayList<String> listOfUsernames = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String usernameQuery = "SELECT id, username FROM users ";
            try (PreparedStatement usernameStmt = connection.prepareStatement(usernameQuery)) {
                try (ResultSet allUsernames = usernameStmt.executeQuery()) {
                    while (allUsernames.next()) {
                        String formattedUser = allUsernames.getString("id") + ": " + allUsernames.getString("username");

                        // Add to the list.
                        listOfUsernames.add(formattedUser);
                    }
                    return listOfUsernames;
                }
            }
        }
    }

    public ArrayList<String> getAllBankAccountNumbers() throws SQLException {
        ArrayList<String> listOfBankAccountNumbers = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String bankAccountQuery = "SELECT user_id, account_number FROM accounts ";
            try (PreparedStatement bankAccountStmt = connection.prepareStatement(bankAccountQuery)) {
                try (ResultSet allBankAccounts = bankAccountStmt.executeQuery()) {
                    while (allBankAccounts.next()) {
                        String formattedUser = allBankAccounts.getString("user_id") + ": " + allBankAccounts.getString("account_number");
                        // Add to the list.
                        listOfBankAccountNumbers.add(formattedUser);
                    }
                    return listOfBankAccountNumbers;
                }
            }
        }
    }

    public void hardReset() throws SQLException {
        String userQuery1 = "TRUNCATE TABLE users";
        String userQuery2 = "TRUNCATE TABLE accounts";
        String resetUserIdSequence = "ALTER TABLE users ALTER COLUMN id RESTART WITH 1";
        String resetAccountIdSequence = "ALTER TABLE accounts ALTER COLUMN id RESTART WITH 1";

        try (Connection connection = getConnection()) {
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

    // ------------------- HELPER METHODS

    // **CONNECTION
    private Connection getConnection() throws SQLException {

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

    // **ADDING USER
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



    public boolean checkAccountExistence(String username, String password) throws SQLException {
        try (Connection connection = getConnection()) {
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
                System.out.println("Account access authorized.");
                return true;
            }
        }
    }

    public ArrayList<String> getExistingAccounts(String username) throws SQLException {
        try (Connection connection = getConnection()) {
            String idQuery = "SELECT id FROM users WHERE username = ?";

            try (PreparedStatement idStmt = connection.prepareStatement(idQuery)) {
                idStmt.setString(1, username);

                try (ResultSet result = idStmt.executeQuery()) {
                    if (result.next()) {
                        int userId = result.getInt("id");
                        ArrayList<String> listOfAccounts = new ArrayList<>();
                        String listAccountsQuery = "SELECT account_number FROM accounts WHERE user_id = ?";

                        try (PreparedStatement listStmt = connection.prepareStatement(listAccountsQuery)) {
                            listStmt.setInt(1, userId);

                            try (ResultSet listResult = listStmt.executeQuery()) {
                                while (listResult.next()) {
                                    String accountNumber = listResult.getString("account_number");
                                    listOfAccounts.add(accountNumber);
                                }
                                return listOfAccounts;
                            }
                        }
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    public boolean isWithdrawValid(String accountNumber, Double withdraw) throws SQLException {
        try (Connection connection = getConnection()) {
            String checkFundQuery = "SELECT fund_amount FROM accounts WHERE account_number = ?";
            try (PreparedStatement checkFundStmt = connection.prepareStatement(checkFundQuery)) {
                checkFundStmt.setString(1, accountNumber);
                try (ResultSet existingFunds = checkFundStmt.executeQuery()) {
                    if (existingFunds.next()) {
                        double currentFunds = existingFunds.getDouble("fund_amount");
                        return currentFunds >= withdraw;
                    }
                }
            }
        }
        return false;
    }
}
