package org.mydotey.artemis.client.common;

import org.mydotey.artemis.client.ArtemisClientManagerConfig;
import org.mydotey.artemis.client.DiscoveryClientConfig;
import org.mydotey.artemis.client.RegistryClientConfig;
import org.mydotey.caravan.util.metric.AuditMetricManager;
import org.mydotey.caravan.util.metric.EventMetricManager;
import org.mydotey.caravan.util.ratelimiter.RateLimiterManager;
import org.mydotey.caravan.util.ratelimiter.RateLimiterManagerConfig;
import org.mydotey.java.StringExtension;
import org.mydotey.scf.facade.StringProperties;

import com.google.common.base.Preconditions;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ArtemisClientConfig {
    private final String _clientId;
    private final ArtemisClientManagerConfig _managerConfig;
    private final AddressManager _addressManager;
    private final RateLimiterManager _rateLimiterManager;

    public ArtemisClientConfig(final String clientId, final ArtemisClientManagerConfig managerConfig,
        final AddressManager addressManager) {
        Preconditions.checkArgument(!StringExtension.isBlank(clientId), "clientId");
        Preconditions.checkArgument(managerConfig != null, "manager config");
        Preconditions.checkArgument(addressManager != null, "addressManager");
        _clientId = clientId;
        _managerConfig = managerConfig;
        _addressManager = addressManager;
        _rateLimiterManager = new RateLimiterManager(clientId,
            new RateLimiterManagerConfig(managerConfig.properties()));
    }

    public String key(final String suffix) {
        Preconditions.checkArgument(!StringExtension.isBlank(suffix), "suffix");
        return _clientId + "." + suffix;
    }

    public StringProperties properties() {
        return _managerConfig.properties();
    }

    public AddressManager addressManager() {
        return _addressManager;
    }

    public EventMetricManager eventMetricManager() {
        return _managerConfig.eventMetricManager();
    }

    public AuditMetricManager valueMetricManager() {
        return _managerConfig.valueMetricManager();
    }

    public RegistryClientConfig registryClientConfig() {
        return _managerConfig.registryClientConfig();
    }

    public DiscoveryClientConfig discoveryClientConfig() {
        return _managerConfig.discoveryClientConfig();
    }

    public RateLimiterManager getRateLimiterManager() {
        return _rateLimiterManager;
    }
}
