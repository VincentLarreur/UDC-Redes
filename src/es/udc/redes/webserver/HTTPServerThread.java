/**
 * <h1>HTTPServerThread Class</h1>
 * The HTTPServerThread is a thread that process the HTTP request on the server
 * side, searches for the resource,encapsulates it in the HTTP response message
 * and sends it. It manage the exception if the file is not found or the method
 * not implemented.
 * <p>
 *
 *
 * @author Vincent
 * @version 1.0
 * @since 2020-03-24
 */
package es.udc.redes.webserver;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;

public class HTTPServerThread extends Thread {

    ReadPropertyFile properties = new ReadPropertyFile();
    static final boolean connected = true;
    private Socket mySocks;

    /**
     * This is the constructor of the class, to initialiaze the thread from the
     * socket sent by the webserver
     *
     * @param c server socket connected to the client, containing the request
     * and infos about the client.
     */
    public HTTPServerThread(Socket c) {
        this.mySocks = c;
    }

    /**
     * This method run of the thread, since it extends Thread, it is the method
     * launched when we do an HTTPServerThread.start in the webserver
     *
     */
    @Override
    public void run() {
        //initializing variable used in the  try to be able to use them in the
        //catch or the finally (to close the Streams for example
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;
        String ip = null;
        String requestLine = null;
        Date dateReceived = new Date();
        try {
            // we read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(mySocks.getInputStream()));
            // we get character output stream to client (for headers)
            out = new PrintWriter(mySocks.getOutputStream());
            // get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(mySocks.getOutputStream());
            // get ip of the client
            ip = (((InetSocketAddress) mySocks.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");

            // get first line of the request from the client
            requestLine = in.readLine();

            // get a boolean to know if there is changes on the files since the 
            //last request of this file
            String ifmodified = manageIfModified(in);

            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(requestLine);
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            // we get file requested
            if (parse.hasMoreTokens()) {
                fileRequested = parse.nextToken().toLowerCase();
            }
            // we support only GET and HEAD methods, we check
            if (!method.equals("GET") && !method.equals("HEAD")) {
                //calling the method to send back an  not implemented response
                notImplemented(out, dataOut, method, requestLine, dateReceived, ip);
                //logging the error in the error.log
                errorLog(requestLine, ip, dateReceived, "501 Not Implemented : " + method + " method.");
            } else {
                //calling the method to send the GET/HEAD response
                gethead(out, dataOut, method, fileRequested, requestLine, dateReceived, ip, ifmodified);
            }

        } catch (FileNotFoundException fnfe) {
            try {
                //calling the method to send back file not found response
                fileNotFound(out, dataOut, fileRequested, requestLine, dateReceived, ip);
                //logging the error in the error.log
                errorLog(requestLine, ip, dateReceived, "404 : File Not Found.");
            } catch (IOException ioe) {
                //logging the error in the error.log
                errorLog(requestLine, ip, dateReceived, "Error with file not found exception : " + ioe.getMessage());
                //printing the error on the client side
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }
        } catch (Exception e) {
            //logging the error in the error.log
            errorLog(requestLine, ip, dateReceived, "Server error : " + e);
            //printing the error on the client side
            System.err.println("Server error : " + e);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                mySocks.close(); // we close socket connection and streams 
            } catch (Exception e) {
                //logging the error in the error.log
                errorLog(requestLine, ip, dateReceived, "Error closing stream : " + e.getMessage());
                //printing the error on the client side
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (connected) {
                System.out.println("Connection closed.\n");
            }
        }
    }
    
    /**
     * This method read the file data into an array of byte
     *
     * @param file to read
     * @param fileLength : length of the file to initialize the array of byte
     * @return an array of byte corresponding to the data in the file
     * @exception IOException On close the FileInputStream, reading the file or file not found
     * error.
     * @see IOException
     */
    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }

        return fileData;
    }

