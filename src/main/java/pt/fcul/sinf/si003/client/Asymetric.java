package pt.fcul.sinf.si003.client;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class Asymetric {
    private final String transformation;
    private final int keySize;

    public Asymetric(String transformation, int keySize) {
        this.transformation = transformation;
        this.keySize = keySize;
    }

    public byte[] wrapKey(Key key, PublicKey publicKey) throws InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return cipher.wrap(key);
    }
}
