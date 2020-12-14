package org.mydotey.artemis.client;

import org.mydotey.artemis.metric.AuditMetricManager;
import org.mydotey.artemis.metric.EventMetricManager;
import org.mydotey.artemis.metric.NullAuditMetricManager;
import org.mydotey.artemis.metric.NullEventMetricManager;
import org.mydotey.scf.facade.StringProperties;

import com.google.common.base.Preconditions;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ArtemisClientManagerConfig {
    private final StringProperties _properties;
    private final EventMetricManager _eventMetricManager;
    private final AuditMetricManager _auditMetricManager;
    private final RegistryClientConfig _registryClientConfig;
    private final DiscoveryClientConfig _discoveryClientConfig;

    public ArtemisClientManagerConfig(final StringProperties properties) {
        this(properties, NullEventMetricManager.INSTANCE, NullAuditMetricManager.INSTANCE);
    }

    public ArtemisClientManagerConfig(final StringProperties properties,
        final EventMetricManager eventMetricManager, final AuditMetricManager valueMetricManager) {
        this(properties, eventMetricManager, valueMetricManager, new RegistryClientConfig(),
            new DiscoveryClientConfig());
    }

    public ArtemisClientManagerConfig(final StringProperties properties,
        final EventMetricManager eventMetricManager, final AuditMetricManager valueMetricManager,
        RegistryClientConfig registryClientConfig) {
        this(properties, eventMetricManager, valueMetricManager, registryClientConfig, new DiscoveryClientConfig());
    }

    public ArtemisClientManagerConfig(final StringProperties properties,
        final EventMetricManager eventMetricManager, final AuditMetricManager valueMetricManager,
        DiscoveryClientConfig discoveryClientConfig) {
        this(properties, eventMetricManager, valueMetricManager, new RegistryClientConfig(),
            new DiscoveryClientConfig());
    }

    public ArtemisClientManagerConfig(final StringProperties properties,
        final EventMetricManager eventMetricManager, final AuditMetricManager valueMetricManager,
        final RegistryClientConfig registryClientConfig, final DiscoveryClientConfig discoveryClientConfig) {
        Preconditions.checkArgument(properties != null, "properties");
        Preconditions.checkArgument(eventMetricManager != null, "event metric manager");
        Preconditions.checkArgument(valueMetricManager != null, "value metric manager");
        Preconditions.checkArgument(registryClientConfig != null, "registry client config");
        Preconditions.checkArgument(discoveryClientConfig != null, "discovery client config");
        _properties = properties;
        _eventMetricManager = eventMetricManager;
        _auditMetricManager = valueMetricManager;
        _registryClientConfig = registryClientConfig;
        _discoveryClientConfig = discoveryClientConfig;
    }

    public StringProperties properties() {
        return _properties;
    }

    public EventMetricManager eventMetricManager() {
        return _eventMetricManager;
    }

    public AuditMetricManager valueMetricManager() {
        return _auditMetricManager;
    }

    public RegistryClientConfig registryClientConfig() {
        return _registryClientConfig;
    }

    public DiscoveryClientConfig discoveryClientConfig() {
        return _discoveryClientConfig;
    }
}
