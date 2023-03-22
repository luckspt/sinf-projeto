package pt.fcul.sinf.si003.server;

import pt.fcul.sinf.si003.CloudSocket;
import pt.fcul.sinf.si003.IO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class myCloudServer {
    private final static IO io = new IO("Server");
    private static String baseDir = "./";

    public static String getBaseDir() {
        return baseDir;
    }

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
        if (arguments.containsKey("baseDir"))
            baseDir = arguments.get("baseDir").get(0);

        // Create server socket
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            io.printMessage("Server started on 0.0.0.0:" + port);
        } catch (IOException e) {
            io.errorAndExit("Could not start server socket: " + e.getMessage());
        }

        while (true) {
            try {
                // Wait for connection
                io.printMessage("Waiting for connections...");
                Socket clientSocket = serverSocket.accept();
                io.printMessage("Connection established with " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                // Create a new thread for the connection
                CloudSocket cloudSocket = new CloudSocket(clientSocket);
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
}