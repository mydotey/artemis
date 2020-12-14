package org.mydotey.artemis.management.instance;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetAllInstanceOperationsResponse implements HasResponseStatus {

    private List<InstanceOperations> allInstanceOperations;
    private ResponseStatus responseStatus;

    @Override
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<InstanceOperations> getAllInstanceOperations() {
        return allInstanceOperations;
    }

    public void setAllInstanceOperations(List<InstanceOperations> allInstanceOperations) {
        this.allInstanceOperations = allInstanceOperations;
    }

}
