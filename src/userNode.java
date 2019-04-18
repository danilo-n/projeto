/**
 * This class implements user's node.
 * Here are implements all the resources necessary to user's node works.
 * This class executes the verification of a prime number, and executes all
 * the communications between user node and grid network manager.
 */

// Packages for network access
import java.net.*;

// Packages for data cryptography over network
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

// Packages for input/output
import java.io.*;
import java.math.BigInteger;


public class userNode {

    private String codNode;
    private long numberToVerify;
    private boolean testResult;
    public static final String TRUSTTORE_LOCATION = "userNodeKey.jks";

    /**
     * Constructor
     * @param codNode String - Indicates user's code over Grid Network.
     */
    public userNode(String codNode) {
        this.codNode = codNode;
        this.numberToVerify = 0;
        this.testResult = false;
    }

    /**
     * Method to configure a number to be verified as a prime number.
     * @param numberToVerify - long, number to be verified as a prime number.
     */
    private void setNumber(long numberToVerify) {
        this.numberToVerify = numberToVerify;
    }

    /**
     * Method to configure the result if the number tested is a prime number or not.
     * @param testResult - boolean, if True the number tested is a prime number,
     *                              if False the number tested is not a prime number.
     */
    private void setResult(boolean testResult) {
        this.testResult = testResult;
    }

    /**
     * Method to get node user's code.
     * @return String, node user's code.
     */
    public String getCodNode(){
        return codNode;
    }

    /**
     * Method to get which number is selected to be verified as a prime number.
     * @return long, number to verified as a prime number.
     */
    public long getNumber(){
        return numberToVerify;
    }

    /**
     * Method to get the result if the number tested is a prime number or not.
     * @return boolean, if True the number tested is a prime number,
     *                  if False the number tested is not a prime number.
     */
    public boolean getResult(){
        return testResult;
    }

    /**
     * Method to configure connection between user node and Grid Network manager server.
     * @param accessType - int, indicates the connection type that user node wants to have with
     *                     Grid Network manager.
     *                     Value "1" - Indicates that user node wants to receive a new task;
     *                     Value "2" - Indicates that user node will send the results.
     * @param serverAddr - String, Grid Network server address.
      * @param serverPort - int, Grid Network server port.
     * @return boolean - If true, indicates that connection was established, if False, errors has detected.
     */
    public boolean serverConnection( int accessType, String serverAddr, int serverPort) {
        try {
            System.out.println( "Trying connection with Grid Network Server ..." );

            System.setProperty("javax.net.ssl.trustStore", TRUSTTORE_LOCATION);
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();

            /* Adding encryption to connection */
            Socket socketServidor = ssf.createSocket(serverAddr, serverPort);
            SSLSession session = ((SSLSocket) socketServidor).getSession();

            // Checks if certificate if valid.
            System.out.println("Peer host � " + session.getPeerHost());
            System.out.println("Cifra � " + session.getCipherSuite());
            System.out.println("Protocolo � " + session.getProtocol());
            System.out.println("ID � " + new BigInteger(session.getId()));
            System.out.println("Session criada no " + session.getCreationTime());
            System.out.println("Session acessada no " + session.getLastAccessedTime());

            System.out.println( "Connected to server" );

            // Starts a data channel to receive data from server.
            InputStream rx = socketServidor.getInputStream( );
            DataInputStream rxDataCh = new DataInputStream ( rx );

            // Starts a data channel to transmit data to server.
            OutputStream tx = socketServidor.getOutputStream( );
            DataOutputStream txDataCh = new DataOutputStream ( tx );

            // Starts a data channel to transmit data structs to server
            ObjectOutputStream txObjDataCh = new ObjectOutputStream ( tx );

            // Send login data to server
            txDataCh.writeUTF(codNode);
            if ( !rxDataCh.readBoolean() ) {
                System.out.println( "Server could not validate login, finishing..." );
                return false;
            }

            // Indicates access type to server
            System.out.println ( "Connected to server, " +
                                 (accessType == 1 ? "ask for new task" : "start sending results") +
                                 ". Code access type: " +  accessType);
            txDataCh.writeInt(accessType);

            // Process the receiving task state.
            if (accessType == 1) {
                // Receives a new task from server, in this case, a number to verify if it is a prime number.
                long numberToVerify = rxDataCh.readLong();
                setNumber(numberToVerify);
                System.out.println ( "Received number " + getNumber() );
                System.out.println ( "Inform to server that this node will start data processing.");
                txDataCh.writeBoolean(true);
            }

            // Process the send results state.
            if (accessType == 2) {
                int resultCheck = 0;
                if (getResult()){
                    // IF testResult equal true.
                    resultCheck = 15;
                } else {
                    // IF testResult equal false.
                    resultCheck = 25;
                }

                // Creates a data structure to send result information to server.
                Results sendResults = new Results(getCodNode(), getNumber(), getResult(), resultCheck);
                System.out.println ( "Sending results to server...");
                // Sends struct with answer.
                txObjDataCh.writeObject(sendResults);
                txObjDataCh.flush();
                System.out.println ( "Results successfuly send to server.");
            }

            /* As the data process could take too much long time to get a result, we should close
             * socket to avoid let a unused connection opened, this could be a security issue on
             * the node system.
             */
            System.out.println ( "Closing socket...\n" );
            socketServidor.close();
        }
        // Catch to detect some problem while trying to connect.
        catch ( Exception e ) {
            System.out.println( e );
            return false;
        }
        return true;
    }

