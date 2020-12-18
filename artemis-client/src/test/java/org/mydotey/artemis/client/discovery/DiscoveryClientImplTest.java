package org.mydotey.artemis.client.discovery;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.client.ServiceChangeEvent;
import org.mydotey.artemis.client.test.utils.ArtemisClientConstants;
import org.mydotey.artemis.client.test.utils.Services;
import org.mydotey.artemis.discovery.DiscoveryConfig;

import java.util.List;
import java.util.Map;

/**
 * Created by fang_j on 10/07/2016.
 */
public class DiscoveryClientImplTest {
    private static final DiscoveryClientImpl _discoveryClientImpl = new DiscoveryClientImpl(
        ArtemisClientConstants.ClientId, ArtemisClientConstants.ManagerConfig);

    @Test
    public void testGetService() {
        final String serviceId = ArtemisClientConstants.Services.serviceId1;
        final DiscoveryConfig discoveryConfig = new DiscoveryConfig(serviceId);
        final Service service = _discoveryClientImpl.getService(discoveryConfig);
        Assert.assertEquals(serviceId, service.getServiceId());
        Assert.assertNotNull(service.getInstances());
        Assert.assertTrue(service.getInstances().size() >= 0);
    }

    @Test
    public void registerServiceChangeListener() {
        final Map<String, DefaultServiceChangeListener> registerServices = Maps.newHashMap();
        final String serviceId = Services.newServiceId();
        registerServices.put("framework.soa.v1.registryservice", new DefaultServiceChangeListener());
        registerServices.put("framework.soa.testservice.v2.testservice", new DefaultServiceChangeListener());
        registerServices.put("framework.soa.test.v1.testportal", new DefaultServiceChangeListener());
        registerServices.put(serviceId, new DefaultServiceChangeListener());

        for (final Map.Entry<String, DefaultServiceChangeListener> entry : registerServices.entrySet()) {
            final DiscoveryConfig discoveryConfig = new DiscoveryConfig(entry.getKey());
            _discoveryClientImpl.registerServiceChangeListener(discoveryConfig, entry.getValue());
        }

        for (final Map.Entry<String, DefaultServiceChangeListener> entry : registerServices.entrySet()) {
            final List<ServiceChangeEvent> serviceChangeEvents = entry.getValue().getServiceChangeEvents();
            Assert.assertEquals(0, serviceChangeEvents.size());
        }
    }

}
