package org.mydotey.artemis.registry.replication;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.InstanceKey;
import org.mydotey.artemis.replication.AbstractReplicationTask;
import org.mydotey.artemis.taskdispatcher.TaskErrorCode;
import org.mydotey.java.ObjectExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public abstract class RegistryReplicationTask extends AbstractReplicationTask {

    private String _taskId;
    private Instance _instance;

    public RegistryReplicationTask(Instance instance, long expiryTime) {
        this(instance, null, expiryTime, null);
    }

    public RegistryReplicationTask(Instance instance, String serviceUrl, long expiryTime, TaskErrorCode errorCode) {
        super(serviceUrl, expiryTime, errorCode);
        ObjectExtension.requireNonNull(instance, "instance");
        _instance = instance;
        _taskId = getClass().getSimpleName() + ":" + InstanceKey.of(instance) + ":" + serviceUrl;
    }

    public Instance instance() {
        return _instance;
    }

    @Override
    public String taskId() {
        return _taskId;
    }

}
