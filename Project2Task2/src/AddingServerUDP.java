/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: AddingServerUDP.java
 * Part Of: Project2Task2
 * Partial code modified from: https://github.com/CMU-Heinz-95702/Project-2-Client-Server
 *
 * This Java file acts as a UDP Server which add the values sent by
 * the client and stores them to a variable 'sum'. After performing
 * addition for each request from the client, it echoes back the sum
 * to the client. The port number that it would listen to is preset.
 * It creates a DatagramSocket and receives a request from the client
 * in the form of a DatagramPacket. The message from the client is in
 * the form of a byte array which is converted to int for performing
 * the operation and the sum is displayed to the user. The sum is
 * then sent to the client. However, if the client sends a message
 * of "halt!", the server will not halt its execution and will keep the
 * value of the 'sum' variable as it was before the client requested
 * for the halt operation. After the client resumes, the server will
 * keep updating the value of the previous sum.
 */

// Imports required for UDP/IP and IO operations
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class AddingServerUDP{

    // Stores the value of the sum
    static int sum = 0;

    /***
     * The main method for the server to set its port, connect
     * with the client, receive a request from the client, perform
     * the addition operation and reply the sum to the client
     * @param args Command line arguments (none here)
     */
    public static void main(String args[]){

        // Prompting the user that the server is running
        System.out.println("Server started");

        // Hard coded port for the server (as suggest on Piazza)
        int port = 6789;

        // Define a UDP style Datagram socket and set the server port to it
        try (DatagramSocket aSocket = new DatagramSocket(port)) {

            // Keep listening to the client's request
            while (true) {

                // Build a byte array buffer of the maximum size possible for the request
                // The maximum size is set by aSocket.getReceiveBufferSize()
                byte[] buffer = new byte[aSocket.getReceiveBufferSize()];

                // Build a DatagramPacket for the request
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                // Block and wait to receive the client request at the port
                aSocket.receive(request);

                // Get the byte array from client's request
                byte[] request_data_bytes = request.getData();

                // Create a new byte array of only the required length (necessary for the message)
                byte[] new_request_data_bytes = new byte[request.getLength()];

                // Copy content from data_bytes to new_data_bytes until the required length
                System.arraycopy(request_data_bytes, 0, new_request_data_bytes, 0, request.getLength());

                // Store the value sent by the client
                int requestInt = 0;

                // Convert byte array to integer
                // Source: https://www.baeldung.com/java-byte-array-to-number#:~:text=The%20Ints%20class%20also%20has,toByteArray(value)%3B
                for (byte b : new_request_data_bytes) {
                    requestInt = (requestInt << 8) + (b & 0xFF);
                }

                // Perform the addition operation and the value into the 'sum' variable
                sum = serverAdd(requestInt, sum);

                // Display the sum to the user and state that the sum is being returned to the client
                System.out.println("Returning sum of " + sum + " to client\n");

                // Convert int sum into byte array
                // Source: https://www.baeldung.com/java-byte-array-to-number#:~:text=The%20Ints%20class%20also%20has,toByteArray(value)%3B
                int sum_copy = sum; // Create a copy if the sum and convert the copy into byte array
                byte[] bytes_reply = new byte[Integer.BYTES];
                int length = bytes_reply.length;
                for (int i = 0; i < length; i++) {
                    bytes_reply[length - i - 1] = (byte) (sum_copy & 0xFF);
                    sum_copy >>= 8;
                }

                // Build a DatagramPacket for the reply containing the sum data after the addition operation
                // The packet includes data in byte array, length of data, destination address and port
                DatagramPacket reply = new DatagramPacket(bytes_reply,
                        bytes_reply.length, request.getAddress(), request.getPort());

                // Send a reply to the client (with the value of the updated sum)
                aSocket.send(reply);
            }
        }
        // Handle socket exceptions
        catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        }
        // Handle general I/O exceptions
        catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    /***
     * Function to perform the addition operation requested by the client
     * @param i Stores the value to add to the client
     * @param sum Stores the current value of the sum variable
     * @return Updated value of the sum variable
     */
    public static int serverAdd(int i, int sum) {

        // Prompt the user about the addition operation
        System.out.println("Adding: " + i + " to " + sum);

        // Return the updated sum
        return i + sum;
    }
}