/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: EchoClientUDP.java
 * Part Of: Project2Task1
 * Partial code modified from: https://github.com/CMU-Heinz-95702/Project-2-Client-Server
 *
 * This Java file acts as a UDP Client which simply sends a
 * message to the server and print the message from the server
 * to the user. The server in this case is just echoing back
 * client's request to the client. So, the client will output
 * whatever it sent to the server. It sends a packet to the
 * server and waits for the server to execute the requested
 * operation. When the response packet arrives from the server,
 * the client creates a String object and displays the output
 * to the user. If the client sends a message of "halt!", to
 * the server, the server will halt its execution and relay
 * back the same message to the client. Hence, the client will
 * also halt its execution.
 * However, while setting the server side prt number, if the
 * client mistakenly enters the eavesdropper's port number,
 * it would be connected with the eavesdropper and the
 * eavesdropper would be able to read the messaages to and from
 * the client.
 */

// imports required for UDP/IP and IO operations
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class EchoClientUDP{
    /***
     * The main method for the client to request for the server port from the user,
     * request a message from the user, send a request message to the server, receive
     * a reply from the server and print the reply to the user
     * @param args Command line arguments (none here)
     */
    public static void main(String args[]){

        // Prompting the user that the client is running
        System.out.println("The client is running.");

        // Creating a Scanner object for taking inputs from the user
        Scanner s = new Scanner(System.in);

        // Requesting the user for the server side port
        System.out.print("Enter server side port number: ");

        // Getting the server port from the user
        int serverPort = s.nextInt();

        // Define a UDP style Datagram socket
        DatagramSocket aSocket = null;
        try {
            // Creating a String object for localhost
            String localhost = "";

            // Build an InetAddress object from a DNS name
            InetAddress aHost = InetAddress.getByName(localhost);

            // Creating a new DatagramSocket
            aSocket = new DatagramSocket();

            // Stores the message from the user
            String nextLine;

            // Prompt the user to enter a message to send to the server
            System.out.print("Enter request message to server: ");

            // Create a BufferReader object to get input from the user
            BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));

            // Until the user input is not null, read an input message from the user
            while ((nextLine = typed.readLine()) != null) {

                // Create a byte array of the user input
                byte [] m = nextLine.getBytes();

                // Build the packet holding the byte array of user input, its length,
                // destination address, and port
                DatagramPacket request = new DatagramPacket(m,  m.length, aHost, serverPort);

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

                // Convert the reply byte array into String
                String replyString = new String(new_reply_data_bytes);

                // Print the reply to the user
                System.out.println("Reply: " + replyString);

                // If the reply from the server is not "halt!"
                if (!replyString.equals("halt!")) {
                    // Prompt the user for another message
                    System.out.print("Enter request message to server: ");
                }
                // If the server request the client to halt
                else {
                    // Prompt the user that the client is quitting
                    System.out.println("Client side quitting");
                    // Halt client execution
                    System.exit(0);
                }
            }

        }
        // Handle socket exceptions
        catch (SocketException e) {System.out.println("Socket: " + e.getMessage());
        }
        // Handle general I/O exceptions
        catch (IOException e){System.out.println("IO: " + e.getMessage());
        }
        // Always close the socket
        finally {if(aSocket != null) aSocket.close();}
    }
}