package org.mydotey.artemis.registry.replication;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.artemis.taskdispatcher.TaskErrorCode;
import org.mydotey.scf.Property;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class RegisterTask extends RegistryReplicationTask {

    private static Property<String, Boolean> _batchingEnabledPropery = ArtemisConfig.properties()
        .getBooleanProperty("artemis.service.registry.register.replication.batching-enabled", false);

    private static Property<String, Integer> _taskTtlProperty = ArtemisConfig.properties()
        .getIntProperty("artemis.service.registry.register.replication.task-ttl", 5000,
            new RangeValueFilter<>(2000, 30 * 1000));

    public RegisterTask(Instance instance) {
        super(instance, System.currentTimeMillis() + _taskTtlProperty.getValue().intValue());
    }

    public RegisterTask(Instance instance, String serviceUrl, long expiryTime, TaskErrorCode errorCode) {
        super(instance, serviceUrl, expiryTime, errorCode);
    }

    @Override
    public boolean batchingEnabled() {
        return _batchingEnabledPropery.getValue().booleanValue();
    }

}
