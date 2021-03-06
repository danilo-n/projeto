/**
 * This class implements Grid Network Manager.
 * This class controls all tasks sent to users nodes and it storage all the results send by user nodes.
 */
import java.io.*;
import java.util.*;

// Package with network resources.
import java.net.*;

// Packages for data cryptography over network
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


public class manager extends Thread{
    // Certificate location
    public static final String KEYSTORE_LOCATION = "managerKey.jks";
    // Certificate Password.
    public static final String KEYSTORE_PASSWORD = "TCC2017";
    // Socket to connect to each thread to a user node.
    private Socket userNodeSckt;
    // Indicates the first number to start prime number evaluation.
    private static long numberInTest = 10;
    // Vector to storage all running task that their nodes do not send their results.
    private static Vector <taskControl> taskManager = new Vector <taskControl> ();

    /**
     * Constructor
     * @param userNodeSckt - Socket to connection to an user node.
     */
    public manager( Socket userNodeSckt ){
        this.userNodeSckt = userNodeSckt;
    }

    /**
     * Method to print task list.
     */
    private void printTaskList() {
        int auxFind = 0;

        System.out.println ( "Task List has (" +  taskManager.size() + ") tasks.\n");
        for (auxFind = 0; auxFind < taskManager.size(); auxFind++) {
            System.out.println ( "idx[" + auxFind + "] codUser: " +
                        taskManager.elementAt(auxFind).getCodUser() +
                        " number: "+  taskManager.elementAt(auxFind).getNumber() + "\n");
        }
        System.out.println ( "\n");
    }

    /**
     * Method to remove tasks from list.
     * @param number - long, remove task that is process this number.
     * @return boolean - always returns true.
     */
    private boolean removeTaskFromTaskManager( long number ) {
        int auxFind = 0;

        for (auxFind = 0; auxFind < taskManager.size(); auxFind++) {
            if (taskManager.elementAt(auxFind).getNumber() == number) {
                /*
                 * Only removes the task from list.
                 * Do not stop current execution on user nodes, because as user nodes could delay
                 * and manager can detect this as a faulty node, we can have several user nodes
                 * processing the same number.
                 */
                taskManager.removeElementAt(auxFind);
            }
        }

        return true;
    }

    /**
     * Method to check if there are delayed tasks that need to be processed again.
     * @return long, Returns "0" if there is not task delayed. If there are delayed tasks, it returns the number sent
     *               to this user node.
     */
    private long checkDelayedTasks() {
        int auxFind = 0;
        for (auxFind = 0; auxFind < taskManager.size(); auxFind++) {
            if (taskManager.elementAt(auxFind).validateProcessingTime()) {
                // return which number is delaying the full result processing.
                return taskManager.elementAt(auxFind).getNumber() ;
            }
        }

        // return that there is no delayed tasks.
        return 0;
    }

    /**
     * Method to check if the received result was generate by a request start by manager.
     * This verification is used to check is a malicious user node send a result without manager sends to it a task.
     * @param codUser - String, indicates user node identification code, to validate which user node generate the
     *                  result.
     * @param number - long, indicates which number was send to that user node.
     * @return boolean - If True, the result is from a request task by manager, else the user node is invalid.
     */
    private boolean checkTaskFromUserNode( String codUser, long number ) {
        int auxFind = 0;

        printTaskList();

        for (auxFind = 0; auxFind < taskManager.size(); auxFind++) {
            if (( taskManager.elementAt(auxFind).getNumber() == number ) &&
                ( taskManager.elementAt(auxFind).getCodUser().equals(codUser))) {
                /*
                 * Returns that the received result was generated by a task requested by manager.
                 */
                return true;
            }
        }

        /*
         * Returns that the received result was not requested by manager, or was request to another user node.
         * This may indicate an attack to the Grid Network.
         */
        return false;
    }

    /**
     * Method to insert tasks on task list manager.
     * @param codUser - String, indicates user node identification code, to validate which user node the task was sent.
     * @param number - long, indicates which number was send to that user node.
     * @return boolean - Returns if task was insert on list.
     */
    private boolean insertTaskOnTaskManager( String codUser, long number) {
        taskManager.add(new taskControl(codUser, number));
        return true;
    }

