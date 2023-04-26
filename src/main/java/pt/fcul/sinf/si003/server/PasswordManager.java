package pt.fcul.sinf.si003.server;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Saves, loads and checks salted passwords from a text file
 */
public class PasswordManager {
    private File file;
    private Map<String, User> users;

    /**
     *  Create a new PasswordManager
     * @param fileName The name of the file to save the passwords
     */
    public PasswordManager(String baseDirectory, String fileName) throws IOException {
        this.file = new File(baseDirectory, fileName);
        if (!this.file.exists())
            this.file.createNewFile();

        this.read();
    }

    private void read() throws IOException {
        // Create streams
        FileReader fileReader = new FileReader(this.file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        this.users = new HashMap();

        // Read file line by line until EOF
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            User user = User.fromString(line);

            // Add user to list
            this.users.put(user.getUsername(), user);
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
        for (User user : this.users.values()) {
            bufferedWriter.write(user.toString());
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

    /**
     * Checks if a password is correct
     * @param username The username to check
     * @param password The password to check
     * @return True if the password is correct, false otherwise
     * @requires userExists(username)
     */
    public boolean checkPassword(String username, String password) {
        User user = this.users.get(username);
        return user.getPassword().equals(password);
    }

    /**
     * Set a user
     *
     * @param username The username of the new user
     * @param password The password of the new user
     */
    public void setUser(String username, String password) throws IOException {
        // Create user and add to list
        User user = new User(username, password);
        this.users.put(user.getUsername(), user);

        // Save to file
        this.flush();
    }
}