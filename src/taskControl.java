/**
 * This class aims to provide a structure to control nodes, providing management
 * for  answers and task executions by nodes.
 */

import java.time.*;

public class taskControl {
    // Attributes used to control when node receive the task.
    private Instant startTime;
    private String codUser;
    private long number;

    /**
     * Constructor
     * @param codUser String - Indicates user's identification code.
     * @param number long - Indicates which number was sent to the node.
     */
    public taskControl(String codUser, long number) {
        this.codUser = codUser;
        this.number = number;
        this.startTime = Instant.now();
    }

    /**
     * Method to get user's identification code, used to identify the node.
     * @return String - user's identification code.
     */
    public String getCodUser() {
        return codUser;
    }

    /**
     * Method to get which number was sent to the node.
     * @return long - number that was sent to the node.
     */
    public long getNumber() {
        return number;
    }

    /**
    *   Method to verify if processing time by a node.
    *   This method can detect if a node is taking an abnormal time to execute the task.
    *   This method could help to avoid attacks, because an attacker could ask for a task,
    *   but never send any answer, affecting final results.
    *   This the execution time is more than the expected, this task could be send to
    *   another node the process it.
    * @return boolean - True, task must be executed on another node.
    *                   False, task must remain on current node.
    */
    public boolean validateProcessingTime(){
        Instant currentTime = Instant.now();
        Duration taskFullTimeExecution = Duration.between( this.startTime, currentTime);
        long longTaskFullTimeExecution = taskFullTimeExecution.toMinutes();

        if (longTaskFullTimeExecution > 5) {
            // If node takes more than 5 minutes to answer, this task is
            // sent to another node.
            return true;
        } else {
            // As task time was not higher than 5 minutes,
            // keeps the task running on the current node.
            return false;
        }
    }
}
