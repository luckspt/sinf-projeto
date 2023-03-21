package client;

import javax.crypto.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Symetric {
    private final KeyGenerator keyGen;
    public Symetric(String algorithm, int keySize) throws NoSuchAlgorithmException {
        this.keyGen = KeyGenerator.getInstance(algorithm);
        this.keyGen.init(keySize);
    }


    public SecretKey generateKey() throws NoSuchAlgorithmException {
        return keyGen.generateKey();
    }

    public void encrypt(SecretKey key, FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        try (fileInputStream; fileOutputStream; CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException ioException) {
            // ioException.printStackTrace();
        }
    }

    public void decrypt(SecretKey key, FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        try (fileInputStream; fileOutputStream; CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, cipher)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                cipherInputStream.read(buffer, 0, bytesRead);
            }
        } catch (IOException ioException) {
            // ioException.printStackTrace();
        }
    }
}
