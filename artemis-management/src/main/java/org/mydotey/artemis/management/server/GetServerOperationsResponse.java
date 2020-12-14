package org.mydotey.artemis.management.server;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetServerOperationsResponse implements HasResponseStatus {

    private ServerOperations operations;
    private ResponseStatus responseStatus;

    @Override
    public ResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

    public ServerOperations getOperations() {
        return operations;
    }

    public void setOperations(ServerOperations operations) {
        this.operations = operations;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

}
