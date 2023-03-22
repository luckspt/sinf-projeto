package pt.fcul.sinf.si003.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientSocket {
    private final Socket socket;

    private final int CHUNK_SIZE = 1024;

    public ClientSocket(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
    }

    /**
     * Send a string to the socket
     *
     * @param string String to send
     */
    public void sendString(String string) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        this.sendStream(string.length(), byteArrayInputStream);
    }

    /**
     * Send an int to the socket
     * @param value Int to send
     */
    public void sendInt(int value) {
        try {
            socket.getOutputStream().write(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBool(boolean bool) {
        this.sendInt(bool ? 1 : 0);
    }

    /**
     * Send a buffer to the socket
     *
     * @param length      Length of the buffer
     * @param inputBuffer Buffer to send
     */
    public void sendStream(int length, InputStream inputBuffer) {
        try {
            // Buffer of chunkSize bytes
            byte[] buffer = new byte[CHUNK_SIZE];

            // Send the file in chunks of chunkSize bytes, until the end
            do {
                // Read chunkSize bytes from the file
                int bytesRead = inputBuffer.read(buffer, 0, CHUNK_SIZE);
                // EOF
                if (bytesRead == -1) {
                    break;
                }

                // Send the chunk and update the length to be sent
                this.sendBytes(buffer, bytesRead);
                length -= bytesRead;
            } while (length > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a byte array to the socket
     *
     * @param bytes  Byte array to send
     * @param length Length of the byte array
     */
    public void sendBytes(byte[] bytes, int length) {
        try {
            this.sendInt(length);
            socket.getOutputStream().write(bytes, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the socket
     */
    public void close() throws IOException {
        socket.close();
    }

    public String receiveString() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.receiveStream(byteArrayOutputStream);
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    }

    public int receiveInt() {
        try {
            return socket.getInputStream().read();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Receive a stream from the socket
     * @param outputBuffer Buffer to receive the stream
     */
    public void receiveStream(OutputStream outputBuffer) {
        try {
            // Length of the file
            int length = this.receiveInt();

            // Buffer of chunkSize bytes
            byte[] buffer = new byte[CHUNK_SIZE];

            // Receive the file in chunks of chunkSize bytes, until the end
            do {
                // Read chunkSize bytes from the socket
                int bytesRead = this.receiveBytes(buffer, CHUNK_SIZE);
                // EOF
                if (bytesRead == -1) {
                    break;
                }

                // Send the chunk and update the length to be sent
                outputBuffer.write(buffer, 0, bytesRead);
                length -= bytesRead;
            } while (length > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive a byte array from the socket
     * @param bytes Byte array to receive the stream
     * @param length Length of the byte array
     * @return Number of bytes received
     */
    public int receiveBytes(byte[] bytes, int length) {
        try {
            return socket.getInputStream().read(bytes, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
