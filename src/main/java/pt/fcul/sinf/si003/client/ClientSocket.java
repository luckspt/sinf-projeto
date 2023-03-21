package pt.fcul.sinf.si003.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientSocket {
    private final Socket socket;

    private final int CHUNK_SIZE = 1024;

    public ClientSocket(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
    }

    public void sendString(String string) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
        this.sendStream(string.length(), byteArrayInputStream);
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
                sendBytes(buffer, bytesRead);
                length -= bytesRead;
            } while (length > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBytes(byte[] bytes, int length) {
        try {
            socket.getOutputStream().write(length);
            socket.getOutputStream().write(bytes, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        socket.close();
    }
}
