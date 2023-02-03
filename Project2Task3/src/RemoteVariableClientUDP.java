/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: RemoteVariableClientUDP.java
 * Part Of: Project2Task3
 * Partial code modified from: https://github.com/CMU-Heinz-95702/Project-2-Client-Server
 *
 * This Java file acts as a UDP Client which sends a request to
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

// imports required for UDP/IP and IO operations
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class RemoteVariableClientUDP {

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
        String user_input = "";

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

        // Convert user_input to byte array
        byte[] user_inputs_in_bytes = user_input.getBytes();

        // Define a UDP style Datagram socket
        DatagramSocket aSocket = null;

        // Stores the sum returned from the server
        int serverSumReturned = 0;
        try {
            // Creating a String object for localhost
            String localhost = "";

            // Build an InetAddress object from a DNS name
            InetAddress aHost = InetAddress.getByName(localhost);

            // Creating a new DatagramSocket
            aSocket = new DatagramSocket();

            // Build the packet holding the byte array of user input, its length,
            // destination address, and port
            DatagramPacket request = new DatagramPacket(user_inputs_in_bytes, user_inputs_in_bytes.length,
                    aHost, serverPort);

            // Send the Datagram on the socket to the server
            aSocket.send(request);

            // Build a byte array buffer of the maximum size possible for the reply
            // The maximum size is set by aSocket.getSendBufferSize()
            byte[] buffer = new byte[aSocket.getSendBufferSize()];

            // Build a DatagramPacket for the reply
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            // Block and wait to receive the reply
            aSocket.receive(reply);

            // Convert the data of the reply into byte array
            byte[] reply_data_bytes = reply.getData();

            // Create a new byte array of only the required length (necessary for the message)
            byte[] new_reply_data_bytes = new byte[reply.getLength()];

            // Copy content from data_bytes to new_data_bytes until the required length
            System.arraycopy(reply_data_bytes, 0, new_reply_data_bytes, 0, reply.getLength());

            // Convert the byte array of the reply into int
            // Source: https://www.baeldung.com/java-byte-array-to-number#:~:text=The%20Ints%20class%20also%20has,toByteArray(value)%3B
            for (byte b : new_reply_data_bytes) {
                serverSumReturned = (serverSumReturned << 8) + (b & 0xFF);
            }

        }
        // Handle socket exceptions
        catch (
                SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        }
        // Handle general I/O exceptions
        catch (
                IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
        // Always close the socket
        finally {
            if (aSocket != null) aSocket.close();
        }

        // Return the updated sum
        return serverSumReturned;
    }
}