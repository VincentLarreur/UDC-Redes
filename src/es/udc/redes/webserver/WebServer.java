/**
 * <h1>WebServer Class</h1>
 * The Webserver Class start the server for 5 minutes wait for the connection of
 * a client and create a thread to process each request.
 * <p>
 *
 *
 * @author Vincent
 * @version 1.0
 * @since 2020-03-24
 */
package es.udc.redes.webserver;

import java.net.*;
import java.io.*;
import java.util.Date;

public class WebServer {

    static final int PORT = 8080;

    /**
     * This main is used to start the server for 5 min and create a HTTPServerThread to
     * process each request from a client.
     *
     * @param args Unused.
     */
    
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            // Create a server socket
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
            // Set a timeout of 300 secs
            serverSocket.setSoTimeout(300000);
            while (true) {
                // Wait for connections
                socket = serverSocket.accept();
                System.out.println("Connection opened. (" + new Date() + ")");
                // Create a ServerThread object, with the new connection as parameter
                HTTPServerThread svt = new HTTPServerThread(socket);
                // Initiate thread using the start() method
                svt.start();
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
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
