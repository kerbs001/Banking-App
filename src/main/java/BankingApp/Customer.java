package BankingApp;

import java.util.ArrayList;

public class Customer {
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private ArrayList<Account> accountNumbers;

    private int funds;

    public Customer(String firstName, String lastName, String userName, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.accountNumbers = new ArrayList<>();
    }

    // GETTERS
    public String getFirstName() {
        return this.firstName;
    }
    public String getLastName() {
        return this.lastName;
    }
    public String getUserName() {
        return this.userName;
    }
    protected String getPassword() {
        return this.password;
    }
    public ArrayList<Account> getAccountNumbers() {
        return this.accountNumbers;
    }

    // SETTERS
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    protected void setPassword(String password) {
        this.password = password;
    }
    protected boolean addAccountNumber(Account bankAccountNumber) {
        if (this.accountNumbers.add(bankAccountNumber)) {
            return true;
        }
        return false;
    }





}
