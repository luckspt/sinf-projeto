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


    public static void main(String[] args) {
	
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
        String[] files = Arrays.copyOfRange(args, 3, args.length);
        if(!checkFiles(files))
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
                    cryptCommand(socket, files);
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
