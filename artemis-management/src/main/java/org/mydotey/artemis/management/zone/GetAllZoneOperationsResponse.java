package org.mydotey.artemis.management.zone;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetAllZoneOperationsResponse implements HasResponseStatus {

    private List<ZoneOperations> allZoneOperations;
    private ResponseStatus responseStatus;

    @Override
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<ZoneOperations> getAllZoneOperations() {
        return allZoneOperations;
    }

    public void setAllZoneOperations(List<ZoneOperations> allZoneOperations) {
        this.allZoneOperations = allZoneOperations;
    }
}
