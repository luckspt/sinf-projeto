package pt.fcul.sinf.si003.server;

import pt.fcul.sinf.si003.CloudSocket;
import pt.fcul.sinf.si003.IO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

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

        // Create server socket
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            io.info("Server started on 0.0.0.0:" + port);
        } catch (IOException e) {
            io.errorAndExit("Could not start server socket: " + e.getMessage());
        }

        while (true) {
            try {
                // Wait for connection
                io.info("Waiting for connections...");
                Socket clientSocket = serverSocket.accept();
                io.info("Connection established with " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                // Create a new thread for the connection
                CloudSocket cloudSocket = new CloudSocket(clientSocket, chunkSize);
                ServerThread serverThread = new ServerThread(cloudSocket);

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
