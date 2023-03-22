package pt.fcul.sinf.si003.client;

import java.security.*;

public class Sign {
    private final String algorithm;
    private final int keySize;

    public Sign(String algorithm, int keySize) {
        this.algorithm = algorithm;
        this.keySize = keySize;
    }

    public byte[] sign(byte[] data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public boolean verify(byte[] data, byte[] signature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature1 = Signature.getInstance(algorithm);
        signature1.initVerify(publicKey);
        signature1.update(data);
        return signature1.verify(signature);
    }
}
