package pt.fcul.sinf.si003.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class myCloudServer extends Thread {

    private final int CHUNK_SIZE = 1024;

    public static void main(String[] args) {


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

        ServerSocket sSoc = null;
        try {
            sSoc = new ServerSocket(Integer.valueOf(port));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        while (true) {
            try {
                System.out.println("esperando ligacao");
                Socket inSoc = sSoc.accept();
                myCloudServer newServerThread = new myCloudServer(inSoc);
                newServerThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //sSoc.close();
    }

    private Socket socket = null;

    myCloudServer(Socket inSoc) {
        socket = inSoc;
        System.out.println("socket");
    }

    public void run() {
        try {
            receiveCommand();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		/*
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
		*/

    }


    // mehod that receives clientSocket.sendString from the client

    public void receiveCommand() throws IOException {

        // open the input stream from the client

        // read the command from the client
        // ObjectInputStream stream = new ObjectInputStream(socket.getInputStream());
        System.out.println("receiveCommand");
        InputStream stream = socket.getInputStream();
        System.out.println("read UTF");
        String message = receiveString();
        System.out.println("message: " + message);


        // separate the message in command and file name
        String command = message.split(" ")[0];
        String fileName = message.split(" ")[1];


        switch (command) {
            case "exists":
                boolean answer = exists(fileName);

                // open output stream to the client
                // send "the file does not exist" to the client

                System.out.println("answer: " + answer);
                sendBool(answer);

                break;
            case "upload":
                upload(stream, fileName);
                break;

        }


    }


    private boolean exists(String fileName) {

        File tmp = new File(fileName);
        boolean exists = tmp.exists();

        return exists;
    }

    private void upload(InputStream stream, String fileName) throws IOException {
        // open the output stream to the file
        System.out.println("upload " + fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        receiveStream(fos);
        fos.close();
    }

    // same as ClientSocket

    /**
     * Send an int to the socket
     *
     * @param value Int to send
     */
    public void sendInt(int value) {
        try {
            socket.getOutputStream().write(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBool(boolean bool) {
        this.sendInt(bool ? 1 : 0);
    }

    /**
     * Receive a byte array from the socket
     *
     * @param bytes  Byte array to receive the stream
     * @param length Length of the byte array
     * @return Number of bytes received
     */
    public int receiveBytes(byte[] bytes, int length) {
        try {
            return socket.getInputStream().read(bytes, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int receiveInt() {
        try {
            return socket.getInputStream().read();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean receiveBool() {
        return this.receiveInt() == 1;
    }

    /**
     * Receive a stream from the socket
     *
     * @param outputBuffer Buffer to receive the stream
     */
    public void receiveStream(OutputStream outputBuffer) {
        try {
            // Length of the file
            int length = this.receiveInt();

            // Buffer of chunkSize bytes
            byte[] buffer = new byte[CHUNK_SIZE];

            // Receive the file in chunks of chunkSize bytes, until the end
            do {
                // Read chunkSize bytes from the socket
                int bytesRead = this.receiveBytes(buffer, CHUNK_SIZE);
                // EOF
                if (bytesRead == -1) {
                    break;
                }

                // Send the chunk and update the length to be sent
                outputBuffer.write(buffer, 0, bytesRead);
                length -= bytesRead;
            } while (length > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveString() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        this.receiveStream(byteArrayOutputStream);
        return byteArrayOutputStream.toString();//StandardCharsets.UTF_8);
    }


}
