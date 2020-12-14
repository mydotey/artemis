package org.mydotey.artemis.client.registry;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.client.ArtemisClientManagerConfig;
import org.mydotey.artemis.client.RegistryClient;
import org.mydotey.artemis.client.common.AddressManager;
import org.mydotey.artemis.client.common.ArtemisClientConfig;
import org.mydotey.artemis.client.common.Conditions;
import org.mydotey.java.StringExtension;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Created by fang_j on 10/07/2016.
 */
public class RegistryClientImpl implements RegistryClient {
    protected final InstanceRepository _instanceRepository;
    protected final InstanceRegistry _instanceRegistry;

    public RegistryClientImpl(final String clientId, final ArtemisClientManagerConfig managerConfig) {
        Preconditions.checkArgument(!StringExtension.isBlank(clientId), "clientId");
        Preconditions.checkArgument(managerConfig != null, "manager config");

        final ArtemisClientConfig config = new ArtemisClientConfig(clientId, managerConfig,
            AddressManager.getRegistryAddressManager(clientId, managerConfig));
        _instanceRepository = new InstanceRepository(config);
        _instanceRegistry = new InstanceRegistry(_instanceRepository, config);
    }

    @Override
    public void register(final Instance... instances) {
        Preconditions.checkArgument(Conditions.verifyInstances(instances), "instances");
        _instanceRepository.register(Sets.newHashSet(instances));
    }

    @Override
    public void unregister(final Instance... instances) {
        Preconditions.checkArgument(Conditions.verifyInstances(instances), "instances");
        Conditions.verifyInstances(instances);
        _instanceRepository.unregister(Sets.newHashSet(instances));
    }
}
