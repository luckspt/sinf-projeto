package pt.fcul.sinf.si003.client;

import pt.fcul.sinf.si003.*;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.net.SocketFactory;
import javax.net.ssl.*;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
//./myCloud.sh -a 0.0.0.0:3000 -d files/client/ -g tt.txt.assinado
/**
 * The client.
 */
public class myCloud {

    /**
     * The socket abstraction to the remote
     */
    private static CloudSocket cloudSocket;
    /**
     * The IO abstraction
     */
    private static final IO io = new IO();
    /**
     * The client keystore
     */
    private static ClientKeyStore clientKeyStore;
    /**
     * The asymmetric cryptography abstraction
     */
    private static Asymmetric asymmetric;
    /**
     * The symmetric cryptography abstraction
     */
    private static Symmetric symmetric;
    /**
     * The signature abstraction
     */
    private static Sign signature;
    /**
     * The base directory
     */
    private static String baseDir = "./";

    /**
     * Run the client
     *
     * @param args The arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        asymmetric = new Asymmetric("RSA");
        signature = new Sign("SHA256withRSA");
        try {
            symmetric = new Symmetric("AES", 128);
        } catch (NoSuchAlgorithmException e) {
            // this never happens
        }

        // Check arguments
        Map<String, List<String>> arguments = io.parseArguments(args);

        // Required arguments
        if (!arguments.containsKey("a"))
            io.errorAndExit("Missing server address parameter (-a)");

        // Only allow one method (one of -au, -c, -s, -e, -g), because they are mutually exclusive
        String method = null;
        for (String key : arguments.keySet()) {
            if (isAMethod(key)) {
                if (method != null)
                    io.errorAndExit("There must be at most one method (one of -au, -c, -s, -e, -g)");
                method = key;
            }
        }

        if (method == null)
            io.errorAndExit("Missing method parameter (one of -au, -c, -s, -e, -g)");

        // Method is now guaranteed to be one of -au, -c, -s, -e, -g, -x
        assert method != null;

        // EXTRA: base directory
        if (arguments.containsKey("b"))
            baseDir = arguments.get("b").get(0);

        // Validate server address and port
        String serverAddress = arguments.get("a").get(0);
        if (serverAddress != null && !serverAddress.matches("^(localhost|(?:[0-9]{1,3}\\.){3}[0-9]{1,3}):[0-9]{1,5}$"))
            io.errorAndExit("Invalid server address. Must be in the format: localhost:port or ip:port");

        // Validate file names and remove duplicates
        List<String> fileNames = new ArrayList<>(new HashSet<>(arguments.get(method)));
        // TODO validate each one separately
        validateFileNames(fileNames);

        // EXTRA: chunk size
        int chunkSize = 1024;
        if (arguments.containsKey("-chunkSize") && arguments.get("-chunkSize").size() == 1 && arguments.get("-chunkSize").get(0).matches("^[0-9]+$"))
            chunkSize = Math.max(1024, Math.min(65535, Integer.parseInt(arguments.get("-chunkSize").get(0))));

        // Connect to server
        String[] serverAddressSplit = serverAddress.split(":");
        try {
            io.info("Connecting to server " + serverAddressSplit[0] + ":" + serverAddressSplit[1] + "...");

            System.setProperty("javax.net.ssl.trustStore", baseDir + "/server.truststore");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");

            SocketFactory socketFactory = SSLSocketFactory.getDefault();
            Socket socket = socketFactory.createSocket(serverAddressSplit[0], Integer.parseInt(serverAddressSplit[1]));

            cloudSocket = new CloudSocket(socket, chunkSize);
            io.success("Connected to server " + cloudSocket.getRemoteAddress());
        } catch (IOException e) {
            io.errorAndExit("Could not connect to server: " + e.getMessage());
        }

        // Register user
        String destination_username = null;
        if (method.equals("au")) {
            if (arguments.get("au").size() != 3)
                io.errorAndExit("-au requires 3 parameters. Missing username, password, or certificate path");

            String username = arguments.get("au").get(0);
            String password = arguments.get("au").get(1);
            String certificatePath = arguments.get("au").get(2);
            registerUser(username, password, certificatePath);
            return;
        } else {
            if ((!arguments.containsKey("u") || arguments.get("u").size() != 1) ||
                    (!arguments.containsKey("p") || arguments.get("p").size() != 1)) {
                // Authenticate user
                // Isn't a signup (-au) and doesn't have username or password
                io.errorAndExit("Missing username (-u) or password (-p) parameter");
            } else {
                String username = arguments.get("u").get(0);
                String password = arguments.get("p").get(0);

                if (!authenticateUser(username, password)) {
                    io.errorAndExit("Username or password is incorrect");
                }
                // EXTRA: keystore path, password, alias, and password
                String keyStoreAlias = arguments.get("-keyStoreAlias") != null ? arguments.get("-keyStoreAlias").get(0) : username;
                String keyStorePassword = arguments.get("-keyStorePassword") != null ? arguments.get("-keyStorePassword").get(0) : password;
                String keyStoreAliasPassword = arguments.get("-keyStoreAliasPassword") != null ? arguments.get("-keyStoreAliasPassword").get(0) : password;
                clientKeyStore = new ClientKeyStore(getBaseDir(), keyStoreAlias, keyStorePassword, keyStoreAliasPassword);
                destination_username = username;
            }
        }

        if(arguments.containsKey("d") && arguments.get("d").size() == 1) {
            // G. allow sending to another user
            destination_username = arguments.get("d").get(0);
        }

        // Execute the method
        for (String fileName : fileNames) {
            io.printMessage("========================================");
            io.info("Executing method " + method + " on file " + fileName + "...");

            // Validate file existence locally, only if it's not a download
            File file = new File(getBaseDir(), fileName);
            if (!file.exists()) {
                if (!method.equals("x") && !method.equals("g")) {
                    io.error("File " + fileName + " does not exist locally.");
                    continue;
                }
            } else {
                if (method.equals("g")) {
                    io.error("File " + fileName + " already exists locally.");
                    continue;
                }
            }

            // and on the server
            boolean cipheredExists = fileExistsInServer(destination_username, new File(fileName + FileExtensions.CIFRADO.getExtensionWithDot()));
            boolean signedExists = fileExistsInServer(destination_username, new File(fileName + FileExtensions.ASSINADO.getExtensionWithDot()));
            boolean secureExists = fileExistsInServer(destination_username, new File(fileName + FileExtensions.SEGURO.getExtensionWithDot()));

            // If it's a download, check if the file exists on the server
            //  ciphered, signed, or secure are mutually exclusive
            if ((!method.equals("g") && !method.equals("x")) && (cipheredExists || signedExists || secureExists)) {
                io.warning("File " + fileName + " already exists on the server");
                continue;
            }

            try {
                switch (method) {
                    case "c":
                        // Hybrid encryption
                        io.info("Performing hybrid encryption on file " + fileName + "...");
                        hybridEncryption(destination_username, file, FileExtensions.CIFRADO);
                        io.success("Hybrid encrypted file " + fileName);
                        break;
                    case "s":
                        io.info("Signing file " + fileName + "...");
                        signFile(destination_username, file);
                        io.success("Signed file " + fileName);

                        // Send signed file to server
                        // Read the signed file
                        FileInputStream signedFileInputStream = new FileInputStream(file);
                        BufferedInputStream signedFileBufferedInputStream = new BufferedInputStream(signedFileInputStream);

                        // Send the signed file to the server
                        String signedFileName = file.getName() + FileExtensions.ASSINADO.getExtensionWithDot();
                        io.info("Sending " + signedFileName + " to user " + destination_username + " to the server...");
                        cloudSocket.sendString("upload " + destination_username + " " + signedFileName);
                        cloudSocket.sendStream(file.length(), signedFileBufferedInputStream);
                        io.success("Sent " + signedFileName + " to the server");

                        // Close signed file input stream
                        signedFileBufferedInputStream.close();
                        signedFileInputStream.close();
                        break;
                    case "e":
                        io.info("Performing hybrid encryption and signing file " + fileName + "...");
                        signFile(destination_username, file);
                        hybridEncryption(destination_username, file, FileExtensions.SEGURO);
                        io.success("Hybrid encrypted and signed file " + fileName);
                        break;
                    case "g":
                        if (!cipheredExists && !signedExists && !secureExists) {
                            io.error("File " + fileName + " does not exist on the server");
                            continue;
                        }

                        downloadAndDecipherFile(destination_username, file, cipheredExists, signedExists, secureExists);
                        break;
                    case "x":
                        if (!cipheredExists && !signedExists && !secureExists) {
                            io.error("File " + fileName + " does not exist on the server");
                            continue;
                        }

                        deleteFile(destination_username, file, cipheredExists, signedExists, secureExists);
                        break;
                    default:
                        io.errorAndExit("Invalid method");
                }
            } catch (InvalidKeyException e) {
                io.error("Key error: Invalid key usage on " + fileName + ".");
            } catch (SignatureException e) {
                io.error("Signature error: " + fileName + " signature is not valid.");
            } catch (Exception e) {
                io.error("Error: " + fileName + ": " + e.getMessage());
            } finally {
                io.info("Finished executing method " + method + " on file " + fileName + ".\n");
            }
        }

        try {
            cloudSocket.close();
        } catch (IOException e) {
            io.errorAndExit("Could not close connection to server: " + e.getMessage());
        }
    }

    /**
     * Get the base directory
     *
     * @return The base directory
     */
    public static String getBaseDir() {
        return baseDir;
    }

