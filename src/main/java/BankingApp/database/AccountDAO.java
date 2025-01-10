package BankingApp.database;

import BankingApp.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AccountDAO {
    private final Database database;

    public AccountDAO(Database database) {
        this.database = database;
    }

    public void generateBankAccount(String username, Double initialDeposit) throws SQLException {
        try (Connection connection = database.getConnection()) {
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

    public void addFunds(String accountNumber, Double deposit) throws SQLException {
        try (Connection connection = database.getConnection()) {
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

    public void decreaseFunds(String accountNumber, Double withdraw) throws SQLException {

        if (isWithdrawValid(accountNumber, withdraw)) {
            try (Connection connection = database.getConnection()) {
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
        try (Connection connection = database.getConnection()) {
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

    public Double getLoanBalance(String accountNumber) throws SQLException {
        try (Connection connection = database.getConnection()) {
            String balanceQuery = "SELECT loan_amount FROM accounts WHERE account_number = ?";

            try (PreparedStatement getStmt = connection.prepareStatement(balanceQuery)) {
                getStmt.setString(1, accountNumber);

                try (ResultSet result = getStmt.executeQuery()) {
                    if (result.next()) {
                        return result.getDouble(1);
                    }
                }

            }
        }
        return 0.0;
    }
    public Double getFunds(String accountNumber) throws SQLException {
        try (Connection connection = database.getConnection()) {
            String fundBalanceQuery = "SELECT fund_amount FROM accounts WHERE account_number = ?";

            try (PreparedStatement getStmt = connection.prepareStatement(fundBalanceQuery)) {
                getStmt.setString(1, accountNumber);

                try (ResultSet result = getStmt.executeQuery()) {
                    if (result.next()) {
                        return result.getDouble(1);
                    }
                }
            }
        }
        return 0.0;
    }

    public double payLoan(String accountNumber, Double payment) throws SQLException {
        if (payment <= 0) {
            System.out.println("Payment amount must be positive.");
            return 0.0;
        }

        Double loanBalance = getLoanBalance(accountNumber);
        if (loanBalance <= 0) {
            System.out.println("No outstanding loan balance for this account.");
            return 0.0;
        }

        double excessPayment = 0.0;

        if (payment > loanBalance) {
            excessPayment = payment - loanBalance;
            payment = loanBalance; // Cap payment to loan balance
        }

        // Update loan balance
        try (Connection connection = database.getConnection()) {
            String payLoanQuery = "UPDATE accounts SET loan_amount = loan_amount - ? WHERE account_number = ?";
            try (PreparedStatement stmt = connection.prepareStatement(payLoanQuery)) {
                stmt.setDouble(1, payment);
                stmt.setString(2, accountNumber);
                stmt.executeUpdate();
            }
        }

        System.out.println("Loan payment of " + payment + " processed successfully.");

        // Deposit excess funds if any
        if (excessPayment > 0) {
            System.out.println("Excess payment of " + excessPayment + " will be deposited to your account.");
            addFunds(accountNumber, excessPayment);
        }

        return excessPayment;
    }

    public boolean checkAccountExistence(String username, String password) throws SQLException {
        try (Connection connection = database.getConnection()) {
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
        try (Connection connection = database.getConnection()) {
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
        try (Connection connection = database.getConnection()) {
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
