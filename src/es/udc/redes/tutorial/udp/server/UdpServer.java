package es.udc.redes.tutorial.udp.server;

import java.net.*;

/**
 * Implements an UDP Echo Server.
 */
public class UdpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: UdpServer <port_number>");
            System.exit(-1);
        }
        DatagramSocket serverSocket = null;
        try {
            // Obtain server port from first argument
            int serverPort = Integer.parseInt(argv[0]);
            // Create a server socket
            serverSocket = new DatagramSocket(serverPort);
            // Set max. timeout to 300 secs
            serverSocket.setSoTimeout(300000);
            while (true) {
                // Prepare datagram for reception
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                // Receive the message
                serverSocket.receive(packet);
                String str = new String(packet.getData());
                System.out.println("CLIENT: Receiving "
                    + str + " from "
                    + packet.getAddress().toString() + ":"
                    + packet.getPort());
                // Prepare datagram to send response
                DatagramPacket dgramResponse = new DatagramPacket(str.getBytes(),
                    str.getBytes().length, packet.getAddress(), packet.getPort());
                // Send response
                serverSocket.send(dgramResponse);
                System.out.println("CLIENT: Sending "
                    + new String(dgramResponse.getData()) + " to "
                    + dgramResponse.getAddress().toString() + ":"
                    + dgramResponse.getPort()+"\n");
            }
        // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the socket
            serverSocket.close();

        }
    }
}
