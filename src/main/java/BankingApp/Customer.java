package BankingApp;



public class Customer {
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private Account account;

    private int funds;

    public Customer(String firstName, String lastName, String userName, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.account = new Account();
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
    public String getAccountNumber() {
        return this.account.getBankAccountNumber();
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






}
