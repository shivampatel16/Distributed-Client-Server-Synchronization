/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: VerifyingServerTCP.java
 * Part Of: Project2Task5
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
 * The server will make two checks before servicing any client request. First,
 * does the public key (included with each request) hash to the ID (also provided
 * with each request)? Second, is the request properly signed? If both of these
 * are true, the request is carried out on behalf of the client. The server will add,
 * subtract or get. Otherwise, the server returns the message "Error in request".
 */

// Imports required for TCP/IP, IO operations, TreeMap, BigInteger, SHA256, Arrays
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;

public class VerifyingServerTCP {

    // Stores the value of the sum to be returned to the client
    static int sum;

    // Create a TreeMap to store each client and its sum
    // Source: https://www.geeksforgeeks.org/treemap-in-java/
    static TreeMap<String, Integer> sum_tree_map = new TreeMap<String, Integer>();

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

                // Get the input (Client ID, e, n, operation, operand, signature) from the client and store it in a String object
                String user_input = in.nextLine();

                // Split user_input based on a comma separator
                String[] request_split = user_input.split(",");

                // Use the request_split to populate the values for client ID, e, n, user_operation,
                // operand, and encryptedHashStr
                String client_ID = request_split[0];
                BigInteger e = new BigInteger(request_split[1]);
                BigInteger n = new BigInteger(request_split[2]);
                String user_operation = request_split[3];
                String operand = request_split[4];
                String encryptedHashStr = request_split[5];

                System.out.println("Visitor's Public Key Material: ");
                System.out.println(" e = " + e);
                System.out.println(" n = " + n);

                System.out.println("\nVisitor's Public Key (e, n) = (" + e + ", \n" + n + ")\n");

                // Stores the result of the public key and client ID verification
                boolean public_key_client_ID_verification_result;

                // Stores the result of signature verification
                boolean signature_verification_result;

                // Generate the message to check
                String messageToCheck = request_split[0] + "," + request_split[1] + "," +
                        request_split[2] + "," + request_split[3] + "," + request_split[4];

                // Verify signature
                signature_verification_result = verifySignature(messageToCheck, encryptedHashStr, e, n);
                if (signature_verification_result) {
                    System.out.println("Signature verified!");
                }
                else {
                    System.out.println("Signature not verified!");
                }

                // Verify public key hash client ID
                public_key_client_ID_verification_result = verifyPublicKeyClientID(client_ID, e, n);
                if (public_key_client_ID_verification_result) {
                    System.out.println("\nPublic Key hash Client ID verified!\n");
                }
                else {
                    System.out.println("\nPublic Key hash Client ID not verified!\n");
                }

