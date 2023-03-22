package pt.fcul.sinf.si003.server;

import pt.fcul.sinf.si003.CloudSocket;
import pt.fcul.sinf.si003.IO;

import java.io.*;

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
            if (command == null || command.isEmpty()) {
                io.printMessage(cloudSocket.getRemoteAddress() + " disconnected");
                break;
            }

            String commandName = command.split(" ")[0];
            String fileName = command.split(" ")[1];
            File file = io.openFile(myCloudServer.getBaseDir(), fileName, false);

            io.printMessage("New request:\n---- Command: " + commandName + "\n---- File   : " + file.getAbsolutePath() + "\n---- Exists : " + file.exists());

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

    private void downloadFile(File file) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        cloudSocket.sendStream((int) file.length(), bufferedInputStream);
    }

    private void uploadFile(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        cloudSocket.receiveStream(fileOutputStream);
        fileOutputStream.close();
        // TODO: send confirmation to client?
    }
}
