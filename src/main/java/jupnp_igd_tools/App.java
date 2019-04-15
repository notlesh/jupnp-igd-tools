/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jupnp_igd_tools;

import jupnp_igd_tools.CLI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class App {

    public static final String APP_NAME = "igd-tool";

    public static final int RETURN_PARSE_ERROR = 1;
    public static final int RETURN_UNMATCHED_CLI_ARGS = 2;

    public static void main(String[] args) {
        CLI cli = new CLI();
        CommandLine cl = null;

        try {
            cl = cli.parse(args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line args: "+ e);
            System.exit(RETURN_PARSE_ERROR);
        }

        if (cl.hasOption("e")) {
            System.out.println("TODO: Implement");
        } else if (cl.hasOption("h")) {
            cli.printUsageText(APP_NAME);
        } else {
            cli.printUsageText(APP_NAME);
            System.exit(RETURN_UNMATCHED_CLI_ARGS);
        }

    }
}
