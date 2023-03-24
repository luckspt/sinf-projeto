package pt.fcul.sinf.si003;

import java.io.*;
import java.net.Socket;

public class CloudSocket {
    private final Socket socket;
    private final int CHUNK_SIZE = 1024;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private ObjectInputStream getIn() throws IOException {
        if (in == null)
            in = new ObjectInputStream(socket.getInputStream());
        return in;
    }

    private ObjectOutputStream getOut() throws IOException {
        if (out == null)
            out = new ObjectOutputStream(socket.getOutputStream());
        return out;
    }

    public CloudSocket(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    public CloudSocket(Socket socket) throws IOException {
        this.socket = socket;
    }

    /**
     * Send a string to the socket
     *
     * @param string String to send
     */
    public void sendString(String string) {
        try {
            getOut().writeObject(string);
            getOut().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send an int to the socket
     *
     * @param value Int to send
     */
    public void sendInt(int value) {
        try {
            getOut().writeInt(value);
            getOut().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBool(boolean bool) {
        try {
            getOut().writeBoolean(bool);
            getOut().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a buffer to the socket
     *
     * @param length      Length of the buffer
     * @param inputBuffer Buffer to send
     */
    public void sendStream(int length, InputStream inputBuffer) {
        try {
            // Send the length of the file
            this.sendInt(length);

            // Buffer of chunkSize bytes
            byte[] buffer = new byte[CHUNK_SIZE];

            // Send the file in chunks of CHUNK_SIZE bytes, until the end
            do {
                // Read chunkSize bytes from the file
                int bytesRead = inputBuffer.read(buffer, 0, Math.min(CHUNK_SIZE, length));
                // EOF
                if (bytesRead <= 0) {
                    break;
                }

                // Send the chunk and update the length to be sent
                this.sendBytes(buffer, bytesRead);
                length -= bytesRead;
            } while (true);
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
            getOut().write(bytes, 0, length);
            getOut().flush();
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
        try {
            return (String) getIn().readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public int receiveInt() {
        try {
            return getIn().readInt();
        } catch (IOException e) {
            return -1;
        }
    }

    public boolean receiveBool() {
        try {
            return getIn().readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Receive a stream from the socket
     *
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
                int bytesRead = this.receiveBytes(buffer, Math.min(CHUNK_SIZE, length));

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
     *
     * @param bytes  Byte array to receive the stream
     * @param length Length of the byte array
     * @return Number of bytes received
     */
    public int receiveBytes(byte[] bytes, int length) {
        try {
            return getIn().read(bytes, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getRemoteAddress() {
        return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }
}
