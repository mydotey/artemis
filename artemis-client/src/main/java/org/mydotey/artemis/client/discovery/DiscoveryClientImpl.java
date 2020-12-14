package org.mydotey.artemis.client.discovery;

import org.mydotey.artemis.Service;
import org.mydotey.artemis.client.ArtemisClientManagerConfig;
import org.mydotey.artemis.client.DiscoveryClient;
import org.mydotey.artemis.client.ServiceChangeListener;
import org.mydotey.artemis.client.common.AddressManager;
import org.mydotey.artemis.client.common.ArtemisClientConfig;
import org.mydotey.artemis.discovery.DiscoveryConfig;
import org.mydotey.artemis.util.DiscoveryConfigChecker;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;

import com.google.common.base.Preconditions;

/**
 * Created by fang_j on 10/07/2016.
 */
public class DiscoveryClientImpl implements DiscoveryClient {
    private final ServiceRepository serviceRepository;

    public DiscoveryClientImpl(final String clientId, final ArtemisClientManagerConfig managerConfig) {
        Preconditions.checkArgument(!StringExtension.isBlank(clientId), "clientId");
        Preconditions.checkArgument(managerConfig != null, "manager config");
        final ArtemisClientConfig config = new ArtemisClientConfig(clientId, managerConfig,
            AddressManager.getDiscoveryAddressManager(clientId, managerConfig));
        serviceRepository = new ServiceRepository(config);

    }

    @Override
    public Service getService(final DiscoveryConfig discoveryConfig) {
        DiscoveryConfigChecker.DEFAULT.check(discoveryConfig, "discoveryConfig");
        return serviceRepository.getService(discoveryConfig);
    }

    @Override
    public void registerServiceChangeListener(final DiscoveryConfig discoveryConfig,
        final ServiceChangeListener listener) {
        DiscoveryConfigChecker.DEFAULT.check(discoveryConfig, "discoveryConfig");
        ObjectExtension.requireNonNull(listener, "listener");

        serviceRepository.registerServiceChangeListener(discoveryConfig, listener);
    }
}
