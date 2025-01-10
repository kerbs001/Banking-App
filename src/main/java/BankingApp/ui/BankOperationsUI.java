package BankingApp.ui;

import BankingApp.database.AccountDAO;
import BankingApp.database.CustomerDAO;
import BankingApp.database.Database;
import BankingApp.model.Customer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class BankOperationsUI {
    private final Scanner scanner;

    private final AdminOperationsUI adminOperationsUI;
    private final Database database;
    private final AccountDAO accountDAO;
    private final CustomerDAO customerDAO;

    public BankOperationsUI(Scanner scanner, Database database) {
        this.scanner = scanner;
        this.database = database;
        this.accountDAO = new AccountDAO(this.database);
        this.customerDAO = new CustomerDAO(this.database);
        this.adminOperationsUI = new AdminOperationsUI(scanner, database);
    }

    // Bank Operations - Main UI
    public void start(){

        System.out.println("Hello! Welcome to my banking app!");

        try {
            while (true) {
                printTextInstructions();
                System.out.print("Please select operation: ");
                int input = Integer.parseInt(scanner.nextLine());

                if (input == 0) {
                    database.stopDatabase();
                    System.out.println("Thanks. Have a good day!");
                    break;
                } else if (input == 1) {
                    createCustomerAccount();
                } else if (input == 2) {
                    createBankAccount();
                } else if (input == 3) {
                    getBalances();
                } else if (input == 4) {
                    depositFunds();
                } else if (input == 5) {
                    withdrawFunds();
                } else if (input == 6) {
                    applyLoan();
                } else if (input == 7) {
                    payLoan();
                } else if (input == 9) {
                    this.adminOperationsUI.startAdminControls();
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getLocalizedMessage());
        }


    }

    // Main Bank Methods
    private void createCustomerAccount() throws SQLException {
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
        this.customerDAO.add(newCustomer, initialDeposit);
    }

    private void createBankAccount() throws SQLException {
        String selectedUser = returnedUser();
        if (selectedUser != null) {
            Double initialDeposit = readDeposit();
            if (initialDeposit == null) {
                return;
            }
            this.accountDAO.generateBankAccount(selectedUser, initialDeposit);
        }
    }

    private void depositFunds() throws SQLException {
        String selectedUser = returnedUser();
        if (selectedUser != null) {
            ArrayList<String> listOfAccounts = this.accountDAO.getExistingAccounts(selectedUser);
            if (listOfAccounts == null) {
                return;
            }
            printArrayListWithIndex(listOfAccounts);

            try {
                System.out.print("Choose which account to deposit funds to: ");
                int index = Integer.parseInt(scanner.nextLine());

                // deposit funds to account in SQL table
                Double deposit = readDeposit();
                if (deposit == null) {
                    return;
                }
                this.accountDAO.addFunds(listOfAccounts.get(index - 1), deposit);
            } catch (Exception e) {
                System.out.println("Error: " + e.getLocalizedMessage());
            }
        }
    }

    private void withdrawFunds() throws SQLException {
        String selectedUser = returnedUser();
        if (selectedUser != null) {
            ArrayList<String> listOfAccounts = this.accountDAO.getExistingAccounts(selectedUser);
            if (listOfAccounts == null) {
                return;
            }
            printArrayListWithIndex(listOfAccounts);

            try {
                System.out.print("Choose which account to deposit funds to: ");
                int index = Integer.parseInt(scanner.nextLine());
                System.out.println("How much do you want to withdraw?");
                Double toWithdraw = Double.valueOf(scanner.nextLine());

                // deposit funds to account in SQL table
                this.accountDAO.decreaseFunds(listOfAccounts.get(index - 1), toWithdraw);

            } catch (Exception e) {
                System.out.println("Error: " + e.getLocalizedMessage());
            }
        }
    }

    private void getBalances() throws SQLException {
        String selectedUser = returnedUser();
        if (selectedUser != null) {
            ArrayList<String> listOfAccounts = this.accountDAO.getExistingAccounts(selectedUser);
            if (listOfAccounts == null) {
                return;
            }
            printArrayListWithIndex(listOfAccounts);
            System.out.print("Choose which account to deposit loaned funds to: ");
            int index = Integer.parseInt(scanner.nextLine());
            System.out.println();
            System.out.println("Existing funds: " + this.accountDAO.getFunds(listOfAccounts.get(index - 1)));
            System.out.println("Existing loan balance: " + this.accountDAO.getLoanBalance(listOfAccounts.get(index - 1)));
        }
    }

    private void applyLoan() throws SQLException {
        String selectedUser = returnedUser();
        if (selectedUser != null) {

            ArrayList<String> listOfAccounts = this.accountDAO.getExistingAccounts(selectedUser);
            if (listOfAccounts == null) {
                return;
            }
            printArrayListWithIndex(listOfAccounts);

            try {
                System.out.print("Choose which account to deposit loaned funds to: ");
                int index = Integer.parseInt(scanner.nextLine());
                System.out.println("How much do you want to loan?");
                double toLoan = Double.parseDouble(scanner.nextLine());

                printLoanInstructions();

                System.out.print("Please select loan deferment basis: ");
                double percentageIncrease = 0.0;

                int input = Integer.parseInt(scanner.nextLine());

                if (input == 1) {
                    percentageIncrease = 1.0;
                } else if (input == 2) {
                    percentageIncrease = 1.05;
                } else if (input == 3) {
                    percentageIncrease = 1.10;
                } else if (input == 4) {
                    return;
                }

                if (percentageIncrease == 0.0) {
                    System.out.println("Invalid input.");
                    return;
                }

                this.accountDAO.loanApplication(listOfAccounts.get(index - 1), toLoan * percentageIncrease);
                this.accountDAO.addFunds(listOfAccounts.get(index - 1), toLoan);

            } catch (Exception e) {
                System.out.println("Error: " + e.getLocalizedMessage());
            }
        }
    }

    private void payLoan() throws SQLException {

        String selectedUser = returnedUser();
        if (selectedUser != null) {

            ArrayList<String> listOfAccounts = this.accountDAO.getExistingAccounts(selectedUser);
            if (listOfAccounts == null) {
                return;
            }
            printArrayListWithIndex(listOfAccounts);

            try {
                System.out.print("Choose which account to deposit loaned funds to: ");
                int index = Integer.parseInt(scanner.nextLine());

                String accountNumber = listOfAccounts.get(index - 1);
                Double loanBalance = this.accountDAO.getLoanBalance(accountNumber);

                if (loanBalance <= 0) {
                    System.out.println("No outstanding loan balance for this account.");
                    return;
                }

                System.out.println("Existing loan balance of account '" + listOfAccounts.get(index - 1) + "' is: " + loanBalance);
                System.out.print("Please enter how much to deposit (Excess amount to be deposited to existing funds): ");
                double payment = Double.parseDouble(scanner.nextLine());

                if (payment <= 0) {
                    System.out.println("Payment amount must be positive.");
                    return;
                }

                double excessPayment = this.accountDAO.payLoan(accountNumber, payment);
                if (excessPayment > 0) {
                    this.accountDAO.addFunds(accountNumber, excessPayment);
                }

            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Helper Methods
    private Double readDeposit() {
        Double deposit = null;
        try {
            System.out.print("Deposit: ");
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
        System.out.println("--- Bank Operations ---");
        System.out.println("1: Create customer account");
        System.out.println("2: Generate bank account for existing customer account");
        System.out.println("3: Get fund and loan balances");
        System.out.println("4: Deposit into account");
        System.out.println("5: Withdraw from account");
        System.out.println("6: Apply for Loan");
        System.out.println("7: Pay Loan");
        System.out.println("9: Admin controls");
        System.out.println();
    }

    private void printLoanInstructions() {
        System.out.println();
        System.out.println("*** Loan Deferment Options ***");
        System.out.println("1: Pay later (1 month: 0%)");
        System.out.println("2: Pay after 3 months (5%)");
        System.out.println("3: Pay after 6 months (10%)");
        System.out.println("4: Return to bank operations");
        System.out.println();
    }

    private void printArrayListWithIndex(ArrayList<String> list) {
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ": " + list.get(i));
        }
    }

    private String returnedUser() throws SQLException {
        System.out.print("Input username: ");
        String username = scanner.nextLine();
        System.out.print("Input password: ");
        String password = scanner.nextLine();
        if (this.accountDAO.checkAccountExistence(username, password)) {
            return username;
        }
        return null;
    }


}
