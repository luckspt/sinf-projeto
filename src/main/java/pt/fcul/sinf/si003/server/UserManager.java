package pt.fcul.sinf.si003.server;

import pt.fcul.sinf.si003.IO;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User manager
 */
public class UserManager {
    private final File file;
    private final File macFile;
    private final Map<String, ServerUser> cachedUsers = new HashMap<>();
    private final String macPassword;

    /**
     *  Create a new UserManager
     * @param fileName The name of the file to save the passwords
     */
    public UserManager(String baseDirectory, String fileName, String macPassword) throws IOException {
        this.macPassword = macPassword;
        this.macFile = new File(baseDirectory, fileName + ".mac");
        this.file = new File(baseDirectory, fileName);

        if (this.macFile.exists()) {
            if (!isMacValid())
                new IO().errorAndExit("FATAL: Invalid MAC, users file has been tampered with or password is not correct.");
        } else {
            // Create or clear the users file
            this.file.createNewFile();
            // Empty the file
            new FileWriter(this.file).close();

            this.macFile.createNewFile();

            // Compute and save MAC
            saveMac(calculateMac());
        }
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

        if (!isMacValid())
            new IO().errorAndExit("FATAL: Invalid MAC, users file has been tampered with.");

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
        if (!isMacValid())
            new IO().errorAndExit("FATAL: Invalid MAC, users file has been tampered with.");

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

        // Compute and save MAC
        saveMac(calculateMac());
    }

    private boolean isMacValid() {
        if (!this.file.exists() && this.macFile.exists()) return false;

        byte[] expectedMac = readMac();
        if (expectedMac == null) return false;

        byte[] actualMac = calculateMac();
        return Arrays.equals(expectedMac, actualMac);
    }

    private byte[] calculateMac() {
        // read file and compute MAC
        FileInputStream fileInputStream = null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(this.macPassword.getBytes(), "HmacSHA512");
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(keySpec);

            fileInputStream = new FileInputStream(this.file);
            byte[] buffer = new byte[1024];
            int n = 0;
            while ((n = fileInputStream.read(buffer)) != -1) {
                mac.update(buffer, 0, n);
            }

            // get the computed MAC
            return mac.doFinal();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            return null;
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (Exception ignored) { }
        }
    }

    private void saveMac(byte[] mac) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(this.macFile);
            fileOutputStream.write(mac);
            fileOutputStream.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Read the MAC expected value from macFile
     */
    private byte[] readMac() {
        try {
            FileInputStream fileInputStream = new FileInputStream(this.macFile);
            byte[] mac = new byte[(int) this.macFile.length()];

            fileInputStream.read(mac);
            fileInputStream.close();

            return mac;
        } catch (IOException e) {
            return null;
        }
    }
}