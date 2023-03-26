package pt.fcul.sinf.si003.client;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;

public class Sign {
    private final String algorithm;

    public Sign(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Read the input stream and sign it with a signature of the given algorithm and private key.
     * 
     * @param inputStream the input stream to sign
     * @param privateKey the private key to sign the input stream
     * 
     * @return the signature of the input stream
     */



    public byte[] sign(InputStream inputStream, PrivateKey privateKey) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        // recebe o ficheiro e a chave privada => retorna o ficheiro assinado
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

    public boolean verify(byte[] data, InputStream signatureStream, Certificate certificate) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature newSignature = Signature.getInstance(algorithm);
        newSignature.initVerify(certificate);

        try {
            // newSignature.read(signatureStream);
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
