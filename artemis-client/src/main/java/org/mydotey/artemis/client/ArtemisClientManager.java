package org.mydotey.artemis.client;

import java.util.concurrent.ConcurrentHashMap;

import org.mydotey.artemis.client.discovery.DiscoveryClientImpl;
import org.mydotey.artemis.client.registry.RegistryClientImpl;
import org.mydotey.java.StringExtension;

import com.google.common.base.Preconditions;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ArtemisClientManager {

    private static final ConcurrentHashMap<String, ArtemisClientManager> _managers = new ConcurrentHashMap<String, ArtemisClientManager>();

    private final String _managerId;
    private final ArtemisClientManagerConfig _managerConfig;
    private final String _clientId;
    private DiscoveryClient _discoveryClient;
    private RegistryClient _registryClient;

    private ArtemisClientManager(final String managerId, final ArtemisClientManagerConfig managerConfig) {
        Preconditions.checkArgument(!StringExtension.isBlank(managerId), "managerId");
        Preconditions.checkArgument(managerConfig != null, "manager config");

        _managerId = managerId;
        _managerConfig = managerConfig;
        _clientId = "artemis.client." + managerId;

    }

    public DiscoveryClient getDiscoveryClient() {
        if (_discoveryClient == null) {
            synchronized (this) {
                if (_discoveryClient == null) {
                    _discoveryClient = new DiscoveryClientImpl(_clientId, _managerConfig);
                }
            }
        }

        return _discoveryClient;
    }

    public RegistryClient getRegistryClient() {
        if (_registryClient == null) {
            synchronized (this) {
                if (_registryClient == null) {
                    _registryClient = new RegistryClientImpl(_clientId, _managerConfig);
                }
            }
        }

        return _registryClient;
    }

    public String getManagerId() {
        return _managerId;
    }

    public ArtemisClientManagerConfig getManagerConfig() {
        return _managerConfig;
    }

    public static ArtemisClientManager getManager(final String managerId,
        final ArtemisClientManagerConfig managerConfig) {
        Preconditions.checkArgument(!StringExtension.isBlank(managerId), "managerId");
        Preconditions.checkArgument(managerConfig != null, "manager config");
        return _managers.computeIfAbsent(managerId, k -> new ArtemisClientManager(managerId, managerConfig));
    }
}
