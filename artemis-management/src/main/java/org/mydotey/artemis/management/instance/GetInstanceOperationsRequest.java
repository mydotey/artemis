package org.mydotey.artemis.management.instance;

import org.mydotey.artemis.InstanceKey;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetInstanceOperationsRequest {

    private InstanceKey instanceKey;

    public InstanceKey getInstanceKey() {
        return instanceKey;
    }

    public void setInstanceKey(InstanceKey instanceKey) {
        this.instanceKey = instanceKey;
    }

}
