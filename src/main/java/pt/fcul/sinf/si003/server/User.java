package pt.fcul.sinf.si003.server;

import java.util.Base64;

public class User {
    private static final String SEPARATOR = ";";
    private final String username;
    private byte[] hashedPassword;
    private byte[] salt;

    public User(String username, String password) {
        this.username = username;
        this.hashPassword(password);
    }

    public User(String username, String hashedPassword, String salt) {
        this.username = username;

        this.setHashedPassword(Base64.getDecoder().decode(hashedPassword.getBytes()));
        this.setSalt(Base64.getDecoder().decode(salt.getBytes()));
    }

    public String getUsername() {
        return this.username;
    }

    public byte[] getHashedPassword() {
        return this.hashedPassword;
    }

    public String getHashedPasswordString() {
        return Base64.getEncoder().encodeToString(this.hashedPassword);
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public String getSaltString() {
        return Base64.getEncoder().encodeToString(this.salt);
    }

    public void hashPassword(String password) {
        // Generate salt
        byte[] salt = Passwords.getNextSalt();

        // Hash password
        byte[] hashedPassword = Passwords.hash(password.toCharArray(), salt);

        this.setHashedPassword(hashedPassword);
        this.setSalt(salt);
    }

    public void setHashedPassword(byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    /**
     * Checks if a password is correct
     * @param password The password to check
     * @return True if the password is correct, false otherwise
     * @requires user != null
     */
    public boolean checkPassword(String password) {
        return Passwords.isExpectedPassword(password.toCharArray(), this.getSalt(), this.getHashedPassword());
    }

    public String toString() {
        return this.username + SEPARATOR + this.getHashedPasswordString() + SEPARATOR + this.getSaltString();
    }

    public static User fromString(String userString) {
        String[] userArray = userString.split(SEPARATOR);

        return new User(userArray[0], userArray[1], userArray[2]);
    }
}
