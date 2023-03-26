package pt.fcul.sinf.si003.client;

import javax.crypto.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Symmetric encryption and decryption abstraction.
 */
public class Symmetric {
    /**
     * The key generator used to generate the keys.
     */
    private final KeyGenerator keyGen;

    /**
     * Creates a new instance of Symmetric.
     *
     * @param algorithm The algorithm used for encryption and decryption.
     * @param keySize   The size of the key.
     * @throws NoSuchAlgorithmException If the algorithm is not supported.
     */
    public Symmetric(String algorithm, int keySize) throws NoSuchAlgorithmException {
        this.keyGen = KeyGenerator.getInstance(algorithm);
        this.keyGen.init(keySize);
    }

    /**
     * Generates a key.
     *
     * @return The generated key.
     */
    public SecretKey generateKey() {
        return keyGen.generateKey();
    }

    /**
     * Encrypts a file.
     *
     * @param key          The key used to encrypt the file.
     * @param inputStream  The file to be encrypted.
     * @param outputStream The encrypted file.
     */
    public void encrypt(SecretKey key, InputStream inputStream, OutputStream outputStream) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(this.keyGen.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, key);

        try {
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }
            cipherOutputStream.close();
        } catch (IOException ioException) {
        }
    }

    /**
     * Decrypts a file.
     *
     * @param key          The key used to decrypt the file.
     * @param inputStream  The file to be decrypted.
     * @param outputStream The decrypted file.
     */
    public void decrypt(SecretKey key, InputStream inputStream, OutputStream outputStream) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(this.keyGen.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, key);

        try {
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            cipherInputStream.close();
        } catch (IOException ignored) {
        }
    }
}
