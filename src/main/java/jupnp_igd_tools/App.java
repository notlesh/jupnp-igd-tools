/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jupnp_igd_tools;

import jupnp_igd_tools.CLI;
import jupnp_igd_tools.igd.NatUpnpManager;

import org.jupnp.support.model.PortMapping;
import org.jupnp.support.model.Connection;
import org.jupnp.model.types.UnsignedIntegerFourBytes;
import org.jupnp.model.types.UnsignedIntegerTwoBytes;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.MissingOptionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static final String APP_NAME = "igd-tool";

    public static final int RETURN_PARSE_ERROR = 1;
    public static final int RETURN_UNMATCHED_CLI_ARGS = 2;
    public static final int RETURN_UPNP_QUERY_ERROR = 3;
    public static final int RETURN_UNCAUGHT_EXCEPTION = 4;

    public static void main(String[] args) {
        try {
            doMain(args);
        } catch (Exception e) {
            System.out.println("Uncaught exception in main, terminating: "+ e);
            e.printStackTrace();
            System.exit(RETURN_UNCAUGHT_EXCEPTION);
        }
    }
    public static void doMain(String[] args) {

        CLI cli = new CLI();
        CommandLine cl = null;

        try {
            cl = cli.parse(args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line args: "+ e);
            System.exit(RETURN_PARSE_ERROR);
        }

        if (cl.hasOption("query-external-ip")) {
            NatUpnpManager upnpManager = new NatUpnpManager();
            upnpManager.start();
            CompletableFuture<String> future = upnpManager.queryExternalIPAddress();
            try {
                String ipAddress = future.get();
                System.out.println(ipAddress);
                System.exit(0);
            } catch (Exception e) {
                System.err.println("Error sending query");
                e.printStackTrace();
                System.exit(RETURN_UPNP_QUERY_ERROR);
            }

        } else if (cl.hasOption("query-status-info")) {
            NatUpnpManager upnpManager = new NatUpnpManager();
            upnpManager.start();
            CompletableFuture<Connection.StatusInfo> future = upnpManager.queryStatusInfo();
            try {
                Connection.StatusInfo statusInfo = future.get();
                System.out.println("GetStatusInfo results:");
                System.out.println("  status:         "+ statusInfo.getStatus());
                System.out.println("  uptime seconds: "+ statusInfo.getUptimeSeconds());
                System.out.println("  last error    : "+ statusInfo.getLastError());
                System.exit(0);
            } catch (Exception e) {
                System.err.println("Error sending query");
                e.printStackTrace();
                System.exit(RETURN_UPNP_QUERY_ERROR);
            }


        } else if (cl.hasOption("map-port")) {
            NatUpnpManager upnpManager = new NatUpnpManager();
            upnpManager.start();
            PortMapping portMapping = null;

            try {
                portMapping = buildPortMappingParametersFormCLI(cl);
            } catch (ParseException e) {
                System.err.println("Error parsing port mapping options: "+ e);
                System.exit(RETURN_UNMATCHED_CLI_ARGS);
            }

            CompletableFuture<String> future = upnpManager.requestPortForward(portMapping);
            try {
                String result = future.get();
                System.out.println(result);
                System.exit(0);
            } catch (Exception e) {
                System.err.println("Error sending query");
                e.printStackTrace();
                System.exit(RETURN_UPNP_QUERY_ERROR);
            }

        } else if (cl.hasOption("print-registry")) {
            NatUpnpManager upnpManager = new NatUpnpManager();
            upnpManager.start();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) { }
            upnpManager.printRegistryContents();
        } else if (cl.hasOption("h")) {
            cli.printUsageText(APP_NAME);
        } else {
            cli.printUsageText(APP_NAME);
            System.exit(RETURN_UNMATCHED_CLI_ARGS);
        }

    }

    /**
     * Pull arguments for port mapping out of the command line.
     *
     * @param cl should be a fully-parsed CommandLine object
     * @return a filled out PortMapping object
     * @throws a ParseException if options are missing or arguments are incorrect.
     */
    private static PortMapping buildPortMappingParametersFormCLI(CommandLine cl) throws ParseException {

        // note that we do a lot of error handling that should be redundant here. however, the 
        // Commons CLI lib doesn't support options that are conditionally required (e.g. required
        // only if another option is present) so we do that manually.
        //
        // in addition, the recommended CommandLine.getParsedOptionValue() isn't working -- I suspect
        // that is related to us not providing "short options".

        PortMapping portMapping = new PortMapping();
        portMapping.setEnabled(true);

        // handle all required params
        // --------------------------

        if (! cl.hasOption("external-port")) {
            throw new MissingOptionException("--external-port is required with --map-port");
        } else {
            String value = cl.getOptionValue("external-port");
            portMapping.setExternalPort(new UnsignedIntegerTwoBytes(value));
        }

        if (! cl.hasOption("internal-port")) {
            throw new MissingOptionException("--internal-port is required with --map-port");
        } else {
            String value = cl.getOptionValue("internal-port");
            portMapping.setInternalPort(new UnsignedIntegerTwoBytes(value));
        }
       
        if (! cl.hasOption("protocol")) {
            throw new MissingOptionException("--protocol is required with --map-port");
        }  else {
            String value = cl.getOptionValue("protocol");
            if (value.equals("udp")) {
                portMapping.setProtocol(PortMapping.Protocol.UDP);
            } else if (value.equals("tcp")) {
                portMapping.setProtocol(PortMapping.Protocol.TCP);
            } else {
                throw new ParseException("--protocol must be either 'tcp' or 'udp'");
            }
        }
       
        if (! cl.hasOption("internal-client")) {
            throw new MissingOptionException("--internal-client is required with --map-port");
        } else {
            String value = cl.getOptionValue("internal-client");
            portMapping.setInternalClient(value);
        }
       
        if (! cl.hasOption("lease-duration")) {
            throw new MissingOptionException("--lease-duration is required with --map-port");
        } else {
            String value = cl.getOptionValue("lease-duration");
            portMapping.setLeaseDurationSeconds(new UnsignedIntegerFourBytes(value));
        }

        // handle all optional params
        // --------------------------

        if (cl.hasOption("remote-host")) {
            String value = cl.getOptionValue("remote-host");
            portMapping.setRemoteHost(value);
        }

        if (cl.hasOption("description")) {
            String value = cl.getOptionValue("description");
            portMapping.setDescription(value);
        }

        return portMapping;
    }
}