    /**
     * Check if it's a command method
     *
     * @param method The method
     * @return True if it's a method, false otherwise
     */
    private static boolean isAMethod(String method) {
        return method.equals("au") || method.equals("c") || method.equals("s") || method.equals("e") || method.equals("g") || method.equals("x");
    }

    /**
     * Download, decipher and verify a file.
     * <p>
     * The priority is: secure, signed, ciphered
     * <p>
     * It may not decpher not verify the file if it's not necessary
     *
     * @param file           The file
     * @param cipheredExists If the ciphered file exists in the remote
     * @param signedExists   If the signed file exists in the remote
     * @param secureExists   If the secure file exists in the remote
     * @throws CertificateException
     */
    private static void downloadAndDecipherFile(String username, File file, boolean cipheredExists, boolean signedExists, boolean secureExists) throws UnrecoverableKeyException, NoSuchPaddingException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeyException, CertificateException {
        // Establish priority:
        // 1. Secure
        // 2. Signed
        // 3. Ciphered
        if (secureExists) {
            decipherHybridEncryption(username, file, FileExtensions.SEGURO);
            verifyFile(username, file);
        } else if (signedExists) {
            // We need to download the signed file first, because verifyFile() will read from disk
            // Download the signed file
            String signedFileName = file.getName() + FileExtensions.ASSINADO.getExtensionWithDot();
            io.info("Downloading " + signedFileName + " from server...");
            FileOutputStream signedOutputStream = new FileOutputStream(file);
            downloadFile(username, new File(getBaseDir(), signedFileName), signedOutputStream);
            io.success("Downloaded " + signedFileName + " from server!");

            // Close signed output file stream
            signedOutputStream.close();

            verifyFile(username, file);
        } else if (cipheredExists)
            decipherHybridEncryption(username, file, FileExtensions.CIFRADO);
    }

