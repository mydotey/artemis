package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class DeleteGroupsInstancesRequest extends OperationContext {
    private List<Long> groupInstanceIds;

    public List<Long> getGroupInstanceIds() {
        return groupInstanceIds;
    }

    public void setGroupInstanceIds(List<Long> groupInstanceIds) {
        this.groupInstanceIds = groupInstanceIds;
    }

    @Override
    public String toString() {
        return "DeleteGroupsInstancesRequest{" +
                "groupInstanceIds=" + groupInstanceIds +
                '}';
    }
}
