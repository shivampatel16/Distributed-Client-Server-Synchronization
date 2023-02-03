/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: AddingClientUDP.java
 * Part Of: Project2Task2
 * Partial code modified from: https://github.com/CMU-Heinz-95702/Project-2-Client-Server
 *
 * This Java file acts as a UDP Client which sends a value to
 * the server. The value is added to a sum variable in the sum
 * and the updated sum is returned to the client. The client
 * receives the value of the updated sum and prints it to the
 * user. The client sends a packet to the server and waits for
 * the server to execute the requested operation. When the response
 * packet arrives from the server, the client creates an int
 * object and displays the output to the user. If the client
 * sends a message of "halt!" to the server, the server will not
 * be affected; however, the client will stop its execution.
 */

// imports required for UDP/IP and IO operations
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class AddingClientUDP {

    // Stores the value of port number of the server
    static int serverPort;

    /***
     * The main method for the client to request for the server port from the user,
     * request a message from the user, send a request message to the server, receive
     * a reply from the server and print the reply to the user
     * @param args Command line arguments (none here)
     */
    public static void main(String args[]) throws IOException {

        // Prompting the user that the client is running
        System.out.println("The client is running.");

        // Creating a Scanner object for taking inputs from the user
        Scanner s = new Scanner(System.in);

        // Requesting the user for the server side port
        System.out.print("Please enter server port: ");

        // Getting the server port from the user
        serverPort = s.nextInt();
        System.out.println();

        // Stores the input from the user
        String nextLine;

        // Create a BufferReader object to get input from the user
        BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));

        // Until the user input is not null, read an input message from the user
        while ((nextLine = typed.readLine()) != null) {

            // Create a byte array of the user input
            byte[] m = nextLine.getBytes();

            // If client request for a "halt!"
            if (nextLine.equals("halt!")) {
                // Prompt the user that the client is quitting
                System.out.println("Client side quitting.\n");
                // Halt client execution
                System.exit(0);
            }

            // Convert byte array of input to String
            String user_string = new String(m);

            // Parse user String to int
            int user_int = Integer.parseInt(user_string);

            // Request the addition operation from server and store the value of sum
            int serverSumReturned = add(user_int);

            // Display the sum received from the server to the user
            System.out.println("The server returned " + serverSumReturned + ".");
        }
    }

    /***
     * Function to communicate with the server and perform the addition
     * operation on the requested integer value by the client
     * @param i Integer value to add to the sum
     * @return Updated sum from the server
     */
    public static int add(int i) {

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

            // Convert user int to byte array
            // Source: https://www.baeldung.com/java-byte-array-to-number#:~:text=The%20Ints%20class%20also%20has,toByteArray(value)%3B
            byte[] user_int_bytes = new byte[Integer.BYTES];
            int length = user_int_bytes.length;
            for (int j = 0; j < length; j++) {
                user_int_bytes[length - j - 1] = (byte) (i & 0xFF);
                i >>= 8;
            }

            // Build the packet holding the byte array of user input, its length,
            // destination address, and port
            DatagramPacket request = new DatagramPacket(user_int_bytes, user_int_bytes.length,
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