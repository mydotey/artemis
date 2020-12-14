package org.mydotey.artemis.management.server;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetAllServerOperationsResponse implements HasResponseStatus {

    private List<ServerOperations> allServerOperations;
    private ResponseStatus responseStatus;

    @Override
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<ServerOperations> getAllServerOperations() {
        return allServerOperations;
    }

    public void setAllServerOperations(List<ServerOperations> allServerOperations) {
        this.allServerOperations = allServerOperations;
    }

}
