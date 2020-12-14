package org.mydotey.artemis.client.common;

import org.junit.Assert;
import org.junit.Test;
import org.mydotey.artemis.client.test.utils.ArtemisClientConstants;
import org.mydotey.artemis.config.RestPaths;

/**
 * Created by fang_j on 10/07/2016.
 */
public class AddressRepositoryTest {
    private final AddressRepository _addressRepository = new AddressRepository(ArtemisClientConstants.ClientId,
        ArtemisClientConstants.ManagerConfig,
        RestPaths.CLUSTER_UP_DISCOVERY_NODES_FULL_PATH);

    @Test
    public void testRefresh() {
        _addressRepository.refresh();
        Assert.assertNotNull(_addressRepository.get());
    }
}
