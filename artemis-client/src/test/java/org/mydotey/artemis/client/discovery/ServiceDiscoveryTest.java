package org.mydotey.artemis.client.discovery;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.mydotey.artemis.Instance;
import org.mydotey.artemis.InstanceChange;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.client.ServiceChangeEvent;
import org.mydotey.artemis.client.ServiceChangeListener;
import org.mydotey.artemis.client.registry.InstanceRegistry;
import org.mydotey.artemis.client.registry.InstanceRepository;
import org.mydotey.artemis.client.test.utils.ArtemisClientConstants;
import org.mydotey.artemis.client.test.utils.Instances;
import org.mydotey.artemis.client.test.utils.Services;
import org.mydotey.artemis.discovery.DiscoveryConfig;
import org.mydotey.java.ThreadExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ServiceDiscoveryTest {
    @Test
    public void testReload() throws Exception {
        final ServiceRepository serviceRepository = new ServiceRepository(ArtemisClientConstants.DiscoveryClientConfig);
        final List<Service> services = Lists.newArrayList();

        Assert.assertEquals(0, services.size());
        Set<String> serviceKeys = Sets.newHashSet(ArtemisClientConstants.Services.serviceId1,
            ArtemisClientConstants.Services.serviceId2);
        Map<String, ServiceChangeListener> serviceChangeListeners = Maps.newHashMap();
        for (String serviceKey : serviceKeys) {
            DefaultServiceChangeListener listener = new DefaultServiceChangeListener();
            DiscoveryConfig discoveryConfig = new DiscoveryConfig(serviceKey);
            serviceChangeListeners.put(serviceKey, listener);
            serviceRepository.registerServiceChangeListener(discoveryConfig, listener);
            serviceRepository.serviceDiscovery.reload(discoveryConfig);

            ThreadExtension.sleep(1000);
            Assert.assertTrue(listener.getServiceChangeEvents().size() >= 1);
            for (ServiceChangeEvent event : listener.getServiceChangeEvents()) {
                Assert.assertEquals(InstanceChange.ChangeType.RELOAD, event.changeType());
                Assert.assertEquals(serviceKey, event.changedService().getServiceId());
            }
        }
    }

    @Test
    public void testSubscribe() throws Exception {
        final ServiceRepository serviceRepository = new ServiceRepository(ArtemisClientConstants.DiscoveryClientConfig);
        final String serviceId = Services.newServiceId();
        final DiscoveryConfig discoveryConfig = new DiscoveryConfig(serviceId);
        final Set<Instance> instances = Sets.newHashSet(Instances.newInstance(serviceId),
            Instances.newInstance(serviceId));
        final CountDownLatch addCount = new CountDownLatch(instances.size());
        final CountDownLatch deleteCount = new CountDownLatch(instances.size());
        final List<ServiceChangeEvent> serviceChangeEvents = Lists.newArrayList();

        serviceRepository.registerServiceChangeListener(discoveryConfig, new ServiceChangeListener() {
            @Override
            public void onChange(ServiceChangeEvent event) {
                serviceChangeEvents.add(event);
                if (InstanceChange.ChangeType.DELETE.equals(event.changeType())) {
                    deleteCount.countDown();
                }

                if (InstanceChange.ChangeType.NEW.equals(event.changeType())) {
                    addCount.countDown();
                }
            }
        });

        InstanceRepository instanceRepository = new InstanceRepository(
            ArtemisClientConstants.RegistryClientConfig);
        instanceRepository.register(instances);
        new InstanceRegistry(instanceRepository, ArtemisClientConstants.RegistryClientConfig);

        Assert.assertTrue(addCount.await(1, TimeUnit.SECONDS));

        instanceRepository.unregister(instances);
        Assert.assertTrue(deleteCount.await(1, TimeUnit.SECONDS));

        Assert.assertTrue(2 * instances.size() <= serviceChangeEvents.size());
    }
}
