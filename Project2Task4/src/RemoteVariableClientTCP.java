/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: RemoteVariableClientTCP.java
 * Part Of: Project2Task4
 * Partial code modified from: https://github.com/CMU-Heinz-95702/Project-2-Client-Server
 *
 * This Java file acts as a TCP Client which sends a request to
 * the server. The request is made of client ID, operation (add,
 * subtract, get) and operand. The operand is added/subtracted
 * from the sum and the updated sum is returned to the client or
 * the sum is directly returned from the server if the client made
 * a get request. The client receives the value of the sum (of the
 * client ID) and prints it to the user. The client sends a packet
 * to the server and waits for the server to execute the requested
 * operation. When the response packet arrives from the server, the
 * client creates an int object and displays the output to the user.
 * If the client wishes to halt its execution, the server will not
 * be affected.
 */

// imports required for TCP/IP and IO operations
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class RemoteVariableClientTCP {

    // Stores the value of port number of the server
    static int serverPort;

    /***
     * The main method for the client to request for the server port from the user,
     * request a message (client ID, operation, operand) from the user, send a request
     * message to the server, receive a reply from the server (sum of the client ID)
     * and print the reply to the user
     * @param args Command line arguments (none here)
     */
    public static void main(String args[]) throws IOException {

        // Prompting the user that the client is running
        System.out.println("\nThe client is running.");

        // Creating a Scanner object for taking inputs from the user
        Scanner s = new Scanner(System.in);

        // Requesting the user for the server side port
        System.out.print("Please enter server port: ");

        // Getting the server port from the user
        serverPort = s.nextInt();
        System.out.println();

        // Stores the input from the user
        String user_input;

        // Create a BufferReader object to get input from the user
        BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));

        // Keeping executing until the client is terminated
        while (true) {

            // Initialize user input to null
            user_input = "";

            // Prompt the user for operation
            System.out.println("""
                    1. Add a value to your sum.
                    2. Subtract a value from your sum.
                    3. Get your sum.
                    4. Exit client""");

            // Update user input
            user_input = user_input + typed.readLine();

            // Switch case for user input
            switch (user_input) {

                // If user requested for an addition operation
                case "1" -> {

                    // Prompt the user for an operand
                    System.out.println("Enter value to add:");

                    // Update user input
                    user_input = user_input + "," + typed.readLine() + ",";
                }

                // If user requested for a subtraction operation
                case "2" -> {
                    // Prompt the user for an operand
                    System.out.println("Enter value to subtract:");

                    // Update user input
                    user_input = user_input + "," + typed.readLine() + ",";
                }

                // If user requested for a get operation
                case "3" -> user_input = user_input + ",,";

                // If user requested for halting
                case "4" -> {
                    // Prompt the user that the client is quitting
                    System.out.println("Client side quitting. The remote variable server is still running.");
                    // Halt client execution
                    System.exit(0);
                }
            }

            // Prompt user for client ID
            System.out.println("Enter your ID:");

            // Update user input
            user_input = user_input + typed.readLine();

            // Request the operation from server and store the value of sum
            int serverSumReturned = operations(user_input);

            // Display the sum received from the server to the user
            System.out.println("The result is " + serverSumReturned + ".\n");
        }
    }

    /***
     * Function to communicate with the server and perform the required
     * operation on the requested integer value by the client
     * @param user_input Input from the user containing client ID, operation and operand
     * @return Updated sum from the server
     */
    public static int operations(String user_input) {

        // Define a TCP style Socket
        Socket clientSocket = null;

        // Stores the sum returned from the server
        int serverSumReturned = 0;

        try {
            // Creating a String object for localhost
            String localhost = "";

            // Updating clientSocket with localhost and the server port
            clientSocket = new Socket(localhost, serverPort);

            // Set up "in" to read from the server socket
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Set up "out" to write to the server socket
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

            // Request to the server with the user_input
            out.println(user_input);

            // Flush to server socket
            out.flush();

            // Store the sum returned from the server and parse it to integer
            serverSumReturned = Integer.parseInt(in.readLine()); // read a line of data from the stream
        }
        // Handle general I/O exceptions
        catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
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

        // Return the updated sum
        return serverSumReturned;
    }
}