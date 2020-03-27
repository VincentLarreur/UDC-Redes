package es.udc.redes.tutorial.tcp.serverMulti;

import java.net.*;
import java.io.*;

/**
 * Multithread TCP echo server.
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
                // Create a ServerThread object, with the new connection as parameter
                ServerThread svt = new ServerThread(socket);
                // Initiate thread using the start() method
                svt.start();
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
