/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: RemoteVariableServerUDP.java
 * Part Of: Project2Task3
 * Partial code modified from: https://github.com/CMU-Heinz-95702/Project-2-Client-Server
 *
 * This Java file acts as a UDP Server which add or subtract the values
 * sent by a client and stores them to a variable 'sum' or gets the current
 * sum of the client. After performing the addition or subtraction operation
 * from the client, it echoes back the sum to the client. In this example, the
 * client can make the request from multiple clients (in the form of different
 * client IDs). Each client has a separate sum variable that gets updated or
 * returned once the client requests the required operation. The port number
 * that it would listen to is preset. It creates a DatagramSocket and
 * receives a request from the client in the form of a DatagramPacket. The
 * message from the client is in the form of a byte array which is converted
 * to int for performing the operation and the sum is displayed to the user.
 * The sum is then sent to the client. However, if the client sends a message
 * of "halt!", the server will not halt its execution and will keep the
 * value of the 'sum' variable (for all the clients in the TreeMap) as they were
 * before the client requested for the halt operation. After the client resumes,
 * the server will keep updating the value of the previous 'sum' variables
 * for each client as necessary.
 */

// Imports required for UDP/IP, IO operations and TreeMap
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Objects;
import java.util.TreeMap;

public class RemoteVariableServerUDP{

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
    public static void main(String args[]){

        // Prompting the user that the server is running
        System.out.println("\nServer started\n");

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

                // Stores the user input (operation, operand and client ID) as a String (from a byte array)
                String user_input = new String(new_request_data_bytes);

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

                // Convert int sum into byte array
                // Source: https://www.baeldung.com/java-byte-array-to-number#:~:text=The%20Ints%20class%20also%20has,toByteArray(value)%3B
                int sum_copy = sum;
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