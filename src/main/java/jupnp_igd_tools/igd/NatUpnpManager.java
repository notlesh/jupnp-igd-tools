/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jupnp_igd_tools.igd;

import org.jupnp.UpnpService;
import org.jupnp.UpnpServiceImpl;
import org.jupnp.DefaultUpnpServiceConfiguration;
import org.jupnp.support.igd.callback.GetExternalIP;
import org.jupnp.support.igd.callback.PortMappingAdd;
import org.jupnp.support.igd.callback.GetStatusInfo;
import org.jupnp.support.model.PortMapping;
import org.jupnp.support.model.Connection;
import org.jupnp.model.meta.Device;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.Service;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.action.ActionInvocation;
import org.jupnp.model.message.UpnpResponse;
import org.jupnp.model.message.header.STAllHeader;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.jupnp.registry.DefaultRegistryListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class NatUpnpManager {

    private final Logger log = LoggerFactory.getLogger(NatUpnpManager.class);

    public static final String SERVICE_DEFAULT_NAMESPACE = "schemas-upnp-org";
    public static final String SERVICE_TYPE_WAN_IP_CONNECTION = "WANIPConnection";

    boolean started = false;
    UpnpService upnpService = null;
    RegistryListener registryListener = null;

    Map<String, Service> recognizedServices;

    /**
     * Empty constructor. Creates in instance of UpnpServiceImpl.
     */
    public NatUpnpManager() {
        this(new UpnpServiceImpl(new DefaultUpnpServiceConfiguration()));
    }

    /**
     * Constructor
     *
     * @param service is the desired instance of UpnpService.
     */
    public NatUpnpManager(final UpnpService service) {
        upnpService = service;

        // registry listener to observe new devices and look for specific services
        registryListener = new DefaultRegistryListener() {
            @Override
            public void deviceAdded(final Registry registry, final Device device) {
                log.info("Device added: "+ device.getDetails().getFriendlyName());
                // TODO: crawl device and look for lintener
                inspectDeviceRecursive(device, recognizedServices.keySet());
            }
        };

        // prime our recognizedServices map so we can use its key-set later
        recognizedServices = new HashMap<>();
        recognizedServices.put(SERVICE_TYPE_WAN_IP_CONNECTION, null);
    }

    /**
     * Start the manager. Must not be in started state.
     *
     * @throws IllegalStateException if already started.
     */
    public void start() {
        if (started) {
            throw new IllegalStateException("Cannot start already-started service");
        }

        log.info("starting upnp service...");
        upnpService.startup();
        upnpService.getRegistry().addListener(registryListener);

        // TODO: does jupnp do this automatically?
        upnpService.getControlPoint().search(new STAllHeader());

        started = true;
    }

    /**
     * Stop the manager. Must not be in stopped state.
     *
     * @throws IllegalStateException if stopped.
     */
    public void stop() {
        if (! started) {
            throw new IllegalStateException("Cannot stop already-stopped service");
        }
        upnpService.getRegistry().removeListener(registryListener);
        upnpService.shutdown();

        started = false;
    }

    /**
     * Returns the first of the discovered services of the given type, if any.
     *
     * @return the first instance of the given type, or null if none 
     */
    public Service getService(final String type) {
        return recognizedServices.get(type);
    }

    /**
     * Get the discovered WANIPConnection service, if any.
     *
     * @return the WANIPConnection Service if we have found it, or null.
     */
    public Service getWANIPConnectionService() {
        return getService(SERVICE_TYPE_WAN_IP_CONNECTION);
    }

    /**
     * Returns a CompletableFuture that will wait for the given service type to be discovered
     *
     * @return future that will return the desired service once it is discovered, or null
     * if the future is cancelled.
     */
    public CompletableFuture<Service> discoverService(final String serviceType) {

        return CompletableFuture.supplyAsync(() -> {

            // wait until our thread is interrupted (assume future was cancelled)
            // or we discover the service
            while (!Thread.currentThread().isInterrupted()) {
                Service service = getService(serviceType);
                if (null != service) {
                    return service;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // fall back through to "isInterrupted() check"
                    }
                }
            }
            return null;
        });
    }

    /**
     * Sends a UPnP request to the discovered IGD for the external ip address.
     *
     * @return A CompletableFuture that can be used to query the result (or error).
     */
    public CompletableFuture<String> queryExternalIPAddress() {

        CompletableFuture<String> upnpQueryFuture = new CompletableFuture<>();

        return discoverService(SERVICE_TYPE_WAN_IP_CONNECTION)
            .thenCompose(service -> {

                // our query, which will be handled asynchronously by the jupnp library
                GetExternalIP callback = new GetExternalIP(service) {
                    @Override
                    protected void success(final String result) {
                        upnpQueryFuture.complete(result);
                    }

                    @Override
                    public void failure(final ActionInvocation invocation, final UpnpResponse operation, final String msg) {
                        upnpQueryFuture.completeExceptionally(new Exception(msg));
                    }
                };
                upnpService.getControlPoint().execute(callback);

                return upnpQueryFuture;
            });
    }

    /**
     * Sends a UPnP request to the discovered IGD to request status info.
     *
     * @return A CompletableFuture that can be used to query the result (or error).
     */
    public CompletableFuture<Connection.StatusInfo> queryStatusInfo() {

        CompletableFuture<Connection.StatusInfo> upnpQueryFuture = new CompletableFuture<>();

        return discoverService(SERVICE_TYPE_WAN_IP_CONNECTION)
            .thenCompose(service -> {

                GetStatusInfo callback = new GetStatusInfo(service) {
                    @Override
                    public void success(final Connection.StatusInfo statusInfo) {
                        upnpQueryFuture.complete(statusInfo);
                    }

                    @Override
                    public void failure(final ActionInvocation invocation, final UpnpResponse operation, final String msg) {
                        upnpQueryFuture.completeExceptionally(new Exception(msg));
                    }
                };
                upnpService.getControlPoint().execute(callback);

                return upnpQueryFuture;
            });
    }

    /**
     * Sends a UPnP request to the discovered IGD to request a port forward.
     *
     * @return A CompletableFuture that can be used to query the result (or error).
     */
    public CompletableFuture<String> requestPortForward(final PortMapping portMapping) {

        CompletableFuture<String> upnpQueryFuture = new CompletableFuture<>();

        return discoverService(SERVICE_TYPE_WAN_IP_CONNECTION)
            .thenCompose(service -> {

                // our query, which will be handled asynchronously by the jupnp library
                PortMappingAdd callback = new PortMappingAdd(service, portMapping) {
                    @Override
                    public void success(final ActionInvocation invocation) {
                        upnpQueryFuture.complete("TODO");
                    }

                    @Override
                    public void failure(final ActionInvocation invocation, final UpnpResponse operation, final String msg) {
                        upnpQueryFuture.completeExceptionally(new Exception(msg));
                    }
                };
                upnpService.getControlPoint().execute(callback);

                return upnpQueryFuture;
            });
    }

    /**
     * Recursively crawls the given device to look for specific services.
     */
    protected void inspectDeviceRecursive(final Device device, final Set<String> serviceIds) {
        for (Service service : device.getServices()) {
            String serviceType = service.getServiceType().getType();
            if (serviceIds.contains(serviceType)) {
                // TODO: handle case where service is already "recognized" as this could lead to
                // some odd bugs
                recognizedServices.put(serviceType, service);
                log.info("Discovered service "+ serviceType);
            }
        }
        for (Device subDevice : device.getEmbeddedDevices()) {
            inspectDeviceRecursive(subDevice, serviceIds);
        }
    }

    /**
     * Print the devices and services known to the registry in a hierarchical fashion
     */
    public void printRegistryContents() {
        System.out.println("Devices known to registry:");
        for (Device device : upnpService.getRegistry().getDevices()) {
            printDeviceRecursive(device, "");
        }
    }

    /**
     * Recursively print out the devices and services known to the registry
     */
    public void printDeviceRecursive(final Device device, final String indent) {
        String nextIndent = "|    ";
        System.out.println(indent +"├-- device: "+ device.getDetails().getFriendlyName());
        System.out.println(indent + nextIndent +"├-- id:           "+ device.getIdentity());
        System.out.println(indent + nextIndent +"├-- manufacturer: "+ device.getDetails().getManufacturerDetails().getManufacturer());
        System.out.println(indent + nextIndent +"├-- model:        "
                + device.getDetails().getModelDetails().getModelName() + " - "
                + device.getDetails().getModelDetails().getModelNumber() + " - "
                + device.getDetails().getModelDetails().getModelDescription());
        System.out.println(indent + nextIndent +"├-- serial:       "+ device.getDetails().getSerialNumber());
        System.out.println(indent + nextIndent +"├-- uda version:  "+ device.getVersion().getMajor() +"."+ device.getVersion().getMinor());
        System.out.println(indent + nextIndent +"├-- type:         "+ device.getType());
        for (Service service : device.getServices()) {
            System.out.println(indent + nextIndent + "├-- service:");
            System.out.println(indent + nextIndent + nextIndent + "├-- id:   "+ service.getServiceId());
            System.out.println(indent + nextIndent + nextIndent + "├-- type: "+ service.getServiceType());
        }
        for (Device subDevice : device.getEmbeddedDevices()) {
            printDeviceRecursive(subDevice, (indent + nextIndent));
        }
    }
}
