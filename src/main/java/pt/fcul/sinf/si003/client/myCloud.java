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
    private static Asymmetric asymmetric;
    private static Symmetric symmetric;
    private static Sign signature;
    private static String baseDir = "./";

    public static String getBaseDir() {
        return baseDir;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException, UnrecoverableKeyException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, SignatureException {
        asymmetric = new Asymmetric("RSA", 2048);
        symmetric = new Symmetric("AES", 128);
        signature = new Sign("SHA256withRSA");

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

        // EXTRA: chunk size
        int chunkSize = 1024;
        if (arguments.containsKey("-chunkSize") && arguments.get("-chunkSize").size() == 1 && arguments.get("-chunkSize").get(0).matches("^[0-9]+$"))
            chunkSize = Math.max(1024, Integer.parseInt(arguments.get("-chunkSize").get(0)));

        io.error("Chunk size: " + chunkSize);
        // Connect to server
        String[] serverAddressSplit = serverAddress.split(":");
        try {
            io.printMessage("Connecting to server " + serverAddressSplit[0] + ":" + serverAddressSplit[1] + "...");
            cloudSocket = new CloudSocket(serverAddressSplit[0], Integer.parseInt(serverAddressSplit[1]), chunkSize);
            io.printMessage("Connected to server " + cloudSocket.getRemoteAddress());
        } catch (IOException e) {
            io.errorAndExit("Could not connect to server: " + e.getMessage());
        }

        // Execute the method
        for (String fileName : fileNames) {
            // Validate file existence locally, only if it's not a download
            File file = io.openFile(getBaseDir(), fileName, false);
            if (!method.equals("g") && !file.exists()) {
                io.error("File " + fileName + " does not exist locally.");
                continue;
            }

            // and on the server
            boolean cipheredExists = fileExistsInServer(new File(fileName + FileExtensions.CIFRADO.getExtensionWithDot()));
            boolean signedExists = fileExistsInServer(new File(fileName + FileExtensions.ASSINADO.getExtensionWithDot()));
            boolean secureExists = fileExistsInServer(new File(fileName + FileExtensions.SEGURO.getExtensionWithDot()));

            // If it's a download, check if the file exists on the server
            //  ciphered, signed, or secure are mutually exclusive
            if (!method.equals("g") && (cipheredExists || signedExists || secureExists)) {
                io.error("File " + fileName + " already exists on the server");
                continue;
            }

            try {
                switch (method) {
                    case "c":
                        // Hybrid encryption
                        io.printMessage("Performing hybrid encryption on file " + fileName + "...");
                        hybridEncryption(file, FileExtensions.CIFRADO);
                        break;
                    case "s":
                        io.printMessage("Signing file " + fileName + "...");
                        signFile(file);

                        // Send signed file to server
                        // Read the signed file
                        FileInputStream signedFileInputStream = new FileInputStream(file);
                        BufferedInputStream signedFileBufferedInputStream = new BufferedInputStream(signedFileInputStream);

                        // Send the signed file to the server
                        io.printMessage("Sending signed file to server...");
                        cloudSocket.sendString("upload " + file.getName() + FileExtensions.ASSINADO.getExtensionWithDot());
                        cloudSocket.sendStream((int) file.length(), signedFileBufferedInputStream);

                        // Close signed file input stream
                        signedFileBufferedInputStream.close();
                        signedFileInputStream.close();
                        break;
                    case "e":
                        io.printMessage("Performing hybrid encryption and signing file " + fileName + "...");
                        signFile(file);
                        hybridEncryption(file, FileExtensions.SEGURO);
                        break;
                    case "g":
                        if (!cipheredExists && !signedExists && !secureExists) {
                            io.error("File " + fileName + " does not exist on the server");
                            continue;
                        }

                        downloadAndDecipherFile(file, cipheredExists, signedExists, secureExists);
                        break;
                    default:
                        io.errorAndExit("Invalid method");
                }
            } catch (InvalidKeyException e) {
                io.error("Key error: Invalid key usage on " + fileName + ".");
            } catch (Exception e) {
                io.error("Error: " + fileName + ": " + e.getMessage());
            }
        }

        cloudSocket.close();
    }

    private static void downloadAndDecipherFile(File file, boolean cipheredExists, boolean signedExists, boolean secureExists) throws IOException, UnrecoverableKeyException, NoSuchPaddingException, KeyStoreException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // Establish priority:
        // 1. Secure
        // 2. Signed
        // 3. Ciphered
        if (secureExists) {
            decipherHybridEncryption(file, FileExtensions.SEGURO);
            verifyFile(file);
        } else if (signedExists) {
            // We need to download the signed file first, because verifyFile() will read from disk
            // Download the signed file
            FileOutputStream signedOutputStream = new FileOutputStream(file);
            downloadFile(new File(getBaseDir(), file.getName() + FileExtensions.ASSINADO.getExtensionWithDot()), signedOutputStream);

            // Close signed output file stream
            signedOutputStream.close();

            verifyFile(file);
        }  else if (cipheredExists)
            decipherHybridEncryption(file, FileExtensions.CIFRADO);
    }

    private static void decipherHybridEncryption(File file, FileExtensions cifrado) throws IOException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Download the wrapped symmetric key
        File wrappedKeyFile = new File(getBaseDir(), file.getName() + FileExtensions.CHAVE_SECRETA.getExtensionWithDot());
        ByteArrayOutputStream wrappedKeyOutputStream = new ByteArrayOutputStream();
        downloadFile(wrappedKeyFile, wrappedKeyOutputStream);

        // Unwrap the symmetric key
        io.printMessage("Unwrapping symmetric key...");
        PrivateKey privateKey = (PrivateKey) clientKeyStore.getAliasKey();
        SecretKey symmetricKey = (SecretKey) asymmetric.unWrapKey(wrappedKeyOutputStream.toByteArray(), privateKey, "AES");
        io.printMessage("Symmetric key unwrapped!");

        // Close wrapped key stream
        wrappedKeyOutputStream.close();

        // Download the ciphered file
        File cipheredFile = new File(getBaseDir(), file.getName() + cifrado.getExtensionWithDot());
        FileOutputStream cipheredOutputStream = new FileOutputStream(cipheredFile);
        downloadFile(cipheredFile, cipheredOutputStream);

        // Close ciphered output file stream
        cipheredOutputStream.close();

        // Decipher the file
        FileInputStream cipheredInputStream = new FileInputStream(cipheredFile);
        BufferedInputStream bufferedCipheredInputStream = new BufferedInputStream(cipheredInputStream);
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        io.printMessage("Deciphering " + file.getName() + " with AES 128...");
        symmetric.decrypt(symmetricKey, bufferedCipheredInputStream, fileOutputStream);
        io.printMessage(file.getName() + " deciphered!");

        // Close the streams and delete the temporary file
        bufferedCipheredInputStream.close();
        cipheredInputStream.close();
        cipheredFile.delete();
        fileOutputStream.close();
    }

    private static void verifyFile(File file) throws KeyStoreException, NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException {
        // Download the signature
        File signatureFile = new File(getBaseDir(), file.getName() + FileExtensions.ASSINATURA.getExtensionWithDot());
        ByteArrayOutputStream signatureOutputStream = new ByteArrayOutputStream();
        downloadFile(signatureFile, signatureOutputStream);

        FileInputStream signedInputStream = new FileInputStream(file);
        BufferedInputStream bufferedSignedInputStream = new BufferedInputStream(signedInputStream);

        // Verify the signature
        Certificate certificate = clientKeyStore.getAliasCertificate();
        boolean valid = signature.verify(signatureOutputStream.toByteArray(), bufferedSignedInputStream, certificate);

        // Close the signature stream
        signatureOutputStream.close();

        // Close the streams and delete the temporary file
        bufferedSignedInputStream.close();
        signedInputStream.close();

        if (valid)
            io.printMessage("Signature is valid!");
        else
            io.error("Signature is not valid!");
    }

    /**
     * Read the file on the path, encrypt it, and send it to the server
     *
     * @param file The file to encrypt
     */
    private static void hybridEncryption(File file, FileExtensions cipheredExtension) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, KeyStoreException, IllegalBlockSizeException {
        String cipheredFileName = file.getName() + cipheredExtension.getExtensionWithDot();

        // Create the streams
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedFileStream = new BufferedInputStream(fileInputStream);
        // encrypted file is a temporary file
        File encryptedFile = new File(getBaseDir(), cipheredFileName);
        FileOutputStream encryptedFileOutputBuffer = new FileOutputStream(encryptedFile);

        // Generate the key and encrypt the file
        io.printMessage("Encrypting file with AES 128...");
        SecretKey symmetricKey = symmetric.generateKey();
        symmetric.encrypt(symmetricKey, bufferedFileStream, encryptedFileOutputBuffer);

        // Close file input stream and encrypted file output stream
        bufferedFileStream.close();
        fileInputStream.close();
        encryptedFileOutputBuffer.close();

        // Read from the temporary file
        FileInputStream encryptedFileInputStream = new FileInputStream(encryptedFile);
        BufferedInputStream encryptedFileBufferedInputStream = new BufferedInputStream(encryptedFileInputStream);

        // Send the encrypted file to the server
        io.printMessage("Sending encrypted file to server...");
        cloudSocket.sendString("upload " + cipheredFileName);
        cloudSocket.sendStream((int) encryptedFile.length(), encryptedFileBufferedInputStream);

        // Close file input stream and delete the temporary file
        encryptedFileBufferedInputStream.close();
        encryptedFileInputStream.close();
        encryptedFile.delete();

        // Get the public key from the keystore
        Certificate certificate = clientKeyStore.getAliasCertificate();
        PublicKey publicKey = certificate.getPublicKey();

        // Wrap the symmetric key with the public key
        io.printMessage("Wrapping symmetric key with public key...");
        byte[] wrappedKey = asymmetric.wrapKey(symmetricKey, publicKey);

        // Send the wrapped key to the server
        io.printMessage("Sending wrapped key to server...");
        cloudSocket.sendString("upload " + file.getName() + FileExtensions.CHAVE_SECRETA.getExtensionWithDot());
        cloudSocket.sendStream(wrappedKey.length, new ByteArrayInputStream(wrappedKey));
    }


    /**
     * Read the file received to sign, create a signed file to the client and send the used signature to the server
     *
     * @param file The file to sign
     *
     * @throws IOException If an I/O error occurs
     * @throws KeyStoreException If the keystore has not been initialized
     * @throws NoSuchAlgorithmException If the algorithm used to check the integrity of the keystore cannot be found
     * @throws UnrecoverableKeyException If the key cannot be recovered
     * @throws SignatureException If the signature algorithm is unable to process the input data provided
     * @throws InvalidKeyException If the key used to initialize the signature is invalid
     */


    /**
     * Read the file received to sign, create a signed file and a signature and send them to the server
     *
     * @param file The file to sign
     * @throws IOException               If an I/O error occurs
     * @throws KeyStoreException         If the keystore has not been initialized
     * @throws NoSuchAlgorithmException  If the algorithm used to check the integrity of the keystore cannot be found
     * @throws UnrecoverableKeyException If the key cannot be recovered
     * @throws SignatureException        If the signature algorithm is unable to process the input data provided
     * @throws InvalidKeyException       If the key used to initialize the signature is invalid
     */

    private static void signFile(File file) throws IOException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, SignatureException, InvalidKeyException {
        // Create the streams
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        // Get private key from keystore
        PrivateKey privateKey = (PrivateKey) clientKeyStore.getAliasKey();

        // Sign file
        io.printMessage("Signing file with SHA256withRSA...");
        byte[] signatureData = signature.sign(bufferedInputStream, privateKey);

        // Close file input stream
        bufferedInputStream.close();
        fileInputStream.close();

        // Create the stream for the signature
        ByteArrayInputStream signatureInputStream = new ByteArrayInputStream(signatureData);

        // Send the signature to the server
        io.printMessage("Sending signature to server...");
        cloudSocket.sendString("upload " + file.getName() + FileExtensions.ASSINATURA.getExtensionWithDot());
        cloudSocket.sendStream(signatureData.length, signatureInputStream);

        // Close signature input stream
        signatureInputStream.close();
    }

    private static void downloadFile(File file, OutputStream outputStream) throws IOException {
        // Check if the file exists in the server
        cloudSocket.sendString("exists " + file.getName());
        if (!cloudSocket.receiveBool()) {
            throw new IOException("File " + file.getName() + " does not exist in the server");
        }

        // Download file to the output stream
        io.printMessage("Downloading file " + file.getName() + "...");
        cloudSocket.sendString("download " + file.getName());
        cloudSocket.receiveStream(outputStream);
        io.printMessage("File " + file.getName() + " downloaded!");
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
