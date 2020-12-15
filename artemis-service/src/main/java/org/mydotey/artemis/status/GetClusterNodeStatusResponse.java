package org.mydotey.artemis.status;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;
import org.mydotey.artemis.cluster.ServiceNodeStatus;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class GetClusterNodeStatusResponse implements HasResponseStatus {

    private ServiceNodeStatus _nodeStatus;
    private ResponseStatus _responseStatus;

    public GetClusterNodeStatusResponse() {

    }

    public GetClusterNodeStatusResponse(ServiceNodeStatus nodeStatus, ResponseStatus responseStatus) {
        _nodeStatus = nodeStatus;
        _responseStatus = responseStatus;
    }

    @Override
    public ResponseStatus getResponseStatus() {
        return _responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus) {
        _responseStatus = responseStatus;
    }

    public ServiceNodeStatus getNodeStatus() {
        return _nodeStatus;
    }

    public void setNodeStatus(ServiceNodeStatus nodeStatus) {
        _nodeStatus = nodeStatus;
    }

}
