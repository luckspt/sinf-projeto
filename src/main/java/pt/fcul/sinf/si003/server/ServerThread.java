package pt.fcul.sinf.si003.server;

import pt.fcul.sinf.si003.CloudSocket;
import pt.fcul.sinf.si003.IO;

import java.io.*;
import java.util.Arrays;

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
            File file = new File(myCloudServer.getBaseDir(), this.getFilePath(commandName, arguments));

            io.info("New request:\n---- Command: " + commandName + "\n---- Parameters:\n- " + Arrays.stream(arguments).map(s -> s + "\n").reduce("\n- ", String::concat) + "---- File: " + file.getAbsolutePath());

            switch (commandName) {
                case "exists": {
                    existsFile(file);
                    break;
                }
                // EXTRA: delete file
                case "delete": {
                    deleteFile(file);
                    break;
                }
                case "upload": {
                    try {
                        uploadFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "download": {
                    try {
                        downloadFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case "signup": {
                    // Client sends username, password and certificate (name only)
                    String username = commandParts[1];
                    String password = commandParts[2];

                    try {
                        signup(username, password, file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        // TODO: make the path to the recipient's folder when sharing a file with someone or saving to self
        switch (commandName) {
            case "signup":
                return CERTS_DIR + arguments[0];
            default:
                return arguments[0];
        }
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
        if (this.userManager.userExists(username)) {
            cloudSocket.sendBool(false);
            return;
        }

        // Add the user
        this.userManager.setUser(username, password);
        cloudSocket.sendBool(true);

        // Create the user's folder
        File userFolder = new File(myCloudServer.getBaseDir(), USERS_DIR + username);
        userFolder.mkdir();

        // Save the certificate
        this.uploadFile(certificate);
    }
}
