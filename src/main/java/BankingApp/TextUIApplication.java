package BankingApp;

import java.util.Scanner;

public class TextUIApplication {
    private Scanner scanner;
    private Database database;

    public TextUIApplication(Scanner scanner) {
        this.scanner = scanner;
        this.database = new Database();
    }
    public void start() {

        System.out.println("Hello! Welcome to my banking app!");

        while (true) {
            System.out.println("Please select operation");
            int  input = Integer.parseInt(scanner.nextLine());

            if (input == 0) {
                System.out.println();
                break;
            }
            if (input == 1) {

            }
        }

        System.out.println("Thanks. Have a good day!");
    }

    private void CreateCustomerAccount() {
        System.out.println("Enter New Customer Account Details:");
        System.out.println();
        System.out.println("First name:");
        String firstName = scanner.nextLine();
        System.out.println("Last name:");
        String lastName = scanner.nextLine();
        System.out.println("Username:");
        String username = scanner.nextLine();
        System.out.println("Password:");
        String password = scanner.nextLine();

        Customer newCustomer = new Customer(firstName, lastName, username, password);
        this.database.add(newCustomer);
    }
}
