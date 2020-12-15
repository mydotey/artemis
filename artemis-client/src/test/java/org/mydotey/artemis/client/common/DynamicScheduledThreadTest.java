package org.mydotey.artemis.client.common;

import org.junit.Assert;
import org.junit.Test;
import org.mydotey.artemis.client.test.utils.ArtemisClientConstants;
import org.mydotey.scf.filter.RangeValueConfig;
import org.mydotey.artemis.util.DynamicScheduledThread;
import org.mydotey.artemis.util.DynamicScheduledThreadConfig;

import com.google.common.util.concurrent.Runnables;

/**
 * Created by fang_j on 10/07/2016.
 */
public class DynamicScheduledThreadTest {
    @Test
    public void testShutdown() throws InterruptedException {
        DynamicScheduledThreadConfig dynamicScheduledThreadConfig = new DynamicScheduledThreadConfig(
            ArtemisClientConstants.Properties,
            new RangeValueConfig<Integer>(20, 0, 200), new RangeValueConfig<Integer>(500, 500, 5 * 1000));
        DynamicScheduledThread t = new DynamicScheduledThread("client", Runnables.doNothing(),
            dynamicScheduledThreadConfig);
        t.setDaemon(true);
        t.start();
        t.shutdown();
        Thread.sleep(500);
        Assert.assertFalse(t.isAlive());
    }
}
