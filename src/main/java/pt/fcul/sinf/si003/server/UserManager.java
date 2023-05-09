package pt.fcul.sinf.si003.server;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * User manager
 */
public class UserManager {
    private final File file;
    private final Map<String, ServerUser> cachedUsers = new HashMap<>();

    /**
     *  Create a new UserManager
     * @param fileName The name of the file to save the passwords
     */
    public UserManager(String baseDirectory, String fileName) throws IOException {
        this.file = new File(baseDirectory, fileName);
        if (!this.file.exists())
            this.file.createNewFile();
    }

    /**
     * Checks if a user exists
     * @param username The username to check
     * @return True if the user exists, false otherwise
     */
    public boolean userExists(String username) {
        return this.getUser(username) != null;
    }

    /**
     * Read the file and look for a user if it is not cached
     * @param username The username to look for
     */
    public ServerUser getUser(String username) {
        if (this.cachedUsers.containsKey(username))
            return this.cachedUsers.get(username);

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            // Create streams
            fileReader = new FileReader(this.file);
            bufferedReader = new BufferedReader(fileReader);

            // Read file line by line until EOF
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                ServerUser serverUser = ServerUser.fromString(line);

                if (serverUser.getUsername().equals(username)) {
                    this.cachedUsers.put(username, serverUser);
                    return serverUser;
                }
            }

            // Close streams
            bufferedReader.close();
            fileReader.close();

            return null;
        } catch (IOException e) {
            return null;
        }
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

        // Append to file
        FileWriter fileWriter = new FileWriter(this.file, true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(serverUser.toString());
        bufferedWriter.newLine();
        bufferedWriter.close();
        fileWriter.close();

        // Invalidate cache
        this.cachedUsers.clear();
    }
}