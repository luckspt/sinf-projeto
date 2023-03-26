package pt.fcul.sinf.si003.client;

import pt.fcul.sinf.si003.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * The KeyStore abstraction.
 */
public class ClientKeyStore {
    /**
     * The KeyStore.
     */
    private final KeyStore keyStore;
    /**
     * The alias of the key to use.
     */
    private final String alias;
    /**
     * The password of the key to use.
     */
    private final String aliasKeyPassword;

    /**
     * Creates a new instance of ClientKeyStore.
     *
     * @param baseDir          the base directory of the keystore
     * @param alias            the alias of the key to use
     * @param keystorePassword the password of the keystore
     * @param aliasKeyPassword the password of the key to use
     */
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

    /**
     * Gets a key from the keystore.
     *
     * @param key      the alias of the key
     * @param password the password of the key
     * @return the key
     */
    public Key getKey(String key, String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        return keyStore.getKey(key, password.toCharArray());
    }

    /**
     * Gets the key of the alias.
     *
     * @return the key
     */
    public Key getAliasKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        return this.getKey(this.alias, this.aliasKeyPassword);
    }

    /**
     * Gets a certificate from the keystore.
     *
     * @param alias the alias of the certificate
     * @return the certificate
     */
    public Certificate getCertificate(String alias) throws KeyStoreException {
        return keyStore.getCertificate(alias);
    }

    /**
     * Gets the certificate of the alias.
     *
     * @return the certificate
     */
    public Certificate getAliasCertificate() throws KeyStoreException {
        return this.getCertificate(this.alias);
    }
}
