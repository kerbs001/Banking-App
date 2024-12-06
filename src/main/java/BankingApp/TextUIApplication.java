package BankingApp;

import java.sql.SQLException;
import java.util.Scanner;

public class TextUIApplication {
    private final Scanner scanner;
    private final Database database;

    public TextUIApplication(Scanner scanner, Database database) {
        this.scanner = scanner;
        this.database = database;
    }
    public void start() throws SQLException{

        System.out.println("Hello! Welcome to my banking app!");

        while (true) {
            printTextInstructions();
            System.out.print("Please select operation: ");
            int  input = Integer.parseInt(scanner.nextLine());

            if (input == 0) {
                this.database.stopDatabase();
                System.out.println("Thanks. Have a good day!");
                break;
            }
            if (input == 1) {
                CreateCustomerAccount();
            }
            if (input == 2) {
                CreateBankAccount();
            }
            if (input == 9) {
                adminControl();
            }
        }

    }

    private void adminControl() throws SQLException {
        this.database.hardReset();
    }

    private void CreateCustomerAccount() throws SQLException {
        System.out.println("Enter New Customer Account Details:");
        System.out.print("First name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Double initialDeposit = readDeposit();
        if (initialDeposit == null) {
            return;
        }
        Customer newCustomer = new Customer(firstName, lastName, username, password);
        this.database.add(newCustomer, initialDeposit);
    }

    private void CreateBankAccount() throws SQLException {
        System.out.print("Input username: ");
        String username = scanner.nextLine();
        System.out.print("Input password: ");
        String password = scanner.nextLine();

        if (this.database.checkAccountExistence(username, password)) {
            Double initialDeposit = readDeposit();
            if (initialDeposit == null) {
                return;
            }
            this.database.generateBankAccount(username, initialDeposit);
        }
    }

    private Double readDeposit() {
        Double deposit = null;
        try {
            System.out.print("Initial deposit: ");
            deposit = Double.valueOf(scanner.nextLine());

            if (deposit <= 0.0) {
                System.out.println("Invalid value. Deposit must be greater than 0.");
                return null;
            }

        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid number for the deposit.");
        }
        return deposit;
    }

    private void printTextInstructions() {
        System.out.println();
        System.out.println("1: Create customer account");
        System.out.println("2: Generate bank account for existing customer account");
        System.out.println("3: Deposit into account");
        System.out.println("4: Withdraw from account");
        System.out.println("5: Apply for Loan");
        System.out.println("6: Pay Loan");
    }
}
