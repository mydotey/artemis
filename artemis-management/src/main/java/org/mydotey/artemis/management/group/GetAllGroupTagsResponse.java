package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetAllGroupTagsResponse implements HasResponseStatus {
    private ResponseStatus responseStatus;
    private List<GroupTags> allGroupTags;

    @Override
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<GroupTags> getAllGroupTags() {
        return allGroupTags;
    }

    public void setAllGroupTags(List<GroupTags> allGroupTags) {
        this.allGroupTags = allGroupTags;
    }
}
