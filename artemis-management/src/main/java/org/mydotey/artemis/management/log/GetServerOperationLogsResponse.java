package org.mydotey.artemis.management.log;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetServerOperationLogsResponse implements HasResponseStatus {
    private ResponseStatus responseStatus;
    private List<ServerOperationLog> logs;
    
    @Override
    public ResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<ServerOperationLog> getLogs() {
        return logs;
    }

    public void setLogs(List<ServerOperationLog> logs) {
        this.logs = logs;
    }
}