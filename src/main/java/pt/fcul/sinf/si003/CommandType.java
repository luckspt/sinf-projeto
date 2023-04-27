package pt.fcul.sinf.si003;

public enum CommandType {
    EXISTS("exists"),
    DELETE("delete"),
    UPLOAD("upload"),
    DOWNLOAD("download"),
    SIGNUP("signup");

    private final String name;

    CommandType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
