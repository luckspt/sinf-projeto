import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.Thread;

public class myCloud {
	public static void main(String[] args) throws Exception {
    	System.out.println(args[0]);
    	System.out.println(args[1]);
    	System.out.println(args.length);
    	comando(args);
        //System.out.println("Hello World!");
    }
	
	public static void comando(String[] command) throws Exception {
    	System.out.println(command.length);
    	for (String string : command) {
			System.out.println(string);
		}
    	
    	if(command[0].equals("-a")) {
    		System.out.println("entrou");
    		if(command[1] != null) {
    			Socket s = new Socket("localhost",5678);
    			System.out.println("entrou");
    			if(command[2].equals("-c")) {
    				List<String> fileString = new ArrayList<>();
    				ObjectOutputStream outStream = new ObjectOutputStream(s.getOutputStream());
    				int x = 3;
    				while(x < command.length) {
	    		        
	    		        cifraHibrida(command[x]);
					    	
				       
				    	
					    
					    fileString.add(command[x] + ".cif");
					    fileString.add(command[x] + ".key");
					    
					    x+=1;
				    	
    				}
    			
    			List<File> files = new ArrayList<>();
    			for (String string : fileString) {
    				File file = new File(string);
					files.add(file);
				}
    			System.out.println(files.size());
    			outStream.writeObject(files.size());
    			FileInputStream fis = null;
    			for (File file : files) {
					outStream.writeObject(file.getName());
					outStream.writeObject(file.length());
					System.out.println(file.getName());
					System.out.println(file.length());
					fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);
					byte[] buf = new byte[1024];
					int bytes;
					while((bytes = bis.read(buf,0,1024)) > 0 ) {
						
						outStream.write(buf,0,bytes);
					}
					bis.close();
				}
    			//Thread.sleep(2000);
    			//fis.close();
    			}
    		}
    	}
	}
	
	private static void cifraHibrida(String ficheiro) throws Exception {
		KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        SecretKey symmetrickey = kg.generateKey();
        
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, symmetrickey);
        
        FileInputStream fis = new FileInputStream(ficheiro);
        FileOutputStream fos = new FileOutputStream(ficheiro + ".cif");
        CipherOutputStream cos = new CipherOutputStream(fos,c);
        
        FileOutputStream kos = null;
        
        byte[] buffer = new byte[1024];
        int i;
        
        while((i = fis.read(buffer)) != -1) {
        	cos.write(buffer, 0, i);
        }
        cos.close();
        fos.close();
        fis.close();
        
		FileInputStream kfile = new FileInputStream("keystore.pedro");  //keystore
	    KeyStore kstore = KeyStore.getInstance("PKCS12");
	    kstore.load(kfile, "123456".toCharArray());           //password
	    
	    String alias = "pedro";
	    Key key1 = kstore.getKey(alias,"123456".toCharArray());
		
		
		if(key1 instanceof PrivateKey) {
	    	
	    	Certificate cert = kstore.getCertificate(alias);  //alias do utilizador
	    	
	        PublicKey publicKey = cert.getPublicKey();
	        
	        new KeyPair(publicKey,(PrivateKey)key1); 
	        
	        Cipher cRSA = Cipher.getInstance("RSA");
	        cRSA.init(Cipher.WRAP_MODE, publicKey);
	        
	        byte[] keyEncoded = symmetrickey.getEncoded();
	        FileOutputStream kos1 = new FileOutputStream(ficheiro + "Symmetric.key");
	        //byte[] keyEncoded = new byte[fis1.available()];
	        kos1.write(keyEncoded);
	        kos1.close();
	    
	    	SecretKey sk = new SecretKeySpec(keyEncoded, "AES");
	    	byte[] wrappedKey = cRSA.wrap(sk);
	    	kos = new FileOutputStream(ficheiro + ".key");
	    	kos.write(wrappedKey);
	    	kos.close();
		}
	}
	
    	
}