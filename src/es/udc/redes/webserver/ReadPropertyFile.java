/**
 * <h1>ReadPropertyFile Class</h1>
 * The ReadPropertyFile Class is used to get the properties stored in the config
 * properties files in the resource folder.
 * * <p>
 *
 *
 * @author Vincent
 * @version 1.0
 * @since 2020-03-24
 */
package es.udc.redes.webserver;

import java.util.Properties;
import java.io.*;

public class ReadPropertyFile {

    FileInputStream fis;

    /**
     * This is the method which return the port number stored in the config properties
     * by loading the file and parsing the corresponding data into an int
     *
     * @return an int : the port number stored in the config.properties.
     * @exception IOException On close the FileInputStream error.
     * @see IOException
     */
    public int getPortProperty() throws IOException {
        //return variable
        int port = -1;
        try {
            Properties prop = new Properties();
            
            //we are here forced to use this method, trying with loading an 
            //inputStream like this doesalways return a null
            //getClass.getClassLoader().getResourceAsStream("config.properties")) 
            
            String propFileName = "/src/es/udc/redes/webserver/resources/config.properties";
            
            fis = new FileInputStream(System.getProperty("user.dir") + propFileName);

            if (fis != null) {
                prop.load(fis);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            
            //parsing from String to int
            port = Integer.parseInt(prop.getProperty("PORT"));

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            //closing the FileInputStream
            fis.close();
        }
        
        return port;
    }

    /**
     * This is the method which return the file asked by the request, using the 
     * path stored in the config.file and the name of the file for the error pages
     *
     * @param fileRequested : name of the requested file, 404 for the not found page
     * and 01 for the not implemented method page
     * @return a file : the corresponding file as requested.
     * @exception IOException On close the FileInputStream error.
     * @see IOException
     */
    public File allow(String fileRequested) throws IOException {
        //result file;
        File myFile = null;
        try {
            Properties prop = new Properties();
            
            //we are here forced to use this method, trying with loading an 
            //inputStream like this doesalways return a null
            //getClass.getClassLoader().getResourceAsStream("config.properties")) 
            
            String propFileName = "/src/es/udc/redes/webserver/resources/config.properties";

            fis = new FileInputStream(System.getProperty("user.dir") + propFileName);
            if (fis != null) {
                prop.load(fis);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            //getting the corresponding file from the request
            if (fileRequested == "501") {
                myFile = new File(prop.getProperty("DIRECTORY"), prop.getProperty("BAD_REQUEST"));
            } else if (fileRequested == "404") {
                myFile = new File(prop.getProperty("DIRECTORY"), prop.getProperty("FILE_NOT_FOUND"));
            } else {
                myFile = new File(prop.getProperty("DIRECTORY"), fileRequested);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            //closing the FileInputStream
            fis.close();
        }
        
        return myFile;
    }

}
