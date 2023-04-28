package pt.fcul.sinf.si003.server;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * User manager
 */
public class UserManager {
    private File file;
    private Map<String, ServerUser> users;

    /**
     *  Create a new PasswordManager
     * @param fileName The name of the file to save the passwords
     */
    public UserManager(String baseDirectory, String fileName) throws IOException {
        this.file = new File(baseDirectory, fileName);
        if (!this.file.exists())
            this.file.createNewFile();

        this.read();
    }

    private void read() throws IOException {
        // Create streams
        FileReader fileReader = new FileReader(this.file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        this.users = new HashMap<>();

        // Read file line by line until EOF
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            ServerUser serverUser = ServerUser.fromString(line);

            // Add user to list
            this.users.put(serverUser.getUsername(), serverUser);
        }

        // Close streams
        bufferedReader.close();
        fileReader.close();
    }

    private void flush() throws IOException {
        // Create streams
    	FileWriter fileWriter = new FileWriter(this.file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        // Write each user to file line by line
        for (ServerUser serverUser : this.users.values()) {
            bufferedWriter.write(serverUser.toString());
            bufferedWriter.newLine();
        }

        // Close streams
        bufferedWriter.close();
        fileWriter.close();
    }

    /**
     * Checks if a user exists
     * @param username The username to check
     * @return True if the user exists, false otherwise
     */
    public boolean userExists(String username) {
        return this.users.containsKey(username);
    }

    public ServerUser getUser(String username) {
        return this.users.get(username);
    }

    /**
     * Checks if a password is correct
     * @param user The user to check
     * @param password The password to check
     * @return True if the password is correct, false otherwise
     * @requires user != null
     */
    public boolean checkPassword(ServerUser user, String password) {
        return Passwords.isExpectedPassword(password.toCharArray(), user.getSalt(), user.getHashedPassword());
    }

    /**
     * Set a user
     *
     * @param username The username of the new user
     * @param password The password of the new user
     */
    public void setUser(String username, String password) throws IOException {
        // Create user and add to list
        ServerUser serverUser = new ServerUser(username, password);
        this.users.put(serverUser.getUsername(), serverUser);

        // Save to file
        this.flush();
    }
}