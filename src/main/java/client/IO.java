package client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IO {
    public void error(String message) {
        printMessage(message);
    }

    /**
     * Error message
     * @param message Error message
     * @throws System.exit(-1) Exit with error
     */
    public void errorAndExit(String message) {
        printMessage(message);

        // Exit with error
        System.exit(-1);
    }

    /**
     * Parse arguments
     * @param args The arguments to parse
     * @return A map with the arguments
     */
    public Map<String, List<String>> parseArguments(String[] args) {
        final Map<String, List<String>> params = new HashMap<>();

        List<String> options = null;
        for (final String a : args) {
            if (a.charAt(0) == '-') {
                if (a.length() < 2)
                    this.errorAndExit("Error at argument " + a);

                options = new ArrayList<>();
                params.put(a.substring(1), options);
            } else if (options != null) {
                options.add(a);
            } else
                this.errorAndExit("Illegal parameter usage");
        }

        return params;
    }

    public void printMessage(String message) {
        System.out.println(message);
    }
}