    /**
     * Method to generate tasks to user nodes.
     * This method checks if there are delayed tasks and send them to be processed by new user nodes.
     * If there are no delayed tasks it send new tasks to be processed by new user nodes.
     * @param codUser - String, indicates user node identification code which the task was sent.
     * @return long - Returns which number was sent to user node to be processed.
     */
    private synchronized long generateTask( String codUser) {

        System.out.println ( "Generating number to node " + codUser);
        long taskResult = 0;
        taskResult = checkDelayedTasks();

        if ( taskResult > 0) {
            // Indicates that there are delayed tasks that need to be processed.
            insertTaskOnTaskManager( codUser, taskResult);
            System.out.println ( "Sending a delayed task to user node " + codUser + " with number:" + taskResult);
        } else if ( taskResult == 0) {
            taskResult = numberInTest;
            insertTaskOnTaskManager( codUser, numberInTest);
            numberInTest++;
            System.out.println ( "Sending task number:" + taskResult + " to user node " + codUser );
        }

        // This number will be send to user node to be processed.
        return taskResult;
    }

    /**
     * Method to write the receive result in non-volatile data (hard-disk).
     * This method is implemented as "synchronized" to allow only one thread access disk per time.
     * Using "synchronized" we have the guarantee that any other instance are using this method.
     * This removes the necessity to use semaphores.
     * @param codUser - String, indicates user node identification code which generate the result.
     * @param number - long, indicates which number was analyzed by user node.
     * @param result - boolean, true indicates if the analyzed number is a prime number, false not.
     * @return boolean - Returns true if could save result on disk, and false if the operation fails.
     */
    private synchronized boolean writeFile( String codUser, long number, boolean result) {
        if ( ! checkTaskFromUserNode( codUser, number)){
            System.err.println ( "The task send by codUser:" + codUser + " by number (" + number
                                + ") was not requested by manager." );
            return false;
        }

        try {
            // Creates a file in CSV format.
            FileWriter resultFile = new FileWriter("results.csv", true);
            BufferedWriter writeInFile = new BufferedWriter(resultFile);

            String textResult;
            textResult = number + "; " +
                         (result ? "prime number" : "not prime number") + "; " +
                         codUser + "; ";
            writeInFile.write(textResult);
            writeInFile.newLine();
            writeInFile.flush();
            resultFile.close();

            // After write the result on file, remover task from list.
            removeTaskFromTaskManager( number );
        }
        // Verifies if there are any problem to write on file.
        catch ( Exception e ) {
            System.out.println( e );
            return false;
        }

        System.err.println ( "Finishing to write on file." );

        return true;
    }

    /**
     * Method to perform user node login.
     * This verifies if the user is valid and active, and if is trustworthy to receive a task.
     * @param codUser - String, indicates user node identification code
     * @return boolean - Returns true if user node is can access Grid Network, and returns false if user node is cannot
     *                   access Grid Network.
     */
    private boolean userNodeLogin ( String codUser ) {
        try {
            FileReader loginFile = new FileReader("login.csv");
            BufferedReader readLoginFile = new BufferedReader(loginFile);

            String strLogin = readLoginFile.readLine();
            while (strLogin != null) {
                System.out.printf("%s\n", strLogin);
                String arrLogin[] = new String[2];
                arrLogin = strLogin.split(";");

                // Compares the codUser read from file against the codUser send by remote node.
                if (codUser.compareTo(arrLogin[0]) == 0) {
                    // If user is active, login is accept.
                    if (arrLogin[1].compareTo("active") == 0) {
                        System.err.println ( "Allowing login from user node: " + arrLogin[0]);
                        readLoginFile.close();
                        loginFile.close();
                        return true;
                    } else {
                        System.err.println ( "Denying login from user node: " + arrLogin[0]);
                        readLoginFile.close();
                        loginFile.close();
                        return false;
                    }
                }

                // read next registry.
                strLogin = readLoginFile.readLine();
            }
            readLoginFile.close();
            loginFile.close();
        }
        // Verifies if there are any problem to read login file.
        catch ( Exception e ) {
            System.out.println( e );
            return false;
        }

        // If read all file, but do not find the user node, it should fail.
        return false;
    }

