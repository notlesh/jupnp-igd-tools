/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jupnp_igd_tools;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

public class CLI {

    protected Options options;

    public CLI() {
        options = buildCommandLine();
    }

    public CommandLine parse(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    public void printUsageText(String name) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null); // this preserves insertion order as opposed to alphanumeric
        formatter.printHelp(
                name, 
                "Interact with UPnP Internet Gateway Devices (IGDs) discovered on the local network.\n\n",
                options,
                "\nVisit https://github.org/notlesh/jupnp-igd-tools for more information.");
    }

    private static Options buildCommandLine() {
        Options options = new Options();

        OptionGroup mainCommandsGroup = new OptionGroup();

        mainCommandsGroup.addOption(Option.builder("h")
                                .longOpt("help")
                                .desc("Print usage help text")
                                .hasArg(false)
                                .build());
        mainCommandsGroup.addOption(Option.builder(null)
                                .longOpt("print-registry")
                                .desc("Print the contents of the registry after 10 seconds of discovery")
                                .hasArg(false)
                                .build());
        mainCommandsGroup.addOption(Option.builder(null)
                                .longOpt("query-external-ip")
                                .desc("Query the IGD for the external IP address")
                                .hasArg(false)
                                .build());
        mainCommandsGroup.addOption(Option.builder(null)
                                .longOpt("map-port")
                                .desc("Request that the IGD map a port")
                                .hasArg(false)
                                .build());

        options.addOptionGroup(mainCommandsGroup);

        // port-mapping options
        // TODO: Commons CLI is lacking in this area: we want all of these options to be required only if 
        //       "--map-port" is present, otherwise they should be ignored / not required
        options.addOption(Option.builder(null)
                                .longOpt("external-port")
                                .argName("external-port")
                                .desc("External port")
                                .hasArg(true)
                                .type(Integer.class)
                                .build());
        options.addOption(Option.builder(null)
                                .longOpt("internal-port")
                                .desc("Internal port")
                                .hasArg(true)
                                .type(Integer.class)
                                .build());
        options.addOption(Option.builder(null)
                                .longOpt("protocol")
                                .desc("Protocol ('tcp' or 'udp')")
                                .hasArg(true)
                                .type(Integer.class)
                                .build());
        options.addOption(Option.builder(null)
                                .longOpt("remote-host")
                                .desc("Remote hoste (IPv4 address)")
                                .hasArg(true)
                                .build());
        options.addOption(Option.builder(null)
                                .longOpt("internal-client")
                                .desc("Internal client (IPv4 address)")
                                .hasArg(true)
                                .build());
        options.addOption(Option.builder(null)
                                .longOpt("lease-duration")
                                .desc("Lease duration, in seconds")
                                .type(Integer.class)
                                .hasArg(true)
                                .build());
        options.addOption(Option.builder(null)
                                .longOpt("description")
                                .desc("Short description of this port mapping")
                                .hasArg(true)
                                .build());

        return options;
    }

}
