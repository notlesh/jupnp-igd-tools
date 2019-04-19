/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jupnp_igd_tools;

import jupnp_igd_tools.CLI;
import jupnp_igd_tools.igd.NatUpnpManager;
import jupnp_igd_tools.igd.ExternalIpQuery;

import org.jupnp.model.meta.Service;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static final String APP_NAME = "igd-tool";

    public static final int RETURN_PARSE_ERROR = 1;
    public static final int RETURN_UNMATCHED_CLI_ARGS = 2;
    public static final int RETURN_UPNP_QUERY_ERROR = 3;

    private static boolean appRunning = false;

    public static void main(String[] args) {
        appRunning = true;

        CLI cli = new CLI();
        CommandLine cl = null;

        try {
            cl = cli.parse(args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line args: "+ e);
            System.exit(RETURN_PARSE_ERROR);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Signaling app to shut down...");
                appRunning = false;
            }
        });

        if (cl.hasOption("e")) {
            NatUpnpManager upnpManager = new NatUpnpManager();
            upnpManager.start();
            while (appRunning) {
                try { Thread.sleep(1000); } catch (Exception e) { /* don't care */ }
                Service wanIPConnectionService = upnpManager.getWANIPConnectionService();
                if (null != wanIPConnectionService) {
                    log.info("We have WANIPConnection service");
                    CompletableFuture<String> future = upnpManager.queryExternalIPAddress();
                    try {
                        String ipAddress = future.get();
                        System.out.println(ipAddress);
                        System.exit(0);
                    } catch (Exception e) {
                        System.err.println("Error sending query: "+ e);
                        System.exit(RETURN_UPNP_QUERY_ERROR);
                    }
                }
            }
            log.warn("Shutting down.");

        } else if (cl.hasOption("h")) {
            cli.printUsageText(APP_NAME);
        } else {
            cli.printUsageText(APP_NAME);
            System.exit(RETURN_UNMATCHED_CLI_ARGS);
        }

    }
}
