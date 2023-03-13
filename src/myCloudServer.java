import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class myCloudServer extends Thread {
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
         
		while(true) {
			try {
                System.out.println("esperando ligacao");
				Socket inSoc = sSoc.accept();
				myCloudServer newServerThread = new myCloudServer(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
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

        System.out.println("thread");
 
    }
}
