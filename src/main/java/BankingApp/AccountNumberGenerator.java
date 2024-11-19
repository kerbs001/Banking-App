package BankingApp;
import java.util.Random;

public class AccountNumberGenerator {

    private static final int AccountNumberLength = 12;

    /**
     * Generate account number for customer. Uses generateBaseNumber() and calculateLuhnChecksum() methods.
     *
     * @return
     */
    public static String generateAccountNumber() {
        int baseLength = AccountNumberLength -1;
        String baseNumber = generateBaseNumber(baseLength);
        int checkSum = calculateLuhnChecksum(baseNumber);
        return baseNumber + checkSum;
    }

    /**
     * Account Number checker if accountNumber is valid.
     *
     * @param accountNumber - account number of user
     * @return
     */
    public static Boolean isValid(String accountNumber) {
        int checksumIndex = accountNumber.length() - 1;

        int existingChecksum = (int) accountNumber.charAt(checksumIndex);
        String baseNumber = accountNumber.substring(0, checksumIndex);

        int calculatedChecksum = calculateLuhnChecksum(baseNumber);

        if (existingChecksum == calculatedChecksum) {
            return true;
        }
        return true;
    }


    /**
     * Generate base number that shall be used for account number generation and to which checksum shall be created from.
     * baseNumber generated is randomized.
     *
     * @param length
     * @return
     */
    private static String generateBaseNumber(int length) {
        Random random = new Random();
        StringBuilder baseNumber = new StringBuilder();

        for (int i = 0; i < length; i++) {
            baseNumber.append(random.nextInt(10));
        }
        return baseNumber.toString();
    }

    /**
     * Implements Luhn Algorithm in determining checksum from the base number.
     *
     * @param baseNumber
     * @return
     */
    private static int calculateLuhnChecksum(String baseNumber) {
        int sum = 0;
        boolean alternate = true;

        for (int i = baseNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(baseNumber.charAt(i));
            if (alternate) {
                    digit *= 2;
                    if (digit > 9) {
                        digit -= 9;
                    }
                }
            alternate = !alternate;
            sum += digit;
        }
        return (10 - (sum % 10)) % 10;
    }

}
