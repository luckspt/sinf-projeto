package pt.fcul.sinf.si003.server;

import java.util.Base64;

public class User {
    private static final String SEPARATOR = ";";
    private String username;
    private byte[] password;
    private byte[] salt;

    public User(String username, String password) {
        this.username = username;
        this.hashPassword(password);
    }

    public User(String username, String password, String salt) {
        this.username = username;

        this.setPassword(Base64.getDecoder().decode(password.getBytes()));
        this.setSalt(Base64.getDecoder().decode(salt.getBytes()));
    }

    public String getUsername() {
        return this.username;
    }

    public byte[] getPassword() {
        return this.password;
    }

    public String getPasswordString() {
        return Base64.getEncoder().encodeToString(this.password);
    }

    public String getSaltString() {
        return Base64.getEncoder().encodeToString(this.salt);
    }

    public void hashPassword(String password) {
        // Generate salt
        byte[] salt = Passwords.getNextSalt();

        // Hash password
        byte[] hashedPassword = Passwords.hash(password.toCharArray(), salt);

        this.setPassword(hashedPassword);
        this.setSalt(salt);
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public String toString() {
        return this.username + SEPARATOR + this.getPasswordString() + SEPARATOR + this.getSaltString();
    }

    public static User fromString(String userString) {
        String[] userArray = userString.split(SEPARATOR);

        return new User(userArray[0], userArray[1], userArray[2]);
    }
}
