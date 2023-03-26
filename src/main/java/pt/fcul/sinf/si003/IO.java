package pt.fcul.sinf.si003;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The IO abstraction
 */
public class IO {
    /**
     * Prints a error message
     * @param message The error message to print
     */
    public void error(String message) {
        printMessage( Color.RED_BOLD + "ERROR" + Color.RESET, Color.WHITE_UNDERLINED + message + Color.RESET);
    }

    /**
     * Prints a info message
     * @param message The info message to print
     */
    public void info(String message) {
        printMessage( Color.BLUE_BOLD + "INFO" + Color.RESET, message);
    }

    /**
     * Prints a warning message
     * @param message The warning message to print
     */
    public void warning(String message) {
        printMessage( Color.YELLOW_BOLD + "WARNING" + Color.RESET, Color.WHITE_UNDERLINED + message + Color.RESET);
    }

    /**
     * Prints a success message
     * @param message The success message to print
     */
    public void success(String message) {
        printMessage( Color.GREEN_BOLD + "SUCCESS" + Color.RESET, message);
    }

    /**
     * Prints a error message and exits (-1)
     * @param message The error message to print
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

    /**
     * Prints a message
     * @param type The type of the message
     * @param message The message to print
     */
    public void printMessage(String type, String message) {
        System.out.println("[" + type + "] " + message);
    }
}
