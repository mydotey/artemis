package org.mydotey.artemis.client.test.utils;

import org.mydotey.artemis.client.ArtemisClientManagerConfig;
import org.mydotey.artemis.client.common.AddressManager;
import org.mydotey.artemis.client.common.ArtemisClientConfig;
import org.mydotey.artemis.client.discovery.ArtemisDiscoveryHttpClient;
import org.mydotey.artemis.config.DeploymentConfig;
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
    public static String ClientId;

    public static final String DOMAIN_ARTEMIS_SERVICE_URL = "{need to replace}";

    static {
        DeploymentConfig.init("SHA", "jqsha", "0", "http", 8080, "artemis");
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
    }

    public static void setDomain(final String domain) {
        MemSource.setPropertyValue(ClientId + ".service.domain.url", domain);
    }

    public static void setSocketTimeout() {
        MemSource.setPropertyValue(ClientId + ".registry.http-client.client.socket-timout", "10000");
        MemSource.setPropertyValue(ClientId + ".discovery.http-client.client.socket-timout", "10000");
    }

    public interface RegistryService {
        public interface Net {
            String serviceKey = "framework.soa.v1.registryservice";
            String serviceCode = "10002";
        }

        public interface Java {
            String serviceKey = "framework.soa4j.registryservice.v1.registryservice";
            String serviceCode = "10586";
        }
    }
}
