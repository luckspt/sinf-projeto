package pt.fcul.sinf.si003.client;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public class Asymmetric {
    private final String algorithm;

    public Asymmetric(String transformation) {
        this.algorithm = transformation;
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
