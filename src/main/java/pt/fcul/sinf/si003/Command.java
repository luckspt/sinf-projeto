package pt.fcul.sinf.si003;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Command {
    private CommandType name;
    private CommandUser user;
    private List<String> args;

    public Command(CommandType name, CommandUser user, List<String> args) {
        this.name = name;
        this.user = user;
        this.args = new ArrayList(args);
    }

    public Command(CommandType name, CommandUser user, List<String> args, File file) {
        this(name, user, args);
        // TODO file?
    }

    public CommandType getName() {
        return this.name;
    }

    public void addArg(String arg) {
        this.args.add(arg);
    }

    @Override
    public String toString() {
        return this.name + " " +
                this.user.toString() + " " +
                String.join(" ", this.args);
    }

    public static Command fromString(String command) {
        String[] parts = command.split(" ");

        CommandType name = CommandType.valueOf(parts[0]);
        CommandUser user = new CommandUser(parts[1], parts[2]);

        List<String> args = new ArrayList();
        for (int i = 1; i < parts.length; i++)
            args.add(parts[i]);

        return new Command(name, user, args);
    }
}
