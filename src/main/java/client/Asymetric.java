package client;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.PublicKey;

public class Asymetric {
    public byte[] wrapKey(Key key, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return cipher.wrap(key);
    }
}
