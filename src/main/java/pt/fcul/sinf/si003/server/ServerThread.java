package pt.fcul.sinf.si003.server;

import pt.fcul.sinf.si003.CloudSocket;
import pt.fcul.sinf.si003.IO;

import java.io.*;

/**
 * The server thread.
 */
public class ServerThread extends Thread {
    /**
     * The socket abstraction
     */
    private final CloudSocket cloudSocket;
    /**
     * The IO abstraction
     */
    private final IO io = new IO();

    /**
     * Creates a new instance of ServerThread.
     * @param cloudSocket the socket abstraction
     */
    public ServerThread(CloudSocket cloudSocket) {
        this.cloudSocket = cloudSocket;
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

            String commandName = command.split(" ")[0];
            String fileName = command.split(" ")[1];
            File file = new File(myCloudServer.getBaseDir(), fileName);

            io.info("New request:\n---- Command: " + commandName + "\n---- File   : " + file.getAbsolutePath() + "\n---- Exists : " + file.exists());

            switch (commandName) {
                case "exists":
                    existsFile(file);
                    break;
                // EXTRA: delete file
                case "delete":
                    deleteFile(file);
                    break;
                case "upload":
                    try {
                        uploadFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "download":
                    try {
                        downloadFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    cloudSocket.sendString("Invalid command. Available commands: exists, delete, upload");
            }
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
}
