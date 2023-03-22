package pt.fcul.sinf.si003.client;

import javax.crypto.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Symmetric {
    private final KeyGenerator keyGen;
    public Symmetric(String algorithm, int keySize) throws NoSuchAlgorithmException {
        this.keyGen = KeyGenerator.getInstance(algorithm);
        this.keyGen.init(keySize);
    }

    public SecretKey generateKey() {
        return keyGen.generateKey();
    }

    public void encrypt(SecretKey key, InputStream inputStream, OutputStream outputStream) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(this.keyGen.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, key);

        try (inputStream; outputStream; CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException ioException) {
            // ioException.printStackTrace();
        }
    }

    public void decrypt(SecretKey key, InputStream inputStream, OutputStream outputStream) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(this.keyGen.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, key);

        try (inputStream; outputStream; CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = cipherInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException ioException) {
            // ioException.printStackTrace();
        }
    }
}