    /**
     * Delete a file in the remote
     *
     * @param file           The file
     * @param cipheredExists If the ciphered file exists in the remote
     * @param signedExists   If the signed file exists in the remote
     * @param secureExists   If the secure file exists in the remote
     */
    private static void deleteFile(String username, File file, boolean cipheredExists, boolean signedExists, boolean secureExists) {
        // Establish priority:
        // 1. Secure
        // 2. Signed
        // 3. Ciphered
        if (secureExists) {
            io.info("Deleting secure file " + file.getName() + " from user " + username + " and its dependencies...");
            cloudSocket.sendString("delete " + username  + " " + file.getName() + FileExtensions.SEGURO.getExtensionWithDot());
            cloudSocket.sendString("delete " + username  + " " + file.getName() + FileExtensions.CHAVE_SECRETA.getExtensionWithDot());
            cloudSocket.sendString("delete " + username  + " " + file.getName() + FileExtensions.ASSINATURA.getExtensionWithDot());
            io.success("File " + file.getName() + " deleted successfully");
        } else if (signedExists) {
            io.info("Deleting signed file " + file.getName() + " from user " + username + " and its dependencies...");
            cloudSocket.sendString("delete " + username  + " " + file.getName() + FileExtensions.ASSINADO.getExtensionWithDot());
            cloudSocket.sendString("delete " + username  + " " + file.getName() + FileExtensions.ASSINATURA.getExtensionWithDot());
            io.success("File " + file.getName() + " deleted successfully");
        } else if (cipheredExists) {
            io.info("Deleting ciphered file " + file.getName() + " from user " + username + " and its dependencies...");
            cloudSocket.sendString("delete " + username  + " " + file.getName() + FileExtensions.CIFRADO.getExtensionWithDot());
            cloudSocket.sendString("delete " + username  + " " + file.getName() + FileExtensions.CHAVE_SECRETA.getExtensionWithDot());
            io.success("File " + file.getName() + " deleted successfully");
        }
    }

