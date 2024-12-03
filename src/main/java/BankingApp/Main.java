package BankingApp;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException {
        String databasePath = "jdbc:h2:tcp://localhost/./bankingdb";

        Database database = new Database(databasePath);
        Scanner scanner = new Scanner(System.in);

        new TextUIApplication(scanner, database).start();

    }

}
