package pt.fcul.sinf.si003;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IO {
    public void error(String message) {
        printMessage( Color.RED_BOLD + "ERROR" + Color.RESET, Color.WHITE_UNDERLINED + message + Color.RESET);
    }

    public void info(String message) {
        printMessage( Color.BLUE_BOLD + "INFO" + Color.RESET, message);
    }

    public void warning(String message) {
        printMessage( Color.YELLOW_BOLD + "WARNING" + Color.RESET, Color.WHITE_UNDERLINED + message + Color.RESET);
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

    public void printMessage(String name, String message) {
        System.out.println("[" + name + "] " + message);
    }

    public void printMessage(String message) {
        System.out.println(message);
    }
}
