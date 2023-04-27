package pt.fcul.sinf.si003;

public class CommandUser {
    private String username;
    private String password;

    public CommandUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    @Override
    public String toString() {
        return this.getUsername() + " " + this.getPassword();
    }
}
