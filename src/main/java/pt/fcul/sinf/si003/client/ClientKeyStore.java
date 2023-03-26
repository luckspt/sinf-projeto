package pt.fcul.sinf.si003.client;

import pt.fcul.sinf.si003.IO;

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

    public ClientKeyStore(String baseDir, String alias, String keystorePassword, String aliasKeyPassword) {
        this.alias = alias;
        this.aliasKeyPassword = aliasKeyPassword;
        File file = new File(baseDir, String.format("keystore.%sCloud", alias));
        if (!file.exists())
            new IO().errorAndExit("File " + file.getName() + " does not exist (" + file.getAbsolutePath() + ")");

        KeyStore keyStore = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fileInputStream, keystorePassword.toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            // this never happens
        } finally {
            this.keyStore = keyStore;
        }
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
