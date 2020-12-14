package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetGroupInstancesResponse implements HasResponseStatus {
    private List<GroupInstance> groupInstances;
    private ResponseStatus responseStatus;

    public List<GroupInstance> getGroupInstances() {
        return groupInstances;
    }

    public void setGroupInstances(List<GroupInstance> groupInstances) {
        this.groupInstances = groupInstances;
    }

    @Override
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }
}
