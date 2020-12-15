package org.mydotey.artemis.client.common;

import java.util.concurrent.atomic.AtomicReference;

import org.mydotey.artemis.client.ArtemisClientManagerConfig;
import org.mydotey.artemis.config.RestPaths;
import org.mydotey.artemis.config.WebSocketPaths;
import org.mydotey.java.StringExtension;

import com.google.common.base.Preconditions;

/**
 * Created by fang_j on 10/07/2016.
 */
public abstract class AddressManager {
    private final AtomicReference<AddressContext> _addressContext = new AtomicReference<AddressContext>();
    protected final AddressRepository _addressRepository;

    public AddressManager(final ArtemisClientManagerConfig managerConfig, final AddressRepository addressRepository) {
        Preconditions.checkArgument(managerConfig != null, "manager config");
        Preconditions.checkArgument(addressRepository != null, "address repository");
        _addressRepository = addressRepository;
        _addressContext.set(newAddressContext());
    }

    public AddressContext getContext() {
        AddressContext context = _addressContext.get();
        if (!context.isAavailable() || context.isExpired()) {
            context = newAddressContext();
            _addressContext.set(context);
        }
        return context;
    }

    protected abstract AddressContext newAddressContext();

    public static AddressManager getDiscoveryAddressManager(final String clientId,
        final ArtemisClientManagerConfig managerConfig) {
        Preconditions.checkArgument(!StringExtension.isBlank(clientId), "clientId");
        Preconditions.checkArgument(managerConfig != null, "manager config");
        return new AddressManager(managerConfig,
            new AddressRepository(clientId, managerConfig, RestPaths.CLUSTER_UP_DISCOVERY_NODES_FULL_PATH)) {
            @Override
            protected AddressContext newAddressContext() {
                return new AddressContext(clientId, managerConfig, _addressRepository.get(),
                    WebSocketPaths.SERVICE_CHANGE_DESTINATION);
            }
        };
    }

    public static AddressManager getRegistryAddressManager(final String clientId,
        final ArtemisClientManagerConfig managerConfig) {
        Preconditions.checkArgument(!StringExtension.isBlank(clientId), "clientId");
        Preconditions.checkArgument(managerConfig != null, "manager config");
        return new AddressManager(managerConfig,
            new AddressRepository(clientId, managerConfig, RestPaths.CLUSTER_UP_REGISTRY_NODES_FULL_PATH)) {
            @Override
            protected AddressContext newAddressContext() {
                return new AddressContext(clientId, managerConfig, _addressRepository.get(),
                    WebSocketPaths.HEARTBEAT_DESTINATION);
            }
        };
    }
}