    /**
     * Method to process the requested task by server.
     * Here the processing is to check if a number is a prime number.
     * @return boolean - True if number is prime, false if not.
     */
    public boolean processTask(){
        // Storage how many dividers where found.
        int numDividers = 0;
        for (long i = 1; i <= getNumber(); i++){
            if ((getNumber() % i) == 0) {
                numDividers++;
            }
        }

        if (numDividers == 2){
            // the number tested is a prime number.
            setResult(true);
            System.out.println ( "the number " + getNumber() + " is a prime number.\n" );
        } else {
            // the number tested is not a prime number.
            setResult(false);
            System.out.println ( "the number " + getNumber() + " is a not prime number.\n" );
        }
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        // TODO Auto-generated method stub
        System.out.println ( "Starting user node...\n" );

        // Checks if argument list is correctly.
        // Example: userNode <codUser> <IP Manager> <Port Manager>
        if ( (args.length < 3) || (args.length > 4) ) {
            System.out.println ( "Usage: userNode <codUser> <IP Manager> <Port Manager> <pause>(optinal)" );
            System.out.println ( "Args" + args.length );
            return;
        }

        // Creates an object to userNode.
        userNode objTestPrimeNum = new userNode(args[0]);

        // Starting a connection to ask for a new task to Grid Network Server.
        if ( !objTestPrimeNum.serverConnection(1, args[1], Integer.parseInt(args[2])) ){
            System.out.println ( "Failed to start a connection to server, could not get a new task.\n" );
            return;
        }

        // Starts process the task.
        if ( !objTestPrimeNum.processTask() ){
            System.out.println ( "Failed to process task.\n" );
        }

        /*
         * Inserts a delay to simulate a different time to each node process the task.
         * This makes that each node send their results in different moment.
         * User could insert a delay by setting pause parameter.
         */
        if (args.length == 3) {
            int delayTime = (int) Math.round( Math.random() * (20));
            System.out.println ( "Start a delay of (" + delayTime + ") seconds.\n" );
            Thread.sleep( (delayTime *1000) );
        } else {
            Thread.sleep( (Integer.parseInt(args[3]) *1000) );
        }

        // Starting a connection to send results to Grid Network Server.
        if ( !objTestPrimeNum.serverConnection(2, args[1], Integer.parseInt(args[2])) ){
            System.out.println ( "Failed to start a connection to server, could not send results.\n" );
            return;
        }

        System.out.println ( "Finishing user node.\n" );
    }
}
