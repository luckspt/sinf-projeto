package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class ClientKeyStore {
    private final KeyStore keyStore;
    public ClientKeyStore(String keystorePath, String keystorePassword, String keystoreType) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        FileInputStream fileInputStream = new FileInputStream(keystorePath);
        this.keyStore = KeyStore.getInstance(keystoreType);
        this.keyStore.load(fileInputStream, keystorePassword.toCharArray());
    }

    public Key getKey(String key, String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        return keyStore.getKey(key, password.toCharArray());
    }

    public Certificate getCertificate(String alias) throws KeyStoreException {
        return keyStore.getCertificate(alias);
    }
}
