package pt.fcul.sinf.si003;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IO {
    private final String name;
    public IO (String name) {
        this.name = name;
    }

    public void error(String message) {
        printMessage( Color.RED_BOLD + this.name + " ERROR" + Color.RESET, Color.WHITE_UNDERLINED + message + Color.RESET);
    }

    /**
     * Error message
     *
     * @param message Error message
     */
    public void errorAndExit(String message) {
        error(message);

        // Exit with error
        System.exit(-1);
    }

    /**
     * Parse arguments
     *
     * @param args The arguments to parse
     * @return A map with the arguments
     */
    public Map<String, List<String>> parseArguments(String[] args) {
        final Map<String, List<String>> params = new HashMap<>();

        List<String> options = null;
        for (final String a : args) {
            if (a.charAt(0) == '-') {
                if (a.length() < 2)
                    errorAndExit("Error at argument " + a);

                options = new ArrayList<>();
                params.put(a.substring(1), options);
            } else if (options != null) {
                options.add(a);
            } else
                errorAndExit("Illegal parameter usage");
        }

        return params;
    }

    public void printMessage(String message) {
        System.out.println("[" + this.name + "] " + message);
    }

    public void printMessage(String name, String message) {
        System.out.println("[" + name + "] " + message);
    }

    public File openFile(String baseDir, String path, boolean errorIfNotExists) {
        File file = new File(baseDir, path);
        if (errorIfNotExists && !file.exists())
            errorAndExit("File " + path + " does not exist (" + file.getAbsolutePath() + ")");

        return file;
    }
}
