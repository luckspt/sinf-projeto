package pt.fcul.sinf.si003.client;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public class Asymmetric {
    private final String algorithm;
    private final int keySize;

    public Asymmetric(String transformation, int keySize) {
        this.algorithm = transformation;
        this.keySize = keySize;
    }

    public byte[] wrapKey(Key key, PublicKey publicKey) throws InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return cipher.wrap(key);
    }

    public Key unWrapKey(byte[] key, PrivateKey privateKey, String wrappedKeyAlgorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.UNWRAP_MODE, privateKey);
        return cipher.unwrap(key, wrappedKeyAlgorithm, Cipher.SECRET_KEY);
    }
}
