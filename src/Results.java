/**
 * This class aims to provide data structure to keep the result from processing the task.
 * Classes "userNode" and "manager" use this class.
 */

// Necessary to use Serializable to send "Results" class through socket.
import java.io.Serializable;

public class Results implements Serializable {
    // Necessary to implement Serializable. This makes the differentiation among class instances.
    private static final long serialVersionUID = 1L;
    private String codUser;
    private long number;
    private boolean result;
    // To check result, value "15" is true, and value "25" is false.
    private int resultCheck;

    /**
     * Constructor
     * @param codUser - String, indicates user's identification code. Used to know which user generate the result.
     * @param number - long, indicates which number the user analyzed to verify if it was prime number.
     * @param result - boolean, indicates if the analyzed was prime number. True number is prime, False not.
     * @param resultCheck - int, this parameter is used to check if result was correctly write, because may happen
     *                      errors during writing, errors on connection transfer, attacks, and more.
     */
    public Results(String codUser, long number, boolean result, int resultCheck) {
        this.codUser = codUser;
        this.number = number;
        this.result = result;
        this.resultCheck = resultCheck;
    }

    /**
     * Method to get node user's code.
     * @return String - user's code.
     */
    public String getCodUser() {
        return codUser;
    }

    /**
     * Method to get result check data.
     * @return int - result check data.
     */
    public int getResultCheck() {
        return resultCheck;
    }

    /**
     * Method to get which number was sent to node to be analyzed.
     * @return long - number to node analyze.
     */
    public long getNumber() {
        return number;
    }

    /**
     * Method to get the node processing result.
     * @return boolean - True processing was ok, False processing failed.
     */
    public boolean getResult() {
        return result;
    }
}
