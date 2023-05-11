package pt.fcul.sinf.si003.server;

import pt.fcul.sinf.si003.CloudSocket;
import pt.fcul.sinf.si003.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * The server.
 */
public class myCloudServer {
    /**
     * The IO abstraction
     */
    private final static IO io = new IO();
    /**
     * The base directory
     */
    private static String baseDir = "./";

    /**
     * The password manager
     */
    private static UserManager userManager;

    /**
     * The main method.
     * @param args the arguments
     */
    public static void main(String[] args) {
        // Check arguments
        Map<String, List<String>> arguments = io.parseArguments(args);

        // Define port
        int port = 3000;
        if (arguments.containsKey("p") && arguments.get("p").size() == 1) {
            // Check if port is a number
            if (!arguments.get("p").get(0).matches("^[0-9]+$"))
                io.errorAndExit("Invalid port: not a nummber");

            port = Integer.parseInt(arguments.get("p").get(0));

            // Check if port is in range
            if (port > 65535)
                io.errorAndExit("Invalid port: out of range");
        }

        // EXTRA: base directory
        if (arguments.containsKey("d"))
            baseDir = arguments.get("d").get(0);

        // EXTRA: chunk size
        int chunkSize = 1024;
        if (arguments.containsKey("-chunkSize") && arguments.get("-chunkSize").size() == 1 && arguments.get("-chunkSize").get(0).matches("^[0-9]+$"))
            chunkSize = Math.max(1024, Math.min(65535, Integer.parseInt(arguments.get("-chunkSize").get(0))));

        // User manager
        try {
            userManager = new UserManager(baseDir, "users.txt");
        } catch (IOException e) {
            io.errorAndExit("Could not read password file: " + e.getMessage());
        }

        // Create server socket
        SSLServerSocket sslServerSocket = null;
        try {
            // Load truststore
        	// Load server key store
        	
            KeyStore serverKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            File keyStoreFile = new File(baseDir, "keystore.jppCloudServer");
            FileInputStream serverKeyStoreFile = new FileInputStream(keyStoreFile);
            serverKeyStore.load(serverKeyStoreFile, "password".toCharArray());
            
        	
            
            // Load server trust store
            KeyStore serverTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            File trustStoreFile = new File(baseDir, "truststore.jppCloudServer");
            FileInputStream serverTrustStoreFile = new FileInputStream(trustStoreFile);
            serverTrustStore.load(serverTrustStoreFile, "password".toCharArray());

            // Create key manager and trust manager factories
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(serverKeyStore, "password".toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(serverTrustStore);

            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            // Create SSL server socket
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
            

            io.info("Server started on 0.0.0.0:" + port);
        } catch (Exception e) {
            io.errorAndExit("Could not start SSL server socket: " + e.getMessage());
        }

        while (true) {
        	try {
                // Wait for connection
                io.info("Waiting for connections...");
                Socket clientSocket = sslServerSocket.accept();
                io.success("Connection established with " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                // Create a new thread for the connection
                CloudSocket cloudSocket = new CloudSocket(clientSocket, chunkSize);
                ServerThread serverThread = new ServerThread(cloudSocket, userManager);

                // Start the thread
                serverThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Close the socket. This code is unreachable
        // serverSocket.close();
    }

    /**
     * Get the base directory
     * @return the base directory
     */
    public static String getBaseDir() {
        return baseDir;
    }
}
