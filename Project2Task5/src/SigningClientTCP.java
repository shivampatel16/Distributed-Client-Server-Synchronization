/**
 * Author: Shivam Patel
 * Andrew ID: shpatel
 * Last Modified: October 7, 2022
 * File: SigningClientTCP.java
 * Part Of: Project2Task5
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
 * The client will send a signed request. Each time the client
 * program runs, it will create new RSA public and private keys and
 * display these keys to the user. The client's ID will be formed by
 * taking the least significant 20 bytes of the hash of the client's
 * public key. The client will also transmit its public key with each
 * request. Finally, the client will sign each request.
 */

// imports required for TCP/IP, IO operations, BigInteger, RSA256, List and Random
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class SigningClientTCP {

    // Stores the value of port number of the server
    static int serverPort;

    /***
     * The main method for the client to request for the server port from the user,
     * request a message (operation, operand) from the user, send a request
     * message to the server, receive a reply from the server (sum of the client ID)
     * and print the reply to the user. The client's ID will be formed by taking the
     * least significant 20 bytes of the hash of the client's public key. The client
     * will also transmit its public key with each request. Finally, the client will
     * sign each request.
     * @param args Command line arguments (none here)
     */
    public static void main(String args[]) throws Exception {

        // Prompting the user that the client is running
        System.out.println("\nThe client is running.");

        // Creating a Scanner object for taking inputs from the user
        Scanner s = new Scanner(System.in);

        // Requesting the user for the server side port
        System.out.print("Please enter server port: ");

        // Getting the server port from the user
        serverPort = s.nextInt();
        System.out.println();

        // List to store the generated parts of the RSA public and private keys
        List keys_list = generateRSAKeys();

        BigInteger e; // e is the exponent of the public key
        BigInteger d; // d is the exponent of the private key
        BigInteger n; // n is the modulus for both the private and public keys
        BigInteger public_key; // Stores the RSA public key

        // Populate the values of e, d, n and public key from the keys_list
        e = (BigInteger) keys_list.get(0);
        d = (BigInteger) keys_list.get(1);
        n = (BigInteger) keys_list.get(2);
        public_key = new BigInteger(e + String.valueOf(n));

        // Compute SHA-256 hash of the public key
        byte[] hash_of_public_key = computeSHA256(String.valueOf(public_key));

        // Stores the least significant 20 bytes of the hash
        byte[] client_LSB_20_bytes = new byte[20];

        // Populate the client_LSB_20_bytes byte array
        for (int i = 0; i < 20; i++) {
            client_LSB_20_bytes[i] = hash_of_public_key[hash_of_public_key.length - 1 - i];
        }

        // Computes the client ID by converting client_LSB_20_bytes bytes array to a hexadecimal String
        String client_ID = bytesToHex(client_LSB_20_bytes);

        // Stores user input
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

            // Read operation from user
            String user_operation = typed.readLine();

            // Stores operand for the operation
            String operand = "";

            // Update user input
            user_input = user_input + user_operation;

            // Switch case for user input
            switch (user_input) {

                // If user requested for an addition operation
                case "1" -> {

                    // Prompt the user for an operand
                    System.out.println("Enter value to add:");
                    operand = typed.readLine();

                    // Update user input
                    user_input = user_input + "," + operand + ",";
                }

                // If user requested for a subtraction operation
                case "2" -> {

                    // Prompt the user for an operand
                    System.out.println("Enter value to subtract:");
                    operand = typed.readLine();

                    // Update user input
                    user_input = user_input + "," + operand + ",";
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

            // Stores client request without signature
            String client_request_without_sign = client_ID + "," + e + "," + n + "," +
                    user_operation + "," + operand;

            // Generates signature for client request
            String signature = sign(client_request_without_sign, d, n);

            // Stores client request with signature
            String client_request_with_sign = client_request_without_sign + "," + signature;

            // Request the operation from server and store the value of sum
            int serverSumReturned = operations(client_request_with_sign);

            // Display the sum received from the server to the user
            System.out.println("The result is " + serverSumReturned + ".\n");
        }
    }

    /***
     * Function to communicate with the server and perform the required
     * operation on the requested integer value by the client
     * @param client_request_with_sign Input from the user containing client ID, operation and operand
     * @return Updated sum from the server
     */
    public static int operations(String client_request_with_sign) {

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
            out.println(client_request_with_sign);

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

    /***
     * Function to generate RSA public and private keys
     * @return A List of (e, d, n) which are the components of the public and private keys
     */
    // Source to generate RSA public and private keys - CMU Heinz 95-702 - Project 2 GitHub Page
    // https://github.com/CMU-Heinz-95702/Project-2-Client-Server
    public static List generateRSAKeys() {
        List<BigInteger> keys_list = new ArrayList<>();

        // Each public and private key consists of an exponent and a modulus
        BigInteger n; // n is the modulus for both the private and public keys
        BigInteger e; // e is the exponent of the public key
        BigInteger d; // d is the exponent of the private key

        // Generate a random number
        Random rnd = new Random();

        // Step 1: Generate two large random primes.
        BigInteger p = new BigInteger(2048, 100, rnd);
        BigInteger q = new BigInteger(2048, 100, rnd);

        // Step 2: Compute n by the equation n = p * q.
        n = p.multiply(q);

        // Step 3: Compute phi(n) = (p-1) * (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        // Step 4: Select a small odd integer e that is relatively prime to phi(n).
        // By convention the prime 65537 is used as the public exponent.
        e = new BigInteger("65537");

        // Step 5: Compute d as the multiplicative inverse of e modulo phi(n).
        d = e.modInverse(phi);

        // Displaying the constituents of client's public and private keys
        System.out.println(" e = " + e);
        System.out.println(" d = " + d);
        System.out.println(" n = " + n);

        // Display RSA public and RSA private keys to the user
        System.out.println("\nRSA Public Key (e, n) = (" + e + ", \n" + n + ")\n");
        System.out.println("RSA Private Key (d, n) = (" + d + ", \n" + n + ")\n");

        // Add e, d and n to the list
        keys_list.add(e);
        keys_list.add(d);
        keys_list.add(n);

        // Return list of the components of the RSA keys
        return keys_list;
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

    /**
     * Function to sign a message using RSA private key.
     * Signing proceeds as follows:
     * 1) Get the bytes from the string to be signed.
     * 2) Compute SHA-256 digest of these bytes.
     * 3) Copy these bytes into a byte array that is one byte longer than needed.
     *    The resulting byte array has its extra byte set to zero. This is because
     *    RSA works only on positive numbers. The most significant byte (in the
     *    new byte array) is the 0'th byte. It must be set to zero.
     * 4) Create a BigInteger from the byte array.
     * 5) Encrypt the BigInteger with RSA d and n.
     * 6) Return to the caller a String representation of this BigInteger.
     * @param message a sting to be signed
     * @param d BigInteger d, part of the RSA Private Key
     * @param n BigInteger n, part of the RSA Private Key
     * @return String representing a big integer - the encrypted hash.
     */
    // Source to generate RSA public and private keys - CMU Heinz 95-702 - Project 2 GitHub Page
    // https://github.com/CMU-Heinz-95702/Project-2-Client-Server
    public static String sign(String message, BigInteger d, BigInteger n) {

        // Compute the digest with SHA-256
        byte[] bigDigest = computeSHA256(message);

        // We add a 0 byte as the most significant byte to keep
        // the value to be signed non-negative.
        byte[] messageDigest = new byte[bigDigest.length + 1];
        messageDigest[0] = 0;   // most significant set to 0

        System.arraycopy(bigDigest, 0, messageDigest, 1, bigDigest.length + 1 - 1);

        // From the digest, create a BigInteger
        BigInteger m = new BigInteger(messageDigest);

        // encrypt the digest with the private key
        BigInteger c = m.modPow(d, n);

        // return this as a big integer string
        return c.toString();
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