package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class UpdateGroupTagsRequest extends OperationContext {
    private List<GroupTags> groupTagsList;

    public List<GroupTags> getGroupTagsList() {
        return groupTagsList;
    }

    public void setGroupTagsList(List<GroupTags> groupTagsList) {
        this.groupTagsList = groupTagsList;
    }

    @Override
    public String toString() {
        return "UpdateGroupTagsRequest{" +
            "groupTagsList=" + groupTagsList +
            "} " + super.toString();
    }
}
