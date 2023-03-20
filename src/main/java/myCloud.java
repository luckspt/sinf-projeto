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
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class myCloud {
    // method to split the command by spaces and return an array with the command

    // method to ask for a new command and receive it
    // public static String[] newCommand() {
    //     System.out.print("Insert command: ");
    //     try (Scanner read = new Scanner(System.in)) {
    //         String command = read.nextLine();
    //         // return command array with the command split by spaces
    //         String [] commandArray = command.split(" ");
    //         return commandArray;
            
    //     }
    // }
    
    // error method to print error messages and call newCommand()
    public static void error(String msg) {
        System.out.println("Error: " + msg);
        //newCommand();
        System.exit(-1);
    }


    // method to check if files are valid
    // receives the list of files
    // files have to be a string with the name of the file and the extension
    public static Boolean checkFiles(String [] files) {
        // use a for each to check if each file is valid
        for (String file : files) {
            if (!file.matches("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$"))
                return false;
        }
        return true;
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



 
    private static void cryptCommand(Socket socket, String[] files) throws IOException {
        
        // 1. criar chave hibrida (publica e privada)
        // 2. gravar chave privada como ficheiro.chave_secreta
        // 3. encriptar ficheiro e gravar como ficheiro.cifrado
        // 4. enviar ficheiro.cifrado para servidor

        

        /*
        ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
        
        String user = "user";
        String passwd = "123";

        outStream.writeObject(user);
        outStream.writeObject(passwd);
        
        System.out.println("waiting answer...");
        System.out.println((Boolean)inStream.readObject());
        
        // enviar ficheiro
        FileInputStream fStream = new FileInputStream("src/src/myClient.java");
        
        byte[] contents = fStream.readAllBytes();
        
        // c) enviar previamente a dimensão do ficheiro.
        outStream.writeInt(contents.length);
        
        // b) usar os métodos write(byte[] buf, int off, int len) e read(byte[] buf, int off, int len)
        outStream.write(contents, 0, contents.length);
        
        fStream.close();
        outStream.close();
        inStream.close();
        */
    }


    public static void main(String[] args) throws Exception {
	
		// call newCommand() and save the command array in a variable
        //String [] commandArray = newCommand();
            
            

        // -a <serverAddress> 
        // identifica o servidor (hostname ou endereço IP e porto; por exemplo 127.0.0.1:23456)


        // myCloud -a 127.0.0.1:23456 -e trab1.pdf aulas.doc
        
        // myCloud -a <serverAddress> -c {<filenames>}+
        // o cliente cifra um ou mais ficheiros e envia-os para o servidor


        // myCloud -a <serverAddress> -s {<filenames>}+
        // o cliente assina um ou mais ficheiros e envia-os para o servidor


        // myCloud -a <serverAddress> -e {<filenames>}+
        // o cliente assina e cifra um ou mais ficheiros e envia-os para o servidor

        // myCloud -a <serverAddress> -g {<filenames>}+
        // o cliente recebe um ou mais ficheiros

        // first receive the command
        
    


        // check if command array has at least 4 arguments

        if(args.length < 4)
            error("numero insuficiente de argumentos");

        if(!args[0].equals("-a"))            
            error("obrigatorio o argumento -a");

        if(!args[1].matches("^localhost:[0-9]{1,5}$") && !args[1].matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{1,5}$"))
            error("endereço do servidor inválido");

        // if third word is a valid string, check if the fourth word is -c or -s or -e or -g
        // call method list files with the number of files to encrypt and the command array andsave the list of files in a string array
        String[] filesArray = Arrays.copyOfRange(args, 3, args.length);
        if(!checkFiles(filesArray))
            error("nome de ficheiro inválido");


        // fazer ligacao ao servidor
        String[] addr = args[1].split(":");
        Socket socket = null;
		try {
            System.out.println("addres: " + addr[0] + " port: " + addr[1]);
			socket = new Socket(addr[0], Integer.valueOf(addr[1]));
		}
		catch (IOException exception) {
			error("nao foi possivel ligar ao servidor");
		}

        try {
            switch(args[2]) {
                case "-c":
                    // call method to encrypt files that receives the list of files
                    System.out.println("-c");
                    // cryptCommand(socket, files);
                    ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                    List<String> fileString = new ArrayList<>();
                    int x = 3;
    				while(x < args.length) {
	    		        
                        
	    		        cifraHibrida(args[x]);
					    	
				       
				    	
					    
					    fileString.add(args[x] + ".cif");
					    fileString.add(args[x] + ".key");
					    
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
                    break;
                case "-s":
                    // call method to sign files that receives the list of files
                    System.out.println("-s");
                    break;
                case "-e":
                // call method to encrypt and sign files that receives the list of files
                    System.out.println("-e");
                    break;
                case "-g":
                    // call method to get files that receives the list of files
                    System.out.println("-g");
                    break;
                default:
                    error("opção inválida");
            }
        }
        catch (IOException exception) {
            error("erro ao comunicar com o servidor");
        }
        try {
            socket.close();
        }
        catch (IOException exception) {
            error("erro ao comunicar com o servidor");
        }
    }
}
