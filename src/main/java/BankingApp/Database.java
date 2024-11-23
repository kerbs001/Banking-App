package BankingApp;
    import java.util.ArrayList;

public class Database {
    private ArrayList<Customer> customerDatabase;

    public Database() {
        this.customerDatabase = new ArrayList<Customer>();
    }

    public void add(Customer customer) {
        this.customerDatabase.add(customer);
    }

    @Override
    public String toString() {
        StringBuilder newString = new StringBuilder();
        for (Customer customer : this.customerDatabase) {
            newString.append(customer);
        }

        // Convert StringBuilder to String using toString()
        return newString.toString();
    }
}
