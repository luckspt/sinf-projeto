package pt.fcul.sinf.si003.client;

import pt.fcul.sinf.si003.CloudSocket;
import pt.fcul.sinf.si003.IO;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/*
 * Para usar criar ficheiro keystore:
 * keytool -genkeypair -keysize 2048 -alias jppCloud -keyalg rsa -keystore keystore.jppCloud -storetype PKCS12
 */
public class myCloud {

    private static CloudSocket cloudSocket;
    private static final IO io = new IO("Client");
    private static ClientKeyStore clientKeyStore;
    private static String baseDir = "./";
    private static Sign sign;

    public static String getBaseDir() {
        return baseDir;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, SignatureException {
        // Check arguments
        Map<String, List<String>> arguments = io.parseArguments(args);

        // Required arguments
        if (!arguments.containsKey("a"))
            io.errorAndExit("Missing server address parameter (-a)");

        // Only allow one method (one of -c, -s, -e, -g), because they are mutually exclusive
        String method = null;
        for (String key : arguments.keySet()) {
            if (key.equals("c") || key.equals("s") || key.equals("e") || key.equals("g")) {
                if (method != null)
                    io.errorAndExit("There must be at most one method (one of -c, -s, -e, -g)");
                method = key;
            }
        }

        if (method == null)
            io.errorAndExit("Missing method parameter (one of -c, -s, -e, -g)");

        // EXTRA: base directory
        if (arguments.containsKey("d"))
            baseDir = arguments.get("d").get(0);

        // EXTRA: keystore path, password, alias, and password
        String keyStoreAlias = arguments.get("-keyStoreAlias") != null ? arguments.get("-keyStoreAlias").get(0) : "jpp";
        String keyStorePassword = arguments.get("-keyStorePassword") != null ? arguments.get("-keyStorePassword").get(0) : "123456";
        String keyStoreAliasPassword = arguments.get("-keyStoreAliasPassword") != null ? arguments.get("-keyStoreAliasPassword").get(0) : "123456";
        clientKeyStore = new ClientKeyStore(getBaseDir(), keyStoreAlias, keyStorePassword, keyStoreAliasPassword);

        // Validate server address and port
        String serverAddress = arguments.get("a").get(0);
        if (serverAddress != null && !serverAddress.matches("^(localhost|(?:[0-9]{1,3}\\.){3}[0-9]{1,3}):[0-9]{1,5}$"))
            io.errorAndExit("Invalid server address. Must be in the format: localhost:port or ip:port");

        // Validate file names and remove duplicates
        List<String> fileNames = new ArrayList<>(new HashSet<>(arguments.get(method)));
        validateFileNames(fileNames);

        // Connect to server
        String[] serverAddressSplit = serverAddress.split(":");
        try {
            io.printMessage("Connecting to server " + serverAddressSplit[0] + ":" + serverAddressSplit[1] + "...");
            cloudSocket = new CloudSocket(serverAddressSplit[0], Integer.parseInt(serverAddressSplit[1]));
            io.printMessage("Connected to server " + cloudSocket.getRemoteAddress());
        } catch (IOException e) {
            io.errorAndExit("Could not connect to server: " + e.getMessage());
        }

        // Execute the method
        for (String fileName : fileNames) {
            // Validate file existence locally, only if it's not a download
            File file = io.openFile(getBaseDir(), fileName, !method.equals("g"));

            // and on the server
            // TODO: FIX! this does NOT include the added extensions (.cifrado, .assinado)
            boolean exists = fileExistsInServer(file);
            if (!exists && method.equals("g")) {
                // -g requires the file to exist, so error if it doesn't exist
                io.error("File " + fileName + " does not exist in the server");
                continue;
            } else if (exists && !method.equals("g")) {
                // all other methods require the file to not exist, so error if it exists
                io.error("File " + fileName + " already exists in the server");
                continue;
            }

            switch (method) {
                case "c":
                    // Hybrid encryption
                    io.printMessage("Performing hybrid encryption on file " + fileName + "...");
                    hybridEncryption(file);
                    break;
                case "s":
                    io.printMessage("Signing file " + fileName + "...");
                    signFile(file);
                    break;
                case "e":
                    io.printMessage("Performing hybrid encryption and signing file " + fileName + "...");
                    // TODO: using hybrid encryption and sign file
                    break;
                case "g":
                    downloadAndDecipherFile(file);
                    break;
                default:
                    io.errorAndExit("Invalid method");
            }
        }

        cloudSocket.close();
    }

    /**
     * Read the file on the path, encrypt it, and send it to the server
     *
     * @param file The file to encrypt
     */
    private static void hybridEncryption(File file) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, KeyStoreException, IllegalBlockSizeException {
        String cipheredFileName = file.getName() + ".cifrado";

        // Create the streams
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedFileStream = new BufferedInputStream(fileInputStream);
        // encrypted file is a temporary file
        File encryptedFile = new File(getBaseDir(), cipheredFileName);
        FileOutputStream encryptedFileOutputBuffer = new FileOutputStream(encryptedFile);

        // Generate the key and encrypt the file
        io.printMessage("Encrypting file with AES 128...");
        Symmetric symmetric = new Symmetric("AES", 128);
        SecretKey symmetricKey = symmetric.generateKey();
        symmetric.encrypt(symmetricKey, bufferedFileStream, encryptedFileOutputBuffer);

        // Read from the temporary file
        FileInputStream encryptedFileInputStream = new FileInputStream(encryptedFile);
        BufferedInputStream encryptedFileBufferedStream = new BufferedInputStream(encryptedFileInputStream);

        // Send the encrypted file to the server
        io.printMessage("Sending encrypted file to server...");
        cloudSocket.sendString("upload " + cipheredFileName);
        cloudSocket.sendStream((int)encryptedFile.length(), encryptedFileBufferedStream);

        // Get the public key from the keystore
        Certificate certificate = clientKeyStore.getAliasCertificate();
        PublicKey publicKey = certificate.getPublicKey();

        // Wrap the symmetric key with the public key
        io.printMessage("Wrapping symmetric key with public key...");
        Asymmetric asymmetric = new Asymmetric("RSA", 2048);
        byte[] wrappedKey = asymmetric.wrapKey(symmetricKey, publicKey);

        // Send the wrapped key to the server
        io.printMessage("Sending wrapped key to server...");
        cloudSocket.sendString("upload " + file.getName() + ".chave_secreta");
        cloudSocket.sendStream(wrappedKey.length, new ByteArrayInputStream(wrappedKey));

        encryptedFileBufferedStream.close();
        encryptedFileInputStream.close();

        // Delete the temporary file
        encryptedFileOutputBuffer.close();
        encryptedFile.delete();

        // Close the streams
        fileInputStream.close();
        bufferedFileStream.close();
    }

    private static void signFile(File file) throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, SignatureException, InvalidKeyException {
        // Create the streams
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        // Get private key from keystore
        PrivateKey privateKey = (PrivateKey) clientKeyStore.getAliasKey();

        // Sign file
        io.printMessage("Signing file with SHA256withRSA...");
        Sign signature = new Sign("SHA256withRSA");
        byte[] signatureData = signature.sign(bufferedInputStream, privateKey);

        // Send the signature to the server
        io.printMessage("Sending signature to server...");
        cloudSocket.sendString("upload " + file.getName() + ".assinatura");
        cloudSocket.sendStream(signatureData.length, new ByteArrayInputStream(signatureData));

        // Send the signed file to the server
        io.printMessage("Sending signed file to server...");
        cloudSocket.sendString("upload " + file.getName() + ".assinado");
        cloudSocket.sendStream((int) file.length(), bufferedInputStream);
    }

    private static void decipherHybridEncryption(String keyName, InputStream fileInputStream, OutputStream outputStream) throws IOException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Download wrapped key from the server
        ByteArrayOutputStream wrappedKeyOutputStream = new ByteArrayOutputStream();

        io.printMessage("Downloading wrapped key " + keyName + "...");

        // Make sure the wrapped key exists
        cloudSocket.sendString("exists " + keyName);
        if (!cloudSocket.receiveBool()) {
            io.error("Wrapped key " + keyName + " does not exist in the server");
            return;
        }

        // Download the wrapped key
        cloudSocket.sendString("download " + keyName);
        cloudSocket.receiveStream(wrappedKeyOutputStream);

        // Unwrap the symmetric key
        io.printMessage("Wrapped key downloaded!");
        io.printMessage("Unwrapping symmetric key with private key...");

        // Get the private key from the keystore
        PrivateKey privateKey = (PrivateKey) clientKeyStore.getAliasKey();

        // Unwrap the symmetric key with the private key
        Asymmetric asymmetric = new Asymmetric("RSA", 2048);
        SecretKey symmetricKey = (SecretKey) asymmetric.unWrapKey(wrappedKeyOutputStream.toByteArray(), privateKey, "AES");

        io.printMessage("Symmetric key unwrapped!");
        io.printMessage("Decrypting file with AES 128...");

        // Decrypt the file
        Symmetric symmetric = new Symmetric("AES", 128);
        symmetric.decrypt(symmetricKey, fileInputStream, outputStream);
    }

