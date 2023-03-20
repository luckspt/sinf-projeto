import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class myCloudServer {

	// port where the server will listen for connections
	// is args[0]
	private String port = null;
	public static void main(String[] args) throws Exception{

		// check if the number of arguments is correct
		if (args.length != 1) {
			System.out.println("Usage: java myCloudServer <port>");
			System.exit(-1);
		}
		// check if the port is valid
		String port = args[0];
		if (!port.matches("^[0-9]+$")) {
			System.out.println("Error: Invalid port");
			System.exit(-1);
		}


		//Criação do servidor
		System.out.println("servidor: main");
		myCloudServer server = new myCloudServer();
		server.startServer();
		
		
	}

	public void startServer () throws Exception{
		//Inicialização da socket
		ServerSocket sSoc = null;
        
		try {
			sSoc = new ServerSocket(Integer.valueOf(port));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
         
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
		    
		}
	}
		
	class ServerThread extends Thread{

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("thread do server para cada cliente");
		}
			
		public void run() {
    			try {
    				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
				FileOutputStream fos = null;
				String nomeFile = null;
				int files = (int) inStream.readObject();
				System.out.println(files);
				for(int i = 0; i < files; i++) {
					System.out.println(i);
					String utfFile = (String) inStream.readObject();
					Long length = (Long) inStream.readObject();
					System.out.println(utfFile.toString());
					System.out.println(length.intValue());
					if(utfFile.contains(".cif")) {
						nomeFile = utfFile + ".cifrado";
					}else if(utfFile.contains(".key")) {
						nomeFile = utfFile + ".chave_secreta";
					}
					File file1 = new File(nomeFile);
				
					if(file1.exists()) {

						System.out.println("Ficheiro já existe");
						int tamanho = inStream.available();
						inStream.skip(tamanho);

					}else{

						fos = new FileOutputStream(nomeFile);
						BufferedOutputStream bos = new BufferedOutputStream(fos);
					
					byte[] buffer = new byte[1024];
					int bytes;
					int tempoDim = length.intValue();
					int size = inStream.available();
					System.out.println(size);
					while (tempoDim > 0) {
						System.out.println("tempoDim:" + Integer.toString(tempoDim));
						if(tempoDim > 1024) {
							bytes = inStream.read(buffer,0,1024);
							System.out.println("bytes1:" + Integer.toString(bytes));
							bos.write(buffer,0,bytes);
						}else {
							bytes = inStream.read(buffer,0,tempoDim);
							System.out.println("bytes2:" + Integer.toString(bytes));
							bos.write(buffer,0,bytes);
						}


					    tempoDim -= bytes;

					}
					    bos.close();
						//fos.close();
					}

				}


				inStream.close();
				System.out.println("Acabou");
				//inStream.close();
				//socket.close();
		
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
	
       		System.out.println("thread");
 
    		}
	}
		//sSoc.close();
}
