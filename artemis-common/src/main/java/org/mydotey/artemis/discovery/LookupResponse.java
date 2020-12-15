package org.mydotey.artemis.discovery;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;
import org.mydotey.artemis.Service;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class LookupResponse implements HasResponseStatus {

    private ResponseStatus _responseStatus;

    private List<Service> _services;

    public LookupResponse() {

    }

    public LookupResponse(List<Service> services, ResponseStatus responseStatus) {
        _services = services;
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

    public List<Service> getServices() {
        return _services;
    }

    public void setServices(List<Service> services) {
        _services = services;
    }

}
