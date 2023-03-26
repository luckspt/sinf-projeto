package pt.fcul.sinf.si003.client;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

/**
 * Asymmetric encryption and decryption abstraction.
 */
public class Asymmetric {
    /**
     * The algorithm used for encryption and decryption.
     */
    private final String algorithm;

    /**
     * Creates a new instance of Asymmetric.
     *
     * @param transformation The algorithm used for encryption and decryption.
     */
    public Asymmetric(String transformation) {
        this.algorithm = transformation;
    }

    /**
     * Wraps a key using a public key.
     *
     * @param key       The key to be wrapped.
     * @param publicKey The public key used to wrap the key.
     * @return The wrapped key.
     */
    public byte[] wrapKey(Key key, PublicKey publicKey) throws InvalidKeyException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return cipher.wrap(key);
    }

    /**
     * Unwraps a key using a private key.
     *
     * @param key                 The key to be unwrapped.
     * @param privateKey          The private key used to unwrap the key.
     * @param wrappedKeyAlgorithm The algorithm of the wrapped key.
     * @return The unwrapped key.
     */
    public Key unWrapKey(byte[] key, PrivateKey privateKey, String wrappedKeyAlgorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.UNWRAP_MODE, privateKey);
        return cipher.unwrap(key, wrappedKeyAlgorithm, Cipher.SECRET_KEY);
    }
}
