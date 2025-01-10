package BankingApp.ui;

import BankingApp.database.AdminDAO;
import BankingApp.database.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class AdminOperationsUI {
    private final Scanner scanner;
    private final AdminDAO adminDAO;


    public AdminOperationsUI(Scanner scanner, Database database) {
        this.scanner = scanner;
        this.adminDAO = new AdminDAO(database);
    }

    public void startAdminControls() throws SQLException {
        System.out.print("Admin username: ");
        String username = scanner.nextLine();
        System.out.print("Admin password: ");
        String password = scanner.nextLine();

        if (username.equals("admin") && password.equals("1234")) {
            while (true) {
                printAdminControlInstructions();

                System.out.print("Please select admin operation: ");
                int input = Integer.parseInt(scanner.nextLine());

                if (input == 1) {
                    ArrayList<String> allUsernames = adminDAO.getAllUsernames();
                    if (allUsernames.isEmpty()) {
                        System.out.println("List of usernames is empty.");
                    } else {
                        printArrayList(allUsernames);
                    }
                }
                if (input == 2) {
                    ArrayList<String> allBankAccounts = adminDAO.getAllBankAccountNumbers();
                    if (allBankAccounts.isEmpty()) {
                        System.out.println("List of bank account numbers is empty.");
                    } else {
                        printArrayList(allBankAccounts);
                    }
                }

                if (input == 3) {
                    adminDAO.hardReset();
                }

                if (input == 4) {
                    break;
                }
            }
        } else {
            System.out.println("Wrong admin username and/or password.");
        }
    }
    private void printAdminControlInstructions() {
        System.out.println();
        System.out.println("*** Admin Control Operations ***");
        System.out.println("1: List all usernames");
        System.out.println("2: List all bank account numbers");
        System.out.println("3: Truncate all tables");
        System.out.println("4: Return to bank operations");
        System.out.println();
    }
    private void printArrayList(ArrayList<String> list) {
        for (String string : list) {
            System.out.println(string);
        }
    }
}
