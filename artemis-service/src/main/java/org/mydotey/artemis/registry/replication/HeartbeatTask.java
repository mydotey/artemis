package org.mydotey.artemis.registry.replication;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.artemis.config.RangeValueFilter;
import org.mydotey.artemis.taskdispatcher.TaskErrorCode;
import org.mydotey.scf.Property;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class HeartbeatTask extends RegistryReplicationTask {

    private static Property<String, Boolean> _batchingEnabledPropery = ArtemisConfig.properties()
        .getBooleanProperty("artemis.service.registry.heartbeat.replication.batching-enabled", true);

    private static Property<String, Integer> _taskTtlProperty = ArtemisConfig.properties()
        .getIntProperty("artemis.service.registry.heartbeat.replication.task-ttl", 5 * 1000,
            new RangeValueFilter<>(2000, 10 * 1000));

    public HeartbeatTask(Instance instance) {
        super(instance, System.currentTimeMillis() + _taskTtlProperty.getValue().intValue());
    }

    public HeartbeatTask(Instance instance, String serviceUrl, long expiryTime, TaskErrorCode errorCode) {
        super(instance, serviceUrl, expiryTime, errorCode);
    }

    @Override
    public boolean batchingEnabled() {
        return _batchingEnabledPropery.getValue().booleanValue();
    }

}
