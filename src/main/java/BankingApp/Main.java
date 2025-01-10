package BankingApp;

import BankingApp.database.Database;
import BankingApp.ui.BankOperationsUI;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException {
        String databasePath = "jdbc:h2:tcp://localhost/./bankingdb";

        Database database = new Database(databasePath);
        Scanner scanner = new Scanner(System.in);

        new BankOperationsUI(scanner, database).start();

    }

}
