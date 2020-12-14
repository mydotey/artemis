package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class DeleteGroupsRequest extends OperationContext {
    private List<Long> groupIds;

    public List<Long> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<Long> groupIds) {
        this.groupIds = groupIds;
    }

    @Override
    public String toString() {
        return "DeleteGroupsRequest{" +
                "groupIds=" + groupIds +
                "} " + super.toString();
    }
}