    /**
     * Decipher a file using hybrid encryption
     *
     * @param file      The file
     * @param extension The file extension
     */
    private static void decipherHybridEncryption(String username, File file, FileExtensions extension) throws IOException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Download the wrapped symmetric key
        File wrappedKeyFile = new File(getBaseDir(), file.getName() + FileExtensions.CHAVE_SECRETA.getExtensionWithDot());
        ByteArrayOutputStream wrappedKeyOutputStream = new ByteArrayOutputStream();
        downloadFile(username, wrappedKeyFile, wrappedKeyOutputStream);

        // Unwrap the symmetric key
        io.info("Unwrapping symmetric key...");
        PrivateKey privateKey = (PrivateKey) clientKeyStore.getAliasKey();
        SecretKey symmetricKey = (SecretKey) asymmetric.unWrapKey(wrappedKeyOutputStream.toByteArray(), privateKey, "AES");
        io.info("Symmetric key unwrapped!");

        // Close wrapped key stream
        wrappedKeyOutputStream.close();

        // Download the ciphered file
        String cipheredFileName = file.getName() + extension.getExtensionWithDot();
        io.info("Downloading " + cipheredFileName + "...");
        File cipheredFile = new File(getBaseDir(), cipheredFileName);
        FileOutputStream cipheredOutputStream = new FileOutputStream(cipheredFile);
        downloadFile(username, cipheredFile, cipheredOutputStream);
        io.success(cipheredFileName + " downloaded!");

        // Close ciphered output file stream
        cipheredOutputStream.close();

        // Decipher the file
        FileInputStream cipheredInputStream = new FileInputStream(cipheredFile);
        BufferedInputStream bufferedCipheredInputStream = new BufferedInputStream(cipheredInputStream);
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        io.info("Deciphering " + file.getName() + " with AES 128...");
        symmetric.decrypt(symmetricKey, bufferedCipheredInputStream, fileOutputStream);
        io.info(file.getName() + " deciphered!");

