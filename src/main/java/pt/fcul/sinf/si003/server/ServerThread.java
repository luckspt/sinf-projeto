package pt.fcul.sinf.si003.server;

import pt.fcul.sinf.si003.CloudSocket;
import pt.fcul.sinf.si003.IO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServerThread extends Thread {
    private final CloudSocket cloudSocket;
    private final IO io = new IO("ServerThread");

    public ServerThread(CloudSocket cloudSocket) {
        this.cloudSocket = cloudSocket;
    }

    @Override
    public void run() {
        while (true) {
            String command = cloudSocket.receiveString();
            // Check if connection was closed
            if (command.isEmpty()) {
                io.printMessage(cloudSocket.getRemoteAddress() + " disconnected");
                break;
            }

            io.printMessage("Received command: " + command);

            String commandName = command.split(" ")[0];
            String fileName = command.split(" ")[1];
            File file = io.openFile(myCloudServer.getBaseDir(), fileName, false);

            io.printMessage("\tFile: " + file.getAbsolutePath() + "\n\tExists: " + file.exists());

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
                default:
                    cloudSocket.sendString("Invalid command. Available commands: exists, delete, upload");
            }
        }
    }

    private void existsFile(File file) {
        cloudSocket.sendBool(file.exists());
    }

    private void deleteFile(File file) {
        cloudSocket.sendBool(file.delete());
    }

    private void uploadFile(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        cloudSocket.receiveStream(fileOutputStream);
        fileOutputStream.close();
        // TODO: send confirmation to client?
    }
}