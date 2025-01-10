package BankingApp.model;

import BankingApp.utils.AccountNumberGenerator;

public class Account {
    private final String bankAccountNumber;
    private int funds;

    public Account() {
        this.bankAccountNumber = AccountNumberGenerator.generateAccountNumber();
        this.funds = 0;
    }
    public Account(int initialDeposit) {
        this.bankAccountNumber = AccountNumberGenerator.generateAccountNumber();
        this.funds = initialDeposit;
    }

    public String getBankAccountNumber() {
        return this.bankAccountNumber;
    }

}
