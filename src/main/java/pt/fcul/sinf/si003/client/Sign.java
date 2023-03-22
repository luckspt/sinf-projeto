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
        try {
            Signature signature = Signature.getInstance(algorithm);
            signature.initSign(privateKey);

            byte[] b = new byte[1024];
            int i = fis.read(b);
            while (i != -1) {
                signature.update(data);
                i = fis.read(b);
            }
            return signature.sign();
            
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return null;
        }
        
        }

    public boolean verify(byte[] data, byte[] signature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature1 = Signature.getInstance(algorithm);
        signature1.initVerify(publicKey);
        signature1.update(data);
        return signature1.verify(signature);
    }
}
