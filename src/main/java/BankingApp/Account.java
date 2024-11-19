package BankingApp;

public class Account {
    private String bankAccountNumber;
    private int funds;

    public Account() {
        this.bankAccountNumber = AccountNumberGenerator.generateAccountNumber();
        this.funds = 0;
    }
    public Account(int initialDeposit) {
        this.bankAccountNumber = AccountNumberGenerator.generateAccountNumber();
        this.funds = initialDeposit;
    }
    public void addFunds(int deposit) {
        this.funds += deposit;
    }
    public boolean withdrawFunds(int toWithdraw) {
        return this.funds >= toWithdraw;
    }

    public int getFunds() {
        return this.funds;
    }
    public String getBankAccountNumber() {
        return this.bankAccountNumber;
    }

}
