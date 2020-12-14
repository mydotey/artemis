package org.mydotey.artemis.discovery;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;
import org.mydotey.artemis.Service;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class GetServiceResponse implements HasResponseStatus {

    private ResponseStatus _responseStatus;

    private Service _service;

    public GetServiceResponse() {

    }

    public GetServiceResponse(Service service, ResponseStatus responseStatus) {
        _service = service;
        _responseStatus = responseStatus;
    }

    public Service getService() {
        return _service;
    }

    public void setService(Service service) {
        _service = service;
    }

    @Override
    public ResponseStatus getResponseStatus() {
        return _responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        _responseStatus = responseStatus;
    }

}