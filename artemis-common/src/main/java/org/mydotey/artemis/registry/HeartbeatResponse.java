package org.mydotey.artemis.registry;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class HeartbeatResponse implements HasResponseStatus, HasFailedInstances {

    private List<FailedInstance> _failedInstances;

    private ResponseStatus _responseStatus;

    public HeartbeatResponse() {

    }

    public HeartbeatResponse(List<FailedInstance> failedInstances, ResponseStatus responseStatus) {
        _failedInstances = failedInstances;
        _responseStatus = responseStatus;
    }

    @Override
    public ResponseStatus getResponseStatus() {
        return _responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        _responseStatus = responseStatus;
    }

    @Override
    public List<FailedInstance> getFailedInstances() {
        return _failedInstances;
    }

    public void setFailedInstances(List<FailedInstance> failedInstances) {
        _failedInstances = failedInstances;
    }

}
