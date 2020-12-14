package org.mydotey.artemis.management.log;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;
import org.mydotey.artemis.management.group.log.ServiceInstanceLog;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetServiceInstanceLogsResponse implements HasResponseStatus {
    private ResponseStatus responseStatus;
    private List<ServiceInstanceLog> logs;

    @Override
    public ResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<ServiceInstanceLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ServiceInstanceLog> logs) {
        this.logs = logs;
    }
}
