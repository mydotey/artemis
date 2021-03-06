package org.mydotey.artemis.management;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;
import org.mydotey.artemis.Service;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetServicesResponse implements HasResponseStatus {

    private List<Service> _services;

    private long _version;

    private ResponseStatus _responseStatus;

    public GetServicesResponse() {

    }

    public GetServicesResponse(List<Service> services, long version, ResponseStatus responseStatus) {
        _services = services;
        _version = version;
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

    public long getVersion() {
        return _version;
    }

    public void setVersion(long version) {
        _version = version;
    }

}
