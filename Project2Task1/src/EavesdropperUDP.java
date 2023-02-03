/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: EavesdropperUDP.java
 * Part Of: Project2Task1
 * Partial code modified from: https://github.com/CMU-Heinz-95702/Project-2-Client-Server
 *
 * This Java file acts as a UDP Eavesdropper which acts as a passive
 * Eavesdropper between the UDP Client and the UDP Server. After
 * running, the Eavesdropper will ask the user for two ports. One port
 * will be the port that the EavesdropperUDP.java will listen on and the
 * other port will be the port number of the server that Eavesdropper.java
 * is masquerading as. The Eavesdropper will display all the messages that
 * go through it (both from the Server and the Client). However, if the
 * client requested for a "halt!" request, it will display a line of
 * asterisks, pass the message to the EchoServer, receive the echo
 * from the Server, and pass the echo to the Client. In the halt execution,
 * both the client and the server will halt their executions, but the
 * Eavesdropper will run forever. Moreover, if the client directly connects
 * to the server, the Eavesdropper will not come into the picture.
 */

// imports required for UDP/IP and IO operations
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class EavesdropperUDP {

    /***
     * The main method for the Eavesdropper to act as a passive malicious player in the middle.
     * It sets the port number of the server and itself, receives the request from the client,
     * passses the request to the server, receive the reply from the server and passes the reply
     * for the client.
     * @param args Command line arguments (none here)
     */
    public static void main(String args[]){

        // Prompting the user that the Eavesdropper is running
        System.out.println("The Eavesdropper is running.");

        // Creating a Scanner object for taking inputs from the user
        Scanner s = new Scanner(System.in);

        // Requesting the user for Eavesdropper port
        System.out.print("Enter evasdropper port number: ");

        // Getting the Eavesdropper port from the user
        int eavesdropperPort = s.nextInt();

        // Requesting the user for the server side port
        System.out.print("Enter server side port number: ");

        // Getting the server port from the user
        int serverPort = s.nextInt();

        // Define a UDP style Datagram socket and set the server port to it
        try (DatagramSocket eavesdropper_Socket = new DatagramSocket(eavesdropperPort)) {

            // Keep listening to the client's request
            while (true) {

                // Part 1: Request from client
                // Build a byte array buffer of the maximum size possible for the request
                // The maximum size is set by aSocket.getReceiveBufferSize()
                byte[] buffer = new byte[eavesdropper_Socket.getReceiveBufferSize()];

                // Build a DatagramPacket for the request from client
                DatagramPacket evasdropper_request_from_client = new DatagramPacket(buffer, buffer.length);

                // Block and wait to receive the client request at the port
                eavesdropper_Socket.receive(evasdropper_request_from_client);

                // Build a DatagramPacket for the request to the server containing the request data
                // from the client. The packet includes data in byte array, length of data, destination
                // address and port
                DatagramPacket request_to_server = new DatagramPacket(evasdropper_request_from_client.getData(),
                        evasdropper_request_from_client.getLength(), evasdropper_request_from_client.getAddress(),
                        serverPort);

                // Get the byte array from client's request
                byte[] request_data_bytes_from_client = evasdropper_request_from_client.getData();

                // Create a new byte array of only the required length (necessary for the message)
                byte[] new_request_data_bytes_from_client = new byte[evasdropper_request_from_client.getLength()];

                // Copy content from data_bytes to new_data_bytes until the required length
                System.arraycopy(request_data_bytes_from_client, 0, new_request_data_bytes_from_client, 0, evasdropper_request_from_client.getLength());

                // Convert the message from the client into String form
                String evasdropper_requestString_from_client = new String(new_request_data_bytes_from_client);

                // If the client requested for a "halt!"
                if (evasdropper_requestString_from_client.equals("halt!")) {
                    // Print a special message to the user (a line of asterisks)
                    System.out.println("*******************************");
                }

                // Print (echo) the message of the client to the user
                System.out.println("Request message from client = " + evasdropper_requestString_from_client);

                // Part 2: Request to server
                // Send a request to the server (with the same contents as the request)
                eavesdropper_Socket.send(request_to_server);

                // Part 3: Reply from server
                // Build a byte array buffer of the maximum size possible for the reply
                // The maximum size is set by aSocket.getSendBufferSize()
                buffer = new byte[eavesdropper_Socket.getSendBufferSize()];

                // Build a DatagramPacket for the reply
                DatagramPacket reply_from_server = new DatagramPacket(buffer, buffer.length);

                // Block and wait to receive the reply
                eavesdropper_Socket.receive(reply_from_server);

                // Convert the data of the reply into byte array
                byte[] reply_data_bytes_from_server = reply_from_server.getData();

                // Create a new byte array of only the required length (necessary for the message)
                byte[] new_reply_data_bytes_from_server = new byte[reply_from_server.getLength()];

                // Copy content from data_bytes to new_data_bytes until the required length
                System.arraycopy(reply_data_bytes_from_server, 0, new_reply_data_bytes_from_server, 0, reply_from_server.getLength());

                // Convert the reply byte array into String
                String evasdropper_replyString_from_server = new String(new_reply_data_bytes_from_server);

                // Print the reply to the user
                System.out.println("Reply message from server: " + evasdropper_replyString_from_server);
                System.out.println();

                // Part 4: Reply to client
                // Build a DatagramPacket for the reply containing the request data from the client
                // The packet includes data in byte array, length of data, destination address and port
                DatagramPacket reply_to_client = new DatagramPacket(reply_from_server.getData(),
                        reply_from_server.getLength(), reply_from_server.getAddress(), evasdropper_request_from_client.getPort());

                // Send a reply to the client (with the contents from the server)
                eavesdropper_Socket.send(reply_to_client);
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