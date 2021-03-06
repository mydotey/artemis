package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetAllGroupOperationsResponse implements HasResponseStatus {
    private ResponseStatus responseStatus;
    private List<GroupOperations> allGroupOperations;

    @Override
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<GroupOperations> getAllGroupOperations() {
        return allGroupOperations;
    }

    public void setAllGroupOperations(List<GroupOperations> allGroupOperations) {
        this.allGroupOperations = allGroupOperations;
    }
}
