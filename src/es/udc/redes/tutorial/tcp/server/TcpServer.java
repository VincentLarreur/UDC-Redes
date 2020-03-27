package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;

/**
 * Monothread TCP echo server.
 */
public class TcpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: TcpServer <port>");
            System.exit(-1);
        }
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            int serverPort = Integer.parseInt(argv[0]);
            // Create a server socket
            serverSocket = new ServerSocket(serverPort);
            // Set a timeout of 300 secs
            serverSocket.setSoTimeout(300000);
            while (true) {
                // Wait for connections
                socket = serverSocket.accept();
                // Set the input channel
                BufferedReader sInput = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                // Set the output channel
                PrintWriter sOutput = new PrintWriter(socket.getOutputStream(), true);
                // Receive the client message
                String recibido = sInput.readLine();
                System.out.println("SERVER: Received " + recibido);
                // Send response to the client
                System.out.println("SERVER: Sending " + recibido);
                sOutput.println(recibido);
                // Close the streams
                sOutput.close();
                sInput.close();
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
//Close the socket
        try {
                socket.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
