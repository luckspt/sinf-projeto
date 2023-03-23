package pt.fcul.sinf.si003.client;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;

public class Sign {
    private final String algorithm;

    public Sign(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
    }

    public byte[] sign(InputStream inputStream, PrivateKey privateKey) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);

        try {
            byte[] buffer = new byte[1024];

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                signature.update(buffer, 0, bytesRead);
            }

            return signature.sign();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean verify(byte[] data, InputStream signatureStream, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(algorithm);
        signature.initVerify(publicKey);

        try {
            byte[] buffer = new byte[1024];

            int bytesRead;
            while ((bytesRead = signatureStream.read(buffer)) != -1) {
                signature.update(buffer, 0, bytesRead);
            }

            return signature.verify(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
