package pt.fcul.sinf.si003.server;

import pt.fcul.sinf.si003.CloudSocket;
import pt.fcul.sinf.si003.IO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * The server thread.
 */
public class ServerThread extends Thread {
    private final String USERS_DIR = "users/";
    private final String CERTS_DIR = "certs/";

    /**
     * The socket abstraction
     */
    private final CloudSocket cloudSocket;
    /**
     * The IO abstraction
     */
    private final IO io = new IO();

    private final UserManager userManager;

    /**
     * Creates a new instance of ServerThread.
     * @param cloudSocket the socket abstraction
     */
    public ServerThread(CloudSocket cloudSocket, UserManager userManager) {
        this.cloudSocket = cloudSocket;
        this.userManager = userManager;
    }

    /**
     * Handles the commands.
     */
    @Override
    public void run() {
        boolean isAuthenticated = false;

        while (true) {
            String command = cloudSocket.receiveString();
            // Check if connection was closed
            if (command == null || command.isEmpty()) {
                io.info(cloudSocket.getRemoteAddress() + " disconnected");
                break;
            }

            String[] commandParts = command.split(" ");
            String commandName = commandParts[0];
            String[] arguments = Arrays.copyOfRange(commandParts, 1, commandParts.length);

            io.info("New request:\n---- Command: " + commandName + "\n---- Parameters:\n- " + Arrays.stream(arguments).map(s -> s + "\n").reduce("\n- ", String::concat));// + "---- File: " + file.getAbsolutePath());
            io.info("Is authenticated: " + isAuthenticated);

            switch (commandName) {
                case "exists": {
                    // exists <username> <file>
                    if (!isAuthenticated) {
                        break;
                    }

                    File file = new File(myCloudServer.getBaseDir(), this.getFilePath(commandName, arguments));
                    existsFile(file);
                    break;
                }
                // EXTRA: delete file
                case "delete": {
                    // delete <username> <file>
                    if (!isAuthenticated) {
                        break;
                    }

                    File file = new File(myCloudServer.getBaseDir(), this.getFilePath(commandName, arguments));
                    deleteFile(file);
                    break;
                }
                case "upload": {
                    // upload <username> <file>
                    if (!isAuthenticated) {
                        break;
                    }

                    try {
                        File file = new File(myCloudServer.getBaseDir(), this.getFilePath(commandName, arguments));
                        uploadFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "download": {
                    // download <username> <file>
                    if (!isAuthenticated) {
                        break;
                    }

                    try {
                        File file = new File(myCloudServer.getBaseDir(), this.getFilePath(commandName, arguments));
                        downloadFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "get-certificate": {
                    // get certificate downloads certificate from certs folder
                    // get-certificate <username>
                    if (!isAuthenticated) {
                        break;
                    }

                    try {
                        File cerFilename = new File(myCloudServer.getBaseDir(), CERTS_DIR + commandParts[1] + ".cer");
                        cloudSocket.sendBool(cerFilename.exists());
                        if(cerFilename.exists())
                            downloadFile(cerFilename);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "signup": {
                    // signup <username> <password> <certificate>
                    if (isAuthenticated) {
                        break;
                    }

                    String username = commandParts[1];
                    String password = commandParts[2];

                    try {
                        File file = new File(myCloudServer.getBaseDir(), this.getFilePath(commandName, arguments));
                        signup(username, password, file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "login": {
                    // login <username> <password>
                    String username = commandParts[1];
                    String password = commandParts[2];

                    isAuthenticated = login(username, password);
                    cloudSocket.sendBool(isAuthenticated);
                    break;
                }
                default:
                    cloudSocket.sendString("Invalid command. Available commands: exists, delete, upload, download, signup");
            }
        }
    }

    /**
     * Calculates the file path based on the command name and arguments.
     * @param commandName the command name
     * @param arguments the arguments
     * @return the file path
     */
    private String getFilePath(String commandName, String[] arguments) {
        if (commandName.equals("signup")) {
            // arguments[0] is the username
            return CERTS_DIR + arguments[0] + ".cer";
        }

        // arguments[0] is the username and arguments[1] is the file name
        return USERS_DIR + arguments[0] + "/" + arguments[1];
    }

    /**
     * Checks if a file exists and sends the result to the requester.
     * @param file the file to be checked
     */
    private void existsFile(File file) {
        cloudSocket.sendBool(file.exists());
    }

    /**
     * Deletes a file.
     * @param file the file do be deleted
     */
    private void deleteFile(File file) {
        io.info("Deleting file: " + file.getAbsolutePath());
        if (file.exists()) {
            boolean r = file.delete();

            if (r)
                io.success("File deleted");
            else
                io.warning("File not deleted");
        } else {
            io.warning("File does not exist");
        }
    }

    /**
     * Sends a file to the requester.
     * @param file the file to be sent
     * @throws FileNotFoundException if the file does not exist
     */
    private void downloadFile(File file) throws FileNotFoundException {
        io.info("Sending file: " + file.getAbsolutePath());
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        cloudSocket.sendStream(file.length(), bufferedInputStream);
        io.success("File sent");
    }

    /**
     * Receives a file from the requester.
     * @param file the file to be received
     * @throws IOException if an I/O error occurs
     */
    private void uploadFile(File file) throws IOException {
        io.info("Receiving file: " + file.getAbsolutePath());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        cloudSocket.receiveStream(fileOutputStream);
        fileOutputStream.close();
        io.success("File received");
    }

    private void signup(String username, String password, File certificate) throws IOException {
        // Validate that the user does not exist
        boolean canSignup = !this.userManager.userExists(username);
        // Send whether the user can be added
        cloudSocket.sendBool(canSignup);

        // Stop if the user cannot be added
        if (!canSignup) {
            return;
        }

        // Add the user
        this.userManager.setUser(username, password);

        // Create the user's folder
        File userFolder = new File(myCloudServer.getBaseDir(), USERS_DIR + username);
        if(!userFolder.mkdir())
            io.error("Could not create user folder");

        // Receive and save the certificate
        this.uploadFile(certificate);
    }

    private boolean login(String username, String password) {
        // Validate that the user exists
        ServerUser user = this.userManager.getUser(username);
        if (user == null) {
            return false;
        }

        // Validate that the password is correct
        if (!user.checkPassword(password)) {
            cloudSocket.sendBool(false);
            return false;
        }

        return true;
    }
    
    public static byte[] calculateHmac(String message, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(keySpec);
        byte[] byteMessage = message.getBytes(StandardCharsets.UTF_8);
        byte[] macBytes = mac.doFinal(byteMessage);
        return macBytes;
    }
}
