package pt.fcul.sinf.si003.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class ClientKeyStore {
    private final KeyStore keyStore;
    private final String alias;
    private final String aliasKeyPassword;

    public ClientKeyStore(String alias, String keystorePassword, String aliasKeyPassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        this.alias = alias;
        this.aliasKeyPassword = aliasKeyPassword;
        File file = IO.openFile(String.format("keystore.%sCloud", alias), true);
        FileInputStream fileInputStream = new FileInputStream(file);
        this.keyStore = KeyStore.getInstance("PKCS12");
        this.keyStore.load(fileInputStream, keystorePassword.toCharArray());
    }

    public Key getKey(String key, String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        return keyStore.getKey(key, password.toCharArray());
    }

    public Key getAliasKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        return this.getKey(this.alias, this.aliasKeyPassword);
    }

    public Certificate getCertificate(String alias) throws KeyStoreException {
        return keyStore.getCertificate(alias);
    }

    public Certificate getAliasCertificate() throws KeyStoreException {
        return this.getCertificate(this.alias);
    }
}
