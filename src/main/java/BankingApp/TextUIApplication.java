package BankingApp;

import java.sql.SQLException;
import java.util.Scanner;

public class TextUIApplication {
    private Scanner scanner;
    private final Database database;

    public TextUIApplication(Scanner scanner, Database database) {
        this.scanner = scanner;
        this.database = database;
    }
    public void start() throws SQLException{

        System.out.println("Hello! Welcome to my banking app!");

        while (true) {
            System.out.print("Please select operation: ");
            int  input = Integer.parseInt(scanner.nextLine());

            if (input == 0) {
                System.out.println("Thanks. Have a good day!");
                break;
            }
            if (input == 1) {
                CreateCustomerAccount();
            }
            if (input == 2) {
                CreateBankAccount();
            }
        }

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

        System.out.print("Initial deposit: ");
        Double initialDeposit = Double.valueOf(scanner.nextLine());

        Customer newCustomer = new Customer(firstName, lastName, username, password);


        this.database.add(newCustomer, initialDeposit);
    }


    private void CreateBankAccount() throws SQLException {
        System.out.print("Input username: ");
        String username = scanner.nextLine();
        System.out.print("Input password: ");
        String password = scanner.nextLine();
        this.database.createAccount(username, password);

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