                // If both the above verifications are correct
                if (signature_verification_result && public_key_client_ID_verification_result) {

                    // Display visitor ID to the user
                    System.out.println("Visitor ID: " + client_ID);

                    // If client requested for an addition operation
                    if (Objects.equals(user_operation, "1")) {

                        // Display the requested operation to the user
                        System.out.println("Operation Requested: " + user_operation + ". Add");

                        // Perform addition on client's sum and store the result into a general sum
                        // variable that would be returned to the client
                        sum = serverAdd(client_ID, Integer.parseInt(operand));
                    }
                    // If client requested for a subtraction operation
                    else if (Objects.equals(user_operation, "2")) {

                        // Display the requested operation to the user
                        System.out.println("Operation Requested: " + user_operation + ". Subtract");

                        // Perform subtraction on client's sum and store the result into a general sum
                        // variable that would be returned to the client
                        sum = serverSubtract(client_ID, Integer.parseInt(operand));
                    }
                    // If client requested for a get operation
                    else if (Objects.equals(user_operation, "3")) {

                        // Display the requested operation to the user
                        System.out.println("Operation Requested: " + user_operation + ". Get");

                        // Perform get on client's sum and store the result into a general sum
                        // variable that would be returned to the client
                        sum = serverGet(client_ID);
                    }

                    // Display the sum to the user and state that the sum is being returned to the client
                    System.out.println("Returning sum of " + sum + " to client.\n");

                    // Reply the sum to the client
                    out.println(sum);

                    // Flush to client socket
                    out.flush();
                }
                // If one or both the above verifications are not correct
                else {
                    // Display error message to the user
                    System.out.println("Error in request");
                }

            }
        }
        // Handle IO exceptions
        catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());
            // If quitting (typically by you sending quit signal) clean up sockets
        }
        // Handle RuntimeException exceptions
        catch (Exception e) {
            throw new RuntimeException(e);
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
    public static int serverAdd(String client_id, int i) {

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
    public static int serverSubtract(String client_id, int i) {

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
    public static int serverGet(String client_id) {

        // If TreeMap does not contain the client ID
        if (!sum_tree_map.containsKey(client_id)) {
            // Initialize the sum of the client ID to be 0
            sum_tree_map.put(client_id, 0);
        }
        // Return sum of the client ID
        return sum_tree_map.get(client_id);
    }

    /**
     * Function to verify a signature. The verifying proceeds as follows:
     * 1) Decrypt the encryptedHash to compute a decryptedHash
     * 2) Hash the messageToCheck using SHA-256 (be sure to handle
     *    the extra byte as described in the signing method.)
     * 3) If this new hash is equal to the decryptedHash, return true else false.
     *
     * @param messageToCheck  a normal string that needs to be verified.
     * @param encryptedHashStr integer string - possible evidence attesting to its origin.
     * @return true or false depending on whether the verification was a success
     */
    // Source to generate RSA public and private keys - CMU Heinz 95-702 - Project 2 GitHub Page
    // https://github.com/CMU-Heinz-95702/Project-2-Client-Server
    public static boolean verifySignature(String messageToCheck, String encryptedHashStr, BigInteger e, BigInteger n) {

        // Take the encrypted string and make it a big integer
        BigInteger encryptedHash = new BigInteger(encryptedHashStr);

        // Decrypt it
        BigInteger decryptedHash = encryptedHash.modPow(e, n);

        // Compute SHA256 hash of the messageToCheck
        byte[] messageToCheckDigest = computeSHA256(messageToCheck);

        // messageToCheckDigest is a full SHA-256 digest
        // add a zero byte as the most significant byte to keep
        // the value to be signed non-negative.
        byte[] messageToCheckDigestWithExtraByte = new byte[messageToCheckDigest.length + 1];
        messageToCheckDigestWithExtraByte[0] = 0;

        // Generate the messageToCheckDigestWithExtraByte byte array
        System.arraycopy(messageToCheckDigest, 0, messageToCheckDigestWithExtraByte, 1, messageToCheckDigest.length + 1 - 1);

        // Make it a big int
        BigInteger bigIntegerToCheck = new BigInteger(messageToCheckDigestWithExtraByte);

        // Inform the user on how the two compare (true or false)
        return bigIntegerToCheck.compareTo(decryptedHash) == 0;
    }

    /***
     * Function to verify if the client ID hashes to the public key
     * @param client_ID Client ID of the client
     * @param e BigInteger e, which forms the public key
     * @param n BigInteger n, which forms the public key
     * @return Result of verification in terms of true or false
     */
    public static boolean verifyPublicKeyClientID(String client_ID, BigInteger e, BigInteger n) {

        // Generate the public key by concatenating e with n
        BigInteger public_key = new BigInteger(e + String.valueOf(n));

        // Compute SHA256 hash of the public key and store it to a byte array
        byte[] hash_of_public_key = computeSHA256(String.valueOf(public_key));

        // Stores the least significant 20 bytes of the hash
        byte[] client_LSB_20_bytes = new byte[20];

        // Populate the client_LSB_20_bytes byte array
        for (int i = 0; i < 20; i++) {
            client_LSB_20_bytes[i] = hash_of_public_key[hash_of_public_key.length - 1 - i];
        }

        // Compute the expected client ID by converting the least significant 20 bytes to hexadecimal String
        String expected_client_ID = bytesToHex(client_LSB_20_bytes);

        // Verify if the expected client ID matches the actual client ID
        return expected_client_ID.equals(client_ID);
    }

    /***
     * Computes the SHA256 hash value of the string passed into the function
     * @param input String input whose hash value is to be computed
     * @return A byte array with the SHA256 hash of the input String
     */
    // Source: Shivam Patel Project 1 Task 1 - CMU Heinz 95-702
    public static byte[] computeSHA256(String input) {

        // Source: CMU 95702 Fall 2022 Lab1-InstallationAndRaft Code
        byte[] digest = new byte[0];
        try {
            // Access MessageDigest class for SHA256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Compute the digest
            md.update(input.getBytes());

            // Store digest as a byte array for further use
            digest = md.digest();
        }
        // Handles No SHA-256 Algorithm exceptions
        catch (NoSuchAlgorithmException e) {
            // Print error message in console
            System.out.println("No SHA-256 available" + e);
        }
        // Return the SHA256 hash of input in byte[] form
        return digest;
    }

    // Code to convert from byte array to hexadecimal String
    // Source: https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /***
     * Function to convert a byte array to hexadecimal String
     * @param bytes Byte array to be converted to hexadecimal String
     * @return Hexadecimal notation (in String form) of the input byte array
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}