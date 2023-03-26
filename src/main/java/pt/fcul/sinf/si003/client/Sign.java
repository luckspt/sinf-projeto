package pt.fcul.sinf.si003.client;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;

/**
 * The Sign abstraction.
 */
public class Sign {
    /**
     * The algorithm to use.
     */
    private final String algorithm;

    /**
     * Creates a new instance of Sign.
     *
     * @param algorithm the algorithm to use
     */
    public Sign(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Signs a file.
     *
     * @param inputStream the file to sign
     * @param privateKey  the private key to use
     */
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

    /**
     * Verifies a file.
     *
     * @param data            the data to verify
     * @param signatureStream the signature to verify
     * @param certificate     the certificate to use
     * @return true if the signature is valid, false otherwise
     */
    public boolean verify(byte[] data, InputStream signatureStream, Certificate certificate) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature newSignature = Signature.getInstance(algorithm);
        newSignature.initVerify(certificate);

        try {
            byte[] buffer = new byte[1024];

            int bytesRead;
            while ((bytesRead = signatureStream.read(buffer)) != -1) {
                newSignature.update(buffer, 0, bytesRead);
            }

            return newSignature.verify(data);
        } catch (IOException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}
