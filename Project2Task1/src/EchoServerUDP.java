/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: EchoServerUDP.java
 * Part Of: Project2Task1
 * Partial code modified from: https://github.com/CMU-Heinz-95702/Project-2-Client-Server
 *
 * This Java file acts as a UDP Server which simply echoes back to
 * the client (or the eavesdropper) the request it received (message)
 * from the client (or the eavesdropper). It starts off by requesting
 * the user for the port number it should be listening to. Then it
 * creates a DatagramSocket and receives a request from the client
 * (or the eavesdropper) in the form of a DatagramPacket. The message
 * from the client is in the form of a byte array which is converted
 * to String and displayed to the user. The same request message is
 * then sent to the client. However, if the client sends a message
 * of "halt!", the server will halt its execution.
 */

// Imports required for UDP/IP and IO operations
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class EchoServerUDP{
    /***
     * The main method for the server to set its port, connect with the client
     * (or the eavesdropper), receive a request from the client (or the eavesdropper)
     * and perform its echo operation to the client (or the eavesdropper)
     * @param args Command line arguments (none here)
     */
    public static void main(String args[]){

        // Prompting the user that the server is running
        System.out.println("The server is running.");

        // Creating a Scanner object for taking inputs from the user
        Scanner s = new Scanner(System.in);

        // Requesting the user for the port that the server should listen to
        System.out.print("Enter port number that the server is supposed to listen to: ");

        // Getting the server port from the user
        int port = s.nextInt();

        // Define a UDP style Datagram socket and set the server port to it
        try (DatagramSocket aSocket = new DatagramSocket(port)) {

            // Keep listening to the client's (or the eavesdropper's) request
            while (true) {

                // Build a byte array buffer of the maximum size possible for the request
                // The maximum size is set by aSocket.getReceiveBufferSize()
                byte[] buffer = new byte[aSocket.getReceiveBufferSize()];

                // Build a DatagramPacket for the request
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                // Block and wait to receive the client (or the eavesdropper) request at the port
                aSocket.receive(request);

                // Build a DatagramPacket for the reply containing the request data from the client
                // The packet includes data in byte array, length of data, destination address and port
                DatagramPacket reply = new DatagramPacket(request.getData(),
                        request.getLength(), request.getAddress(), request.getPort());

                // Get the byte array from client's (or the eavesdropper's) request
                byte[] data_bytes = request.getData();

                // Create a new byte array of only the required length (necessary for the message)
                byte[] new_data_bytes = new byte[request.getLength()];

                // Copy content from data_bytes to new_data_bytes until the required length
                System.arraycopy(data_bytes, 0, new_data_bytes, 0, request.getLength());

                // Convert the message from the client (or the eavesdropper) into String form
                String requestString = new String(new_data_bytes);

                // Print (echo) the message of the client (or the eavesdropper) to the user
                System.out.println("Echoing: " + requestString);

                // Send a reply to the client (or the eavesdropper) with the same contents as the request
                aSocket.send(reply);

                // If client (or the eavesdropper) messaged to halt
                if (requestString.equals("halt!")) {
                    // Prompt the user that the server is quitting
                    System.out.println("Server side quitting");
                    // Halt server execution
                    System.exit(0);
                }
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
}