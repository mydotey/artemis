package org.mydotey.artemis.client.discovery;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.client.test.utils.ArtemisClientConstants;
import org.mydotey.artemis.client.test.utils.Services;
import org.mydotey.artemis.discovery.DiscoveryConfig;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ArtemisDiscoveryHttpClientTest {
    private final ArtemisDiscoveryHttpClient _client = new ArtemisDiscoveryHttpClient(
        ArtemisClientConstants.DiscoveryClientConfig);

    @Test
    public void testGetService_ShouldReturnEmptyInstances() {
        Assert.assertTrue(CollectionUtils.isEmpty(_client.getService(Services.newDiscoverConfig()).getInstances()));
    }

    @Test
    public void testGetService_ShouldReturnIntances() throws Exception {
        final Service service = _client
            .getService(new DiscoveryConfig(ArtemisClientConstants.Services.serviceId1));
        Assert.assertNotNull(service);
        Assert.assertTrue(service.getInstances().size() > 0);
    }

    @Test
    public void testGetServices() {
        final String serviceId = Services.newServiceId();
        final List<String> serviceIds = Lists.newArrayList(serviceId,
            ArtemisClientConstants.Services.serviceId1);
        final List<DiscoveryConfig> discoveryConfigs = Lists.newArrayList(new DiscoveryConfig(serviceId),
            new DiscoveryConfig(ArtemisClientConstants.Services.serviceId1));
        final List<Service> services = _client.getServices(discoveryConfigs);
        Assert.assertEquals(discoveryConfigs.size(), services.size());
        for (final Service service : services) {
            Assert.assertTrue(serviceIds.contains(service.getServiceId()));
            if (ArtemisClientConstants.Services.serviceId1
                .equals(service.getServiceId())) {
                Assert.assertTrue(service.getInstances().size() > 0);
            } else {
                Assert.assertTrue((service.getInstances() == null) || (service.getInstances().size() == 0));
            }
        }
    }
}