        // Close the streams and delete the temporary file
        bufferedCipheredInputStream.close();
        cipheredInputStream.close();
        cipheredFile.delete();
        fileOutputStream.close();
    }

    /**
     * Verify a file's signature.
     * <p>
     * The file must be downloaded first, because this method will read from disk
     * <p>
     * The signature will be downloaded by this method
     *
     * @param file The file
     * @throws CertificateException
     */
    private static boolean verifyFile(String username, File file) throws IOException, NoSuchAlgorithmException, InvalidKeyException, CertificateException {
        // Download the signature
        io.info("Downloading signature of " + file.getName() + " ...");
        File signatureFile = new File(getBaseDir(), file.getName() + FileExtensions.ASSINATURA.getExtensionWithDot());
        ByteArrayOutputStream signatureOutputStream = new ByteArrayOutputStream();
        downloadFile(username, signatureFile, signatureOutputStream);
        io.success("Signature of " + file.getName() + " downloaded!");

        FileInputStream signedInputStream = new FileInputStream(file);
        BufferedInputStream bufferedSignedInputStream = new BufferedInputStream(signedInputStream);

        // Obtain certificate
        File certificateFile = new File(getBaseDir(), username + FileExtensions.CERTIFICADO.getExtensionWithDot());
        io.info("Obtain certificate " + certificateFile + " ...");
        if(!certificateFile.exists()) {
            // certificate file does not exist; request server
            FileOutputStream outputStream = new FileOutputStream(certificateFile);
            io.info("Get certificate file " + certificateFile.getName() + "...");
            cloudSocket.sendString("get-certificate " + username);
            if (!cloudSocket.receiveBool()) {
                throw new IOException("Certificate " + certificateFile.getName() + " does not exist in the server");
            }
            cloudSocket.receiveStream(outputStream);
            outputStream.close();
            io.success("Certificate for " + username + " downloaded!");
        }
        // convert certificate file to type Certificate
        CertificateFactory fac = CertificateFactory.getInstance("X509");
        FileInputStream certificateInputStream = new FileInputStream(certificateFile);
        X509Certificate certificate = (X509Certificate) fac.generateCertificate(certificateInputStream);
        io.success("Certificate obtained!");

        // Verify the signature
        io.info("Verifying signature of " + file.getName() + " ...");
        boolean valid = signature.verify(signatureOutputStream.toByteArray(), bufferedSignedInputStream, certificate);

        // Close the signature stream
        signatureOutputStream.close();

        // Close the streams and delete the temporary file
        bufferedSignedInputStream.close();
        signedInputStream.close();

        if (valid)
            io.success("Signature of " + file.getName() + " is valid!");
        else
            io.warning("Signature of " + file.getName() + "is not valid!");

        return valid;
    }

    /**
     * Read the file on the path, encrypt it with hybrid encryption, and send it to the remote
     *
     * @param file              The file to encrypt
     * @param cipheredExtension The ciphered file extension
     */
    private static void hybridEncryption(String username, File file, FileExtensions cipheredExtension) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, CertificateException {
        String cipheredFileName = file.getName() + cipheredExtension.getExtensionWithDot();

        // Create the streams
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedFileStream = new BufferedInputStream(fileInputStream);
        // encrypted file is a temporary file
        File encryptedFile = new File(getBaseDir(), cipheredFileName);
        FileOutputStream encryptedFileOutputBuffer = new FileOutputStream(encryptedFile);

        // Generate the key and encrypt the file
        io.info("Encrypting " + file.getName() + " with AES 128...");
        SecretKey symmetricKey = symmetric.generateKey();
        symmetric.encrypt(symmetricKey, bufferedFileStream, encryptedFileOutputBuffer);
        io.success(file.getName() + " encrypted!");

        // Close file input stream and encrypted file output stream
        bufferedFileStream.close();
        fileInputStream.close();
        encryptedFileOutputBuffer.close();

        // Read from the temporary file
        FileInputStream encryptedFileInputStream = new FileInputStream(encryptedFile);
        BufferedInputStream encryptedFileBufferedInputStream = new BufferedInputStream(encryptedFileInputStream);

        // Send the encrypted file to the server
        io.info("Sending encrypted " + file.getName() + " from user " + username + " to server...");
        cloudSocket.sendString("upload " + username + " " + cipheredFileName);
        cloudSocket.sendStream(encryptedFile.length(), encryptedFileBufferedInputStream);
        io.success(file.getName() + " sent to server!");

        // Close file input stream and delete the temporary file
        encryptedFileBufferedInputStream.close();
        encryptedFileInputStream.close();
        encryptedFile.delete();

        // Get the certificate from the received user
        // Obtain certificate
        File certificateFile = new File(getBaseDir(), username + FileExtensions.CERTIFICADO.getExtensionWithDot());
        io.info("Obtain certificate " + certificateFile + " ...");
        if(!certificateFile.exists()) {
            // certificate file does not exist; request server
            FileOutputStream outputStream = new FileOutputStream(certificateFile);
            io.info("Get certificate file " + certificateFile.getName() + "...");
            cloudSocket.sendString("get-certificate " + username);
            if (!cloudSocket.receiveBool()) {
                throw new IOException("Certificate " + certificateFile.getName() + " does not exist in the server");
            }
            cloudSocket.receiveStream(outputStream);
            outputStream.close();
            io.success("Certificate for " + username + " downloaded!");
        }

        // convert certificate file to type Certificate
        CertificateFactory fac = CertificateFactory.getInstance("X509");
        FileInputStream certificateInputStream = new FileInputStream(certificateFile);
        X509Certificate certificate = (X509Certificate) fac.generateCertificate(certificateInputStream);
        io.success("Certificate obtained!");

        // Get the public key from the certificate
        PublicKey publicKey = certificate.getPublicKey();

        // Wrap the symmetric key with the public key
        io.info("Wrapping symmetric key with public key...");
        byte[] wrappedKey = asymmetric.wrapKey(symmetricKey, publicKey);
        io.success("Symmetric key wrapped!");

        // Send the wrapped key to the server
        io.info("Sending wrapped key to server...");
        cloudSocket.sendString("upload " + username + " " + file.getName() + FileExtensions.CHAVE_SECRETA.getExtensionWithDot());
        cloudSocket.sendStream(wrappedKey.length, new ByteArrayInputStream(wrappedKey));
        io.success("Wrapped key sent to server!");
    }

    /**
     * Sign a file with the private key from the keystore
     *
     * @param file The file to sign
     */
    private static void signFile(String username, File file) throws IOException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        // Create the streams
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        // Get private key from keystore
        PrivateKey privateKey = (PrivateKey) clientKeyStore.getAliasKey();

        // Sign file
        io.info("Signing " + file.getName() + " with SHA256withRSA...");
        byte[] signatureData = signature.sign(bufferedInputStream, privateKey);
        io.success(file.getName() + " signed!");

        // Close file input stream
        bufferedInputStream.close();
        fileInputStream.close();

        // Create the stream for the signature
        ByteArrayInputStream signatureInputStream = new ByteArrayInputStream(signatureData);

        // Send the signature to the server
        io.info("Sending signature of " + file.getName() + " from user " + username + "  to server...");
        cloudSocket.sendString("upload " + username + " " + file.getName() + FileExtensions.ASSINATURA.getExtensionWithDot());
        cloudSocket.sendStream(signatureData.length, signatureInputStream);
        io.success("Signature of " + file.getName() + " sent to server!");

        // Close signature input stream
        signatureInputStream.close();
    }

    /**
     * Download a file from the remote
     *
     * @param file         The file to download
     * @param outputStream The output stream to write the file to
     */
    private static void downloadFile(String username, File file, OutputStream outputStream) throws IOException {
        // Check if the file exists in the server
        io.info("Checking if file " + file.getName() + " from user " + username + " exists in the server...");
        cloudSocket.sendString("exists " + username + " " + file.getName());
        if (!cloudSocket.receiveBool()) {
            throw new IOException("File " + file.getName() + " does not exist in the server");
        }
        io.success("File " + file.getName() + " exists in the server!");

        // Download file to the output stream
        io.info("Downloading file " + file.getName() + "...");
        cloudSocket.sendString("download " + username + " " + file.getName());
        cloudSocket.receiveStream(outputStream);
        io.success("File " + file.getName() + " downloaded!");
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
     * Check if the file exists in the remote
     *
     * @param file The file to check
     * @return True if the file exists, false otherwise
     */
    private static boolean fileExistsInServer(String username, File file) {
        cloudSocket.sendString("exists " + username + " " + file.getName());
        return cloudSocket.receiveBool();
    }

    private static boolean authenticateUser(String username, String password) {
        cloudSocket.sendString("login " + username + " " + password);
        return cloudSocket.receiveBool();
    }

    private static boolean registerUser(String username, String password, String certificatePath) throws FileNotFoundException {
        // First, check if the certificate exists
        File certificateFile = new File(getBaseDir(), certificatePath);
        if (!certificateFile.exists()) {
            io.errorAndExit("Certificate file " + certificateFile.getName() + " does not exist at " + certificateFile.getAbsolutePath());
        }

        cloudSocket.sendString("signup " + username + " " + password);
        boolean registered = cloudSocket.receiveBool();
        if (!registered) {
            io.errorAndExit("User " + username + " already exists!");
        }

        // Read from the certificate file
        FileInputStream certificateInputStream = new FileInputStream(certificateFile);
        BufferedInputStream certificateBufferedInputStream = new BufferedInputStream(certificateInputStream);

        // Send the certificate file to the server
        io.info("Sending certificate " + certificateFile.getName() + " to server...");
        cloudSocket.sendStream(certificateFile.length(), certificateBufferedInputStream);
        io.success(certificateFile.getName() + " sent to server!");

        return registered;
    }
}
