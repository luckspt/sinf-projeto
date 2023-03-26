package pt.fcul.sinf.si003;

import java.io.*;
import java.net.Socket;

/**
 * The CloudSocket abstracts the socket communication for the service
 * <p>
 * It provides methods to send and receive different types of data
 */
public class CloudSocket {
    /**
     * The socket used for communication
     */
    private final Socket socket;

    /**
     * The size of the chunks used to send streams
     */
    private int chunkSize;

    /**
     * The ObjectInputStream
     */
    private ObjectInputStream in;

    /**
     * The ObjectOutputStream
     */
    private ObjectOutputStream out;

    /**
     * Get the ObjectInputStream
     *
     * @return The ObjectInputStream
     * @throws IOException If an error occurs
     */
    private ObjectInputStream getIn() throws IOException {
        if (in == null)
            in = new ObjectInputStream(socket.getInputStream());
        return in;
    }

    /**
     * Get the ObjectOutputStream
     *
     * @return The ObjectOutputStream
     * @throws IOException If an error occurs
     */
    private ObjectOutputStream getOut() throws IOException {
        if (out == null)
            out = new ObjectOutputStream(socket.getOutputStream());
        return out;
    }

    /**
     * Create a new CloudSocket
     *
     * @param host Hostname or IP
     * @param port Port
     * @throws IOException If an error occurs
     */
    public CloudSocket(String host, int port, int chunkSize) throws IOException {
        this(new Socket(host, port), chunkSize);
    }

    /**
     * Create a new CloudSocket from a socket
     *
     * @param socket Socket to use
     * @param chunkSize Size of the chunks in bytes
     * @throws IOException If an error occurs
     */
    public CloudSocket(Socket socket, int chunkSize) throws IOException {
        this.socket = socket;
        this.chunkSize = chunkSize;
    }

    /**
     * Send a string
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
     * Send an integer
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

    /**
     * Send a boolean
     *
     * @param bool Boolean to send
     */
    public void sendBool(boolean bool) {
        try {
            getOut().writeBoolean(bool);
            getOut().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a stream
     *
     * @param length      Length of the stream
     * @param inputStream Stream to send
     */
    public void sendStream(int length, InputStream inputStream) {
        try {
            // Send the length of the file
            this.sendInt(length);

            // Buffer of chunkSize bytes
            byte[] buffer = new byte[chunkSize];

            // Send the file in chunks of CHUNK_SIZE bytes, until the end
            do {
                // Read chunkSize bytes from the file
                int bytesRead = inputStream.read(buffer, 0, Math.min(chunkSize, length));
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
     * Send a byte array
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
            byte[] buffer = new byte[chunkSize];

            // Receive the file in chunks of chunkSize bytes, until the end
            do {
                // Read chunkSize bytes from the socket
                int bytesRead = this.receiveBytes(buffer, Math.min(chunkSize, length));

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