    /**
     * This method return the supported MIME Types from thename of the file Requested
     *
     * @param fileRequested : name of the file of which we'll extract the extension
     * to know the type of file requested 
     * @return the type supported of the file requested 
     */
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html")) {
            return "text/html";
        } else if (fileRequested.endsWith(".txt")) {
            return "text/plain";
        } else if (fileRequested.endsWith(".gif")) {
            return "image/gif";
        } else if (fileRequested.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * This method send the response of a get/head request
     *
     * @param out : output stream buffer to flush
     * @param dataOut : binary output stream to client (for requested data)
     * @param method : method of the request (Get or Head supported only)
     * @param fileRequested : name of the file requested
     * @param d : date of the request received for the logs
     * @param ip : ip of the client
     * @param ifmodified : date of the if modified header of the header
     */
    private void gethead(PrintWriter out, BufferedOutputStream dataOut, String method, String fileRequested, String requestLine, Date d, String ip, String ifmodified) throws Exception {
        // GET or HEAD method
        File file = properties.allow(fileRequested);
        int fileLength = (int) file.length();
        String content = getContentType(fileRequested);

        byte[] fileData = readFileData(file, fileLength);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        boolean modified = checkmodified(sdf, ifmodified, new Date(file.lastModified()));

        // send HTTP Headers
        if (modified) {
            out.println("HTTP/1.1 200 OK");
        } else {
            out.println("HTTP/1.1 304 Not Modified");
        }
        out.println("Date: " + new Date());
        out.println("Server: localhost " + properties.getPortProperty());
        out.println("Last-Modified: " + sdf.format(file.lastModified()));
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer
        accessLog(requestLine, ip, d, 200, fileLength);
        if (method.equals("GET")) { // GET method so we return content
            if (modified) { // only if it has been modified
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            }
        }

        if (connected && modified) {
            System.out.println("File " + fileRequested + " of type " + content + " returned");
        }

    }

        /**
     * This method send the response of not implemented method request
     *
     * @param out : output stream buffer to flush
     * @param dataOut : binary output stream to client (for requested data)
     * @param method : method of the request (Get or Head supported only)
     * @param fileRequested : name of the file requested
     * @param d : date of the request received for the logs
     * @param ip : ip of the client
     * @param ifmodified : date of the if modified header of the header
     */
    private void notImplemented(PrintWriter out, BufferedOutputStream dataOut, String method, String requestLine, Date d, String ip) throws IOException {
        if (connected) {
            System.out.println("501 Not Implemented : " + method + " method.");
        }
        // we return the not supported file to the client
        File file = properties.allow("501");
        int fileLength = (int) file.length();
        String contentMimeType = "text/html";
        //read content to return to client
        byte[] fileData = readFileData(file, fileLength);

        // we send HTTP Headers with data to client
        out.println("HTTP/1.1 400 Bad request");
        out.println("Date: " + new Date());
        out.println("Content-type: " + contentMimeType);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer
        // file
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
    }

        /**
     * This method send the response of request on a file not found in the server
     * 
     * @param out : output stream buffer to flush
     * @param dataOut : binary output stream to client (for requested data)
     * @param method : method of the request (Get or Head supported only)
     * @param fileRequested : name of the file requested
     * @param d : date of the request received for the logs
     * @param ip : ip of the client
     * @param ifmodified : date of the if modified header of the header
     */
    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested, String requestLine, Date d, String ip) throws IOException {
        File file = properties.allow("404");
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); // blank line between headers and content, very important !
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (connected) {
            System.out.println("File " + fileRequested + " not found");
        }
    }

    /**
     * This method write the log of any access to a file from a client request
     *
     * @param requestLine : requestline of the client request
     * @param size : size of the file acceded
     * @param dReceived : date of the request received for the logs
     * @param ip : ip of the client
     * @param status : status of the access
     */
    private void accessLog(String requestLine, String ip, Date dReceived, int status, int size) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        BufferedWriter writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/src/es/udc/redes/webserver/resources/access.log", true));
        writer.append(requestLine + "\n");
        writer.append("From" + ip.toString() + "\n");
        writer.append(sdf.format(dReceived) + "\n");
        writer.append("Status : " + status + "\n");
        writer.append(size + " bytes\n\n");

        writer.close();
        System.out.println("Access Logged");

    }

    /**
     * This method write the log of errors
     *
     * @param requestLine : requestline of the client request
     * @param size : size of the file acceded
     * @param dReceived : date of the request received for the logs
     * @param error : error message
     */
    private void errorLog(String requestLine, String ip, Date dReceived, String error) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/src/es/udc/redes/webserver/resources/error.log", true));
            writer.append(requestLine + "\n");
            writer.append("From" + ip.toString() + "\n");
            writer.append(sdf.format(dReceived) + "\n");
            writer.append(error + "\n\n");
            writer.close();
            System.out.println("Error Logged");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method write the log of any access to a file from a client request
     *
     * @param in : Buffered Reader containing the request header 
     * @return null if no ifModifiedSince header, if there is : return the date 
     * as a string
     */
    private String manageIfModified(BufferedReader in) throws IOException {
        //forced to use this method of stopping when contain Accept Language
        //the usual last line before Ifmodified Since line
        //because the while((strCurrentLine = in.readLine()) != null) never stop
        //due to the socket still open
        String strCurrentLine = in.readLine();
        String result = null;
        while (!strCurrentLine.contains("Accept-Language")) {
            strCurrentLine = in.readLine();
        }
        strCurrentLine = in.readLine();
        if (strCurrentLine.contains("If-Modified-Since")) {
            result = strCurrentLine.substring(19);
            System.out.println(result);
        }
        return result;
    }

    //compare if the date of the ifmodifiedSince header occurs after the date of
    /**
     * This method compare if the date of the ifmodifiedSince header occurs 
     * after the date of the last modified property of the file
     *
     * @param sdf : SimpleDateFormat to format the date on the same maneer
     * @param ifmodified : last know modification from the client of the file 
     * @param lastModified : property of the file
     * @return a boolean : true if it has been modified since last access, false
     * if not
     */
    private boolean checkmodified(SimpleDateFormat sdf, String ifmodified, Date lastModified) throws Exception {
        boolean result = false;
        if (ifmodified != null) // if modified since is in the header
        {
            Date ifModifDate = sdf.parse(ifmodified);
            if (ifModifDate.compareTo(lastModified) > 0) // test the date 
            {
                result = true;
            }
        }
        return result;
    }

}