    /**
     * Download the file from the server, decipher it, and validate the signature
     *
     * @param file The file to download
     * @throws IOException If there is an error reading the file
     */
    private static void downloadAndDecipherFile(File file) throws IOException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Create the streams
        // Create a temporary file to store the file
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        BufferedOutputStream fileBufferedOutputStream = new BufferedOutputStream(fileOutputStream);

        // Download file to the output stream
        io.printMessage("Downloading file " + file.getName() + "...");
        cloudSocket.sendString("download " + file.getName());
        cloudSocket.receiveStream(fileBufferedOutputStream);

        io.printMessage("File " + file.getName() + " downloaded!");

        // Read the temporary file
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream fileBufferedInputStream = new BufferedInputStream(fileInputStream);

        int lastIndexOfDot = file.getName().lastIndexOf(".");
        String fileName = file.getName().substring(0, lastIndexOfDot);
        String extension = file.getName().substring(lastIndexOfDot + 1);

        File outputFile = new File(getBaseDir(), fileName);
        FileOutputStream outputFileOutputStream = new FileOutputStream(outputFile);
        BufferedOutputStream outputFileBufferedOutputStream = new BufferedOutputStream(outputFileOutputStream);
        switch (extension) {
            // If the file is encrypted with hybrid encryption
            case "cifrado":
                decipherHybridEncryption(fileName + ".chave_secreta", fileBufferedInputStream, outputFileBufferedOutputStream);
                break;
            // If the file is signed
            case "assinado":
                // verifySignature(file)
                break;
            // If the file is encrypted with hybrid encryption and signed
            case "seguro":
                break;
            default:
                io.errorAndExit("Invalid file extension");
        }

        // Close the streams
        fileBufferedInputStream.close();
        fileInputStream.close();
        fileBufferedOutputStream.close();
        fileOutputStream.close();

        // Delete the temporary file
        file.delete();
    }

    /**
     * Check if the file names are valid
     *
     * @param fileNames File names to check
     */
    private static void validateFileNames(List<String> fileNames) {
        for (String fileName : fileNames) {
            if (!fileName.matches("^[^<>:;,?\"*|/]+$")) {
                io.errorAndExit("Invalid file name: " + fileName);
            }
        }
    }

    /**
     * Check if the file exists in the server
     *
     * @param file The file to check
     * @return True if the file exists in the server, false otherwise
     */
    private static boolean fileExistsInServer(File file) {
        cloudSocket.sendString("exists " + file.getName());
        return cloudSocket.receiveBool();
    }


}
