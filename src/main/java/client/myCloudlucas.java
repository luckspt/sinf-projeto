package client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class myCloudlucas {
    private static final String KEYSTORE_PASSWORD = "123456";
    private static final String KEYSTORE_ALIAS = "pedro";

    private static ClientSocket clientSocket;

    private static final IO io = new IO();

    public static void main(String[] args) {
        // Check arguments
        Map<String, List<String>> arguments = io.parseArguments(args);

        if (arguments.size() != 2)
            io.errorAndExit("Invalid options amount. There must be one -a and one of the following parameters: -c, -s, -e, -g");

        // Required arguments
        if (!arguments.containsKey("a"))
            io.errorAndExit("Missing server address parameter");

        // Validate server address and port
        String serverAddress = arguments.get("a").get(0);
        if (!serverAddress.matches("^(localhost|(?:[0-9]{1,3}\\.){3}[0-9]{1,3}):[0-9]{1,5}$"))
            io.errorAndExit("Invalid server address. Must be in the format: localhost:port or ip:port");

        // Get the other key (except "a")
        String[] keys = (String[]) arguments.keySet().toArray();
        String method = keys[0].equals("a") ? keys[1] : keys[0];

        // Validate file names and remove duplicates
        List<String> fileNames = new ArrayList<>(new HashSet<>(arguments.get(method)));
        if(!validFileNames(fileNames))
            io.errorAndExit("Invalid file names");

        // Connect to server
        String[] serverAddressSplit = serverAddress.split(":");
        try {
            clientSocket = new ClientSocket(serverAddressSplit[0], Integer.parseInt(serverAddressSplit[1]));
        } catch (IOException e) {
            io.errorAndExit("Could not connect to server");
        }

        // Execute the method
        switch (method) {
            case "c":
                // Hybrid encryption
                for (String fileName : fileNames)
                    hybridEncryption(fileName);
                break;
            case "s":
                for (String fileName : fileNames)
                    signFile(fileName);
                break;
            case "e":
                // TODO: using hybrid encryption and sign file
                break;
            case "g":
                for (String fileName : fileNames)
                    downloadFile(fileName);
                break;
            default:
                io.errorAndExit("Invalid method");
        }
    }

    /**
     * Read the file on the path, encrypt it, and send it to the server
     * @param filePath The file path
     */
    private static void hybridEncryption(String filePath) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        File file = new File(filePath);
        if (!file.exists())
            io.error("File " + filePath + " does not exist");

        // Ask the server if the file already exists
        String text = "fileExists " + file.getName();
        clientSocket.sendBytes(text.getBytes(StandardCharsets.UTF_8), text.length());

        // clientSocket.readBytes();
    }

    private static void signFile(String path) {

    }

    private static void downloadFile(String path) {

    }

    /**
     * Check if the file names are valid
     * @param fileNames File names to check
     * @return True if the file names are valid, false otherwise
     */
    private static boolean validFileNames(List<String> fileNames) {
        for (String fileName : fileNames) {
            if (!fileName.matches("^[^<>:;,?\"*|/]+$"))
                return false;
        }

        return true;
    }
}
