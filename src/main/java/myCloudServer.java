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
	public static void main(String[] args) throws Exception{
		//Criação do servidor
		System.out.println("servidor: main");
		myCloudServer server = new myCloudServer();
		server.startServer();
		
		
	}

	public void startServer () throws Exception{
		//Inicialização da socket
		ServerSocket sSoc = null;
        int portNumber = 5678;
		try {
			sSoc = new ServerSocket(portNumber);
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
			
		public void run(){
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
						try {
							fos = new FileOutputStream(nomeFile);
							BufferedOutputStream bos = new BufferedOutputStream(fos);
							
					        byte[] buffer = new byte[1024];
					        int bytes;
					        int tempoDim = length.intValue();
					        while (tempoDim > 0) {
					        	if(tempoDim > 1024) {
					        		bytes = inStream.read(buffer,0,1024);
					        	}else {
					        		bytes = inStream.read(buffer,0,tempoDim);
					        	}
					        	bos.write(buffer,0,bytes);
					        	
					           tempoDim -= bytes;
					        }
					        bos.close();
					        //fos.close();
						}catch (IOException e) {
						    // handle error
							System.out.println("Entrou aqui");
						    e.printStackTrace();
						}finally {
						    if (fos != null) {
						        //try {
						        	System.out.println("Entrou aqui 2");
						            //fos.close();
						       // } catch (IOException e) {
						            // handle error
						           // e.printStackTrace();
						        //}
						    }
						}
						
					}
					
				}
				//inStream.close();
				
		       
				System.out.println("Acabou");
				//inStream.close();
				//socket.close();
			
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
		//sSoc.close();
}
