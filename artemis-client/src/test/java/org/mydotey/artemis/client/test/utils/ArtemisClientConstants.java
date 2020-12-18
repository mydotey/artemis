package org.mydotey.artemis.client.test.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.client.ArtemisClientManagerConfig;
import org.mydotey.artemis.client.common.AddressManager;
import org.mydotey.artemis.client.common.ArtemisClientConfig;
import org.mydotey.artemis.client.discovery.ArtemisDiscoveryHttpClient;
import org.mydotey.artemis.client.registry.ArtemisRegistryHttpClient;
import org.mydotey.artemis.server.App;
import org.mydotey.java.ThreadExtension;
import org.mydotey.scf.ConfigurationManager;
import org.mydotey.scf.facade.ConfigurationManagers;
import org.mydotey.scf.facade.StringProperties;
import org.mydotey.scf.facade.SimpleConfigurationSources;
import org.mydotey.scf.source.stringproperty.memorymap.MemoryMapConfigurationSource;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ArtemisClientConstants {

    public static String Name = "client";
    public static MemoryMapConfigurationSource MemSource = SimpleConfigurationSources.newMemoryMapSource(
        "artemis.client.test.config.source");
    public static StringProperties Properties;
    public static ArtemisClientManagerConfig ManagerConfig;
    public static ArtemisClientConfig DiscoveryClientConfig;
    public static ArtemisClientConfig RegistryClientConfig;
    public static ArtemisDiscoveryHttpClient DiscoveryHttpClient;
    public static ArtemisRegistryHttpClient RegistryHttpClient;
    public static String ClientId;

    public static final String DOMAIN_ARTEMIS_SERVICE_URL = "http://127.0.0.1:8080";

    static {
        startServer();

        final ConfigurationManager manager = ConfigurationManagers
            .newManager(ConfigurationManagers.newConfigBuilder().setName("test").addSource(1, MemSource).build());
        Properties = new StringProperties(manager);
        ClientId = "artemis.client" + "." + Name.toLowerCase();
        ArtemisClientConstants.setDomain(DOMAIN_ARTEMIS_SERVICE_URL);
        MemSource.setPropertyValue(ClientId + ".websocket-session.reconnect-times.rate-limiter.enabled",
            "false");
        ManagerConfig = new ArtemisClientManagerConfig(Properties);
        DiscoveryClientConfig = new ArtemisClientConfig(ClientId, ManagerConfig,
            AddressManager.getDiscoveryAddressManager(ClientId, ManagerConfig));
        RegistryClientConfig = new ArtemisClientConfig(ClientId, ManagerConfig,
            AddressManager.getRegistryAddressManager(ClientId, ManagerConfig));
        DiscoveryHttpClient = new ArtemisDiscoveryHttpClient(DiscoveryClientConfig);
        RegistryHttpClient = new ArtemisRegistryHttpClient(
            ArtemisClientConstants.RegistryClientConfig);

        generateRegistryData();
    }

    private static void startServer() {
        System.setProperty("host.ip", "127.0.0.1");
        System.setProperty("artemis.management.enabled", "false");
        App.main(new String[0]);
        ThreadExtension.sleep(5);
    }

    private static void generateRegistryData() {
        Instance instance1 = Instances.newInstance(Services.serviceId1);
        Instance instance2 = Instances.newInstance(Services.serviceId2);
        Runnable generate = () -> {
            RegistryHttpClient.register(new HashSet<Instance>(
                Arrays.asList(instance1, instance2)));
        };
        generate.run();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(generate, 5, 5, TimeUnit.SECONDS);
    }

    public static void setDomain(final String domain) {
        MemSource.setPropertyValue(ClientId + ".service.domain.url", domain);
    }

    public static void setSocketTimeout() {
        MemSource.setPropertyValue(ClientId + ".registry.http-client.client.socket-timout", "10000");
        MemSource.setPropertyValue(ClientId + ".discovery.http-client.client.socket-timout", "10000");
    }

    public interface Services {
        String serviceId1 = "service1";
        String serviceId2 = "service2";
    }
}