    public static void main(String[] args) {
        /*
         * Checks argument list
         */
        if (args.length != 1) {
            System.out.println ( "Usage: gerenteExe <port>" );
            System.out.println ( "Args" + args.length );
            return;
        }

        // Starts Grid Network manager server.
        System.err.println ( "Starting Grid Network manager server." );
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);
        try {
            SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket SrvSckt = (SSLServerSocket) sslserversocketfactory.createServerSocket(Integer.parseInt(args[0]));
            System.err.println ( "Server listing on port: " + Integer.parseInt(args[0]) );

            // Loop for wait user node connections.
            while (true) {
                try {
                    System.err.println ( "Waiting for user node connection..." );
                    Socket newConnection = SrvSckt.accept();

                    System.err.println ( "Starting a thread..." );
                    Thread newUserNode = new manager(newConnection);
                    newUserNode.start();

                } catch ( Exception e) {
                    System.err.println( e );
                    System.err.println ( "There was a failure to provide access to a new user node." );
                }
            }

        } catch ( Exception e ) {
            System.err.println( e );
            System.err.println ( "Grid Network manager fails to create server socket for user nodes." );
        }
    }

    // Execution of communication thread from user nodes to server.
    public void run() {
        try {
            System.err.println ( "Starting thread..." );

            // Start a data channel to receive data.
            InputStream rx = this.userNodeSckt.getInputStream( );
            DataInputStream rxDataChannel = new DataInputStream ( rx );

            // Start a data channel to transmit data.
            OutputStream tx = this.userNodeSckt.getOutputStream( );
            DataOutputStream txDataChannel = new DataOutputStream ( tx );

            // Start a data channel to receive data structures.
            ObjectInputStream rxObjDataChannel = new ObjectInputStream ( rx );

            // Check login information.
            String readCodUser = rxDataChannel.readUTF();
            if ( userNodeLogin(readCodUser)) {
                // Indicates that Manager accept login from user node.
                txDataChannel.writeBoolean(true);
            } else {
                // Indicates that Manager reject login from user node.
                txDataChannel.writeBoolean(false);

                // finishing threat due login rejected.
                System.out.println ( "Finishing connection due login failed.");
                this.userNodeSckt.close();
                return;
            }

            // Verifies which access type user node wants to execute.
            int accessTypeFromUserNode = rxDataChannel.readInt();
            System.err.println ( "Access type: " + accessTypeFromUserNode );

            // Sends a new task to user node.
            if (accessTypeFromUserNode == 1) {
                System.out.println ( "Sending number ( " + numberInTest + ") to be analyzed." );
                txDataChannel.writeLong( generateTask( readCodUser ) );
                System.out.println ( "Next number that will be analyzed: " + numberInTest );

                // Verifies that the user node accept the new task.
                boolean userNodeAcceptTask = rxDataChannel.readBoolean();
                System.out.println ( "User node accept the task: " + userNodeAcceptTask );
            }

            // Receives results from user node.
            if (accessTypeFromUserNode == 2) {
                System.out.println ( "Reading the result from user node." );
                Results receiveResultFromUserNode = null;
                receiveResultFromUserNode = (Results) rxObjDataChannel.readObject();

                // Verifies the result and writes in file.
                if ( ((receiveResultFromUserNode.getResult() == true) &&
                    (receiveResultFromUserNode.getResultCheck() == 15)) ||
                    ((receiveResultFromUserNode.getResult() == false) &&
                    (receiveResultFromUserNode.getResultCheck() == 25)) ){
                    // Write result in file.
                    if ( !writeFile(receiveResultFromUserNode.getCodUser(),
                        receiveResultFromUserNode.getNumber(),
                        receiveResultFromUserNode.getResult()) ){
                        System.out.println ( "Failed to write result for user node: "
                                            + receiveResultFromUserNode.getCodUser());
                    }
                }
                receiveResultFromUserNode = null;
            }

            System.out.println ( "Finishing socket.");
            System.out.println ( "\n\n\n");
            this.userNodeSckt.close();
        } catch ( Exception e ) {
            System.err.println( e );
            System.err.println ( "This thread fails to create a server socket to user nodes." );
        }
    }
}
