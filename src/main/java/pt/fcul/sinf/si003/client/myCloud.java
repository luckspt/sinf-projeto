package pt.fcul.sinf.si003.client;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;


/*
 * Para usar criar ficheiro keystore:
 * keytool -genkeypair -keysize 2048 -alias jppCloud -keyalg rsa -keystore keystore.jppCloud -storetype PKCS12
 */

public class myCloud {
    private static final String KEYSTORE_PASSWORD = "123456";
    private static final String KEYSTORE_ALIAS = "pedro";

    private static ClientSocket clientSocket;
    private static ClientKeyStore clientKeyStore;

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
        // Check arguments
        Map<String, List<String>> arguments = IO.parseArguments(args);

        // Required arguments
        if (!arguments.containsKey("a"))
            IO.errorAndExit("Missing server address parameter (-a)");

        // Only allow one method (one of -c, -s, -e, -g), because they are mutually exclusive
        int methods = 0;
        for (String key : arguments.keySet()) {
            if (key.equals("c") || key.equals("s") || key.equals("e") || key.equals("g"))
                methods++;
        }

        if (methods != 1)
            IO.errorAndExit("There must be exactly one method (one of -c, -s, -e, -g)");

        // EXTRA: keystore path, password, alias, and password
        String keyStoreAlias = arguments.get("ksAlias") != null ? arguments.get("ksAlias").get(0) : "jpp";
        String keyStorePassword = arguments.get("ksPassword") != null ? arguments.get("ksPassword").get(0) : "123456";
        String keyStoreAliasPassword = arguments.get("ksAliasPassword") != null ? arguments.get("ksAliasPassword").get(0) : "123456";
        clientKeyStore = new ClientKeyStore(keyStoreAlias, keyStorePassword, keyStoreAliasPassword);

        // Validate server address and port
        String serverAddress = arguments.get("a").get(0);
        if (serverAddress != null && !serverAddress.matches("^(localhost|(?:[0-9]{1,3}\\.){3}[0-9]{1,3}):[0-9]{1,5}$"))
            IO.errorAndExit("Invalid server address. Must be in the format: localhost:port or ip:port");

        // Get the other key (except "a")
        String[] keys = Arrays.stream(arguments.keySet().toArray()).toArray(String[]::new);
        String method = keys[0].equals("a") ? keys[1] : keys[0];

        // Validate file names and remove duplicates
        List<String> fileNames = new ArrayList<>(new HashSet<>(arguments.get(method)));
        validateFileNames(fileNames);

        // Connect to server
        String[] serverAddressSplit = serverAddress.split(":");
        try {
            clientSocket = new ClientSocket(serverAddressSplit[0], Integer.parseInt(serverAddressSplit[1]));
        } catch (IOException e) {
            IO.errorAndExit("Could not connect to server: " + e.getMessage());
        }

        // Execute the method
        for (String fileName : fileNames) {
            // Validate file existence locally, only if it's not a download
            File file = IO.openFile(fileName, !method.equals("g"));

            // and on the server
            boolean exists = fileExistsInServer(file);
            System.out.println("exists: " + exists);
            if (!exists && method.equals("g")) {
                // -g requires the file to exist, so error if it doesn't exist
                IO.error("File " + fileName + " does not exist in the server");
                continue;
            } else if (exists && !method.equals("g")) {
                // all other methods require the file to not exist, so error if it exists
                IO.error("File " + fileName + " already exists in the server");
                continue;
            }

            switch (method) {
                case "c":
                    // Hybrid encryption
                    IO.printMessage("Performing hybrid encryption on file " + fileName + "...");
                    hybridEncryption(file);
                    break;
                case "s":
                    IO.printMessage("Signing file " + fileName + "...");
                    signFile(file);
                    break;
                case "e":
                    IO.printMessage("Performing hybrid encryption and signing file " + fileName + "...");
                    // TODO: using hybrid encryption and sign file
                    break;
                case "g":
                    IO.printMessage("Downloading file " + fileName + "...");
                    downloadAndDecipherFile(file);
                    break;
                default:
                    IO.errorAndExit("Invalid method");
            }
        }

        clientSocket.close();
    }

    /**
     * Read the file on the path, encrypt it, and send it to the server
     *
     * @param file The file to encrypt
     */
    private static void hybridEncryption(File file) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, KeyStoreException, IllegalBlockSizeException {
        // Create the streams
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Generate the key and encrypt the file
        Symmetric symmetric = new Symmetric("AES", 128);
        SecretKey symmetricKey = symmetric.generateKey();
        symmetric.encrypt(symmetricKey, bufferedInputStream, byteArrayOutputStream);

        // Send the encrypted file to the server
        clientSocket.sendString("upload " + file.getName() + ".cifrado");
        clientSocket.sendStream(byteArrayOutputStream.size(), new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

        // Get the public key from the keystore
        Certificate certificate = clientKeyStore.getAliasCertificate();
        PublicKey publicKey = certificate.getPublicKey();

        // Wrap the symmetric key with the public key
        Asymetric asymetric = new Asymetric("RSA", 2048);
        byte[] wrappedKey = asymetric.wrapKey(symmetricKey, publicKey);

        // Send the wrapped key to the server
        clientSocket.sendString("upload " + file.getName() + ".chave_secreta");
        clientSocket.sendStream(wrappedKey.length, new ByteArrayInputStream(wrappedKey));

        // Close the streams
        fileInputStream.close();
        bufferedInputStream.close();
        byteArrayOutputStream.close();
    }

    private static void signFile(File file) {

    }

    private static void decipherHybridEncryption(File file, ByteArrayOutputStream fileStream) throws IOException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Download wrapped key from the server
        ByteArrayOutputStream wrappedKeyOutputStream = new ByteArrayOutputStream();
        clientSocket.sendString("download " + file.getName().replace(".cifrado$", ".chave_secreta"));
        clientSocket.receiveStream(wrappedKeyOutputStream);

        // Get the private key from the keystore
        PrivateKey privateKey = (PrivateKey) clientKeyStore.getAliasKey();

        // Unwrap the symmetric key with the private key
        Asymetric asymetric = new Asymetric("RSA", 2048);
        SecretKey symmetricKey = (SecretKey) asymetric.unWrapKey(wrappedKeyOutputStream.toByteArray(), privateKey, "AES");

        // Decrypt the file
        Symmetric symmetric = new Symmetric("AES", 128);
        // TODO: write file to disk or memory?
        symmetric.decrypt(symmetricKey, new ByteArrayInputStream(fileStream.toByteArray()), new FileOutputStream(file));
    }

    /**
     * Download the file from the server, decipher it, and validate the signature
     *
     * @param file The file to download
     * @throws IOException If there is an error reading the file
     */
    private static void downloadAndDecipherFile(File file) throws IOException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Create the streams
        ByteArrayOutputStream fileStream = new ByteArrayOutputStream();

        // Download file to the output stream
        clientSocket.sendString("download " + file.getName());
        clientSocket.receiveStream(fileStream);

        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        switch (extension) {
            // If the file is encrypted with hybrid encryption
            case "cifrado":
                decipherHybridEncryption(file, fileStream);
                break;
            case "assinado":
                break;
            case "seguro":
        }
    }

    /**
     * Check if the file names are valid
     *
     * @param fileNames File names to check
     */
    private static void validateFileNames(List<String> fileNames) {
        for (String fileName : fileNames) {
            if (!fileName.matches("^[^<>:;,?\"*|/]+$")) {
                IO.errorAndExit("Invalid file name: " + fileName);
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
        clientSocket.sendString("exists " + file.getName());
        return clientSocket.receiveBool();
    }
}
