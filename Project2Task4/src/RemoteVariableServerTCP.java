/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: RemoteVariableServerTCP.java
 * Part Of: Project2Task4
 * Partial code modified from: https://github.com/CMU-Heinz-95702/Project-2-Client-Server
 *
 * This Java file acts as a TCP Server which add or subtract the values
 * sent by a client and stores them to a variable 'sum' or gets the current
 * sum of the client. After performing the addition or subtraction operation
 * from the client, it echoes back the sum to the client. In this example, the
 * client can make the request from multiple clients (in the form of different
 * client IDs). Each client has a separate sum variable that gets updated or
 * returned once the client requests the required operation. The port number
 * that it would listen to is preset. It creates a Socket and receives a request
 * from the client. The message from the client is in the form of String which is
 * processed to perform the necessary operations. The sum for the client is then
 * sent to the client. However, if the client request to halt, the server will not
 * halt its execution and will keep the value of the 'sum' variable (for all the
 * clients in the TreeMap) as they were before the client requested for the halt
 * operation.
 */

// Imports required for TCP/IP, IO operations and TreeMap
import java.net.*;
import java.io.*;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;

public class RemoteVariableServerTCP {

    // Stores the value of the sum to be returned to the client
    static int sum;

    // Create a TreeMap to store each client and its sum
    // Source: https://www.geeksforgeeks.org/treemap-in-java/
    static TreeMap<Integer, Integer> sum_tree_map = new TreeMap<Integer, Integer>();

    /***
     * The main method for the server to set its port, connect
     * with the client, receive a request from the client, perform
     * the operation and reply the sum to the client
     * @param args Command line arguments (none here)
     */
    public static void main(String args[]) {

        // Prompting the user that the server is running
        System.out.println("\nServer started\n");

        // Define a TCP style Socket
        Socket clientSocket = null;

        // Define a TCP style ServerSocket
        ServerSocket listenSocket;
        try {

            // Hard coded port for the server (as suggest on Piazza)
            int serverPort = 6789;

            // Create a new server socket
            listenSocket = new ServerSocket(serverPort);

            /*
             * Forever,
             *   read a line from the socket
             *   print it to the console
             *   echo it (i.e. write it) back to the client
             */
            while (true) {
                /*
                 * Block waiting for a new connection request from a client.
                 * When the request is received, "accept" it, and the rest
                 * the tcp protocol handshake will then take place, making
                 * the socket ready for reading and writing.
                 */
                clientSocket = listenSocket.accept();
                // If we get here, then we are now connected to a client.

                // Set up "in" to read from the client socket
                Scanner in;
                in = new Scanner(clientSocket.getInputStream());

                // Set up "out" to write to the client socket
                PrintWriter out;
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

                // Get the input (operation, operand and client ID) from the client and store it in a String object
                String user_input = in.nextLine();

                // Split user_input based on a comma separator
                String[] instructions = user_input.split(",");

                // Display visitor ID to the user
                System.out.println("Visitor ID: " + instructions[2]);

                // If client requested for an addition operation
                if (Objects.equals(instructions[0], "1")) {

                    // Display the requested operation to the user
                    System.out.println("Operation Requested: " + instructions[0] + ". Add");

                    // Perform addition on client's sum and store the result into a general sum
                    // variable that would be returned to the client
                    sum = serverAdd(Integer.parseInt(instructions[2]), Integer.parseInt(instructions[1]));
                }
                // If client requested for a subtraction operation
                else if (Objects.equals(instructions[0], "2")) {

                    // Display the requested operation to the user
                    System.out.println("Operation Requested: " + instructions[0] + ". Subtract");

                    // Perform subtraction on client's sum and store the result into a general sum
                    // variable that would be returned to the client
                    sum = serverSubtract(Integer.parseInt(instructions[2]), Integer.parseInt(instructions[1]));
                }
                // If client requested for a get operation
                else if (Objects.equals(instructions[0], "3")) {

                    // Display the requested operation to the user
                    System.out.println("Operation Requested: " + instructions[0] + ". Get");

                    // Perform get on client's sum and store the result into a general sum
                    // variable that would be returned to the client
                    sum = serverGet(Integer.parseInt(instructions[2]));
                }

                // Display the sum to the user and state that the sum is being returned to the client
                System.out.println("Returning sum of " + sum + " to client.\n");

                // Reply the sum to the client
                out.println(sum);

                // Flush to client socket
                out.flush();
            }
        }
        // Handle IO exceptions
        catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
            // If quitting (typically by you sending quit signal) clean up sockets
        }
        // Always close the socket
        finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }

    /***
     * Function to perform the addition operation on the sum variable of the
     * client who made the request (using the client ID)
     * @param client_id Client ID of the client who made the request
     * @param i Value to add to the sum of the client ID
     * @return Updated value of the sum variable of the client ID
     */
    public static int serverAdd(int client_id, int i) {

        // If TreeMap contains the client ID
        if (sum_tree_map.containsKey(client_id)) {
            // Update the sum of the client by adding i
            sum_tree_map.put(client_id, sum_tree_map.get(client_id) + i);
        }
        // If TreeMap does not contain the client ID
        else {
            // Set the new sum of the client to be i
            sum_tree_map.put(client_id, i);
        }
        // Return the updated sum of the client
        return sum_tree_map.get(client_id);
    }

    /***
     * Function to perform the subtraction operation on the sum variable of the
     * client who made the request (using the client ID)
     * @param client_id Client ID of the client who made the request
     * @param i Value to subtract from the sum of the client ID
     * @return Updated value of the sum variable of the client ID
     */
    public static int serverSubtract(int client_id, int i) {

        // If TreeMap contains the client ID
        if (sum_tree_map.containsKey(client_id)) {
            // Update the sum of the client by subtracting i
            sum_tree_map.put(client_id, sum_tree_map.get(client_id) - i);
        }
        // If TreeMap does not contain the client ID
        else {
            // Set the new sum of the client to be -i
            sum_tree_map.put(client_id, -i);
        }
        // Return the updated sum of the client
        return sum_tree_map.get(client_id);
    }

    /***
     * Function to get the sum of the client who requested for it (using the
     * client ID)
     * @param client_id Client ID of the client who made the request
     * @return Sum variable of the client ID
     */
    public static int serverGet(int client_id) {

        // If TreeMap does not contain the client ID
        if (!sum_tree_map.containsKey(client_id)) {
            // Initialize the sum of the client ID to be 0
            sum_tree_map.put(client_id, 0);
        }
        // Return sum of the client ID
        return sum_tree_map.get(client_id);
    }
}