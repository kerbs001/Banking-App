package BankingApp.database;

import java.sql.*;
import java.util.ArrayList;

public class AdminDAO {
    private final Database database;

    public AdminDAO(Database database) {
        this.database = database;
    }
    public ArrayList<String> getAllUsernames() throws SQLException {
        ArrayList<String> listOfUsernames = new ArrayList<>();
        try (Connection connection = database.getConnection()) {
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
        try (Connection connection = database.getConnection()) {
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

        try (Connection connection = database.getConnection()) {
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
