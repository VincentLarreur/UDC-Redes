package es.udc.redes.tutorial.tcp.serverMulti;

import java.net.*;
import java.io.*;

/**Thread that processes an echo server connection.*/

public class ServerThread extends Thread {

    private Socket socket;
    
    public ServerThread(Socket socket) {
        this.socket = socket;
    }
 
    public void run() {
        try {
            // Set the input channel
            BufferedReader sInput = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            // Set the output channel
            PrintWriter sOutput = new PrintWriter(socket.getOutputStream(), true);
            // Receive the message from the client
            String recibido = sInput.readLine();
            System.out.println("SERVER: Received " + recibido);
            // Sent the echo message to the client
            String echo = new String("echo");
            System.out.println("SERVER: "+ echo);
            sOutput.println(echo);
            // Close the streams 
            sOutput.close();
            sInput.close();
        } catch  (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch  (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally
        {
            //close the socket
             try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
