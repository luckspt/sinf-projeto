package commands;

import java.util.Scanner;

public class prompt {


    // method to split the command by spaces and return an array with the command

    // method to ask for a new command and receive it
    public static String[] newCommand() {
        System.out.print("Insert command: ");
        try (Scanner read = new Scanner(System.in)) {
            String command = read.nextLine();
            // return command array with the command split by spaces
            String [] commandArray = command.split(" ");
            return commandArray;
            
        }
    }
    
    // error method to print error messages and call newCommand()
    public static void error() {
        System.out.println("Error: command not found");
        newCommand();
    }


    // method to list files that return a list of files
    // receives the number of files to encrypt and the command array
    public static String [] listFiles(int numberOfFiles, String [] commandArray) {
        String [] files = new String[numberOfFiles];
        for (int i = 0; i < numberOfFiles; i++) {
            files[i] = commandArray[i + 4];
        }
        return files;
    }
     
    

    // method to count the number of files to encrypt
    // the number of files is the number of arguments - 4
    public static int countFiles(String [] commandArray) {
        int numberOfFiles = commandArray.length - 4;
        return numberOfFiles;
    }


    // method to check if files are valid
    // receives the list of files
    // files have to be a string with the name of the file and the extension
    public static Boolean checkFiles(String [] files) {
        // use a for each to check if each file is valid
        for (String file : files) {
            if (file.matches("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")) {
                return true;
            } else {
                System.out.println("Error: invalid files");
                newCommand();
            }
        }
        return false;
    }
 



    public static void main(String[] args) {
		
		// call newCommand() and save the command array in a variable
        String [] commandArray = newCommand();
            
            

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

            if (commandArray.length > 4) {
                if (commandArray[0].equals("myCloud")) {

                    
                    // if first word is myCloud, check if the second word is -a
                    if (commandArray[1].equals("-a")) {
                        
                    
                            // if second word is -a, check if the third word is a valid server address like 127.0.0.1:23456
                        if (commandArray[2].matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{1,5}$")) {
                            // if third word is a valid string, check if the fourth word is -c or -s or -e or -g
                            // call method list files with the number of files to encrypt and the command array andsave the list of files in a string array
                            String [] files = listFiles(countFiles(commandArray), commandArray);
                            
                            // call method to check if files are valid
                            checkFiles(files);

                            if (commandArray[3].equals("-c")) {
                                // call method to encrypt files that receives the list of files
                                System.out.println("-c");
                            }
                            else if (commandArray[3].equals("-s")) {
                                // call method to sign files that receives the list of files
                                System.out.println("-s");
                            }
                            else if (commandArray[3].equals("-e")) {
                                // call method to encrypt and sign files that receives the list of files
                                System.out.println("-e");
                            }
                            else if (commandArray[3].equals("-g")) {
                                // call method to get files that receives the list of files
                                System.out.println("-g");
                                // call method to check if files are valid
                                
                            } else {
                                // call error method
                                error();
                            }
                        } else {
                            // call error method
                            error();
                            System.out.println("1");
                        }
                    } else {
                        // call error method
                        error();
                        System.out.println("2");
                    }     
                    
                } else {
                    // call error method
                    error();
                    System.out.println("3");
                }
            } else {
                // call error method
                error();
                System.out.println("4");
            }
        }
    

}
