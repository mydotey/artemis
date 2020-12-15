package org.mydotey.artemis.client.registry;

import org.junit.Test;
import org.mydotey.artemis.client.test.utils.ArtemisClientConstants;
import org.mydotey.artemis.client.test.utils.Instances;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ArtemisRegistryHttpClientTest {
    private static final ArtemisRegistryHttpClient client = new ArtemisRegistryHttpClient(
        ArtemisClientConstants.RegistryClientConfig);

    @Test
    public void testRegister() throws Exception {
        client.register(Instances.newInstances(3));
    }

    @Test
    public void testUnregister() throws Exception {
        client.unregister(Instances.newInstances(3));
    }
}
