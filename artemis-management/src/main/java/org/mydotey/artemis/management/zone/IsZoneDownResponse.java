package org.mydotey.artemis.management.zone;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class IsZoneDownResponse implements HasResponseStatus {

    private boolean down;
    private ResponseStatus responseStatus;

    public boolean isDown() {
        return down;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    @Override
    public ResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

}
