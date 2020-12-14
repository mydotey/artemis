package org.mydotey.artemis.management.log;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;
import org.mydotey.artemis.management.group.log.GroupOperationLog;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetGroupOperationLogsResponse implements HasResponseStatus {
    private ResponseStatus responseStatus;
    private List<GroupOperationLog> logs;

    @Override
    public ResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<GroupOperationLog> getLogs() {
        return logs;
    }

    public void setLogs(List<GroupOperationLog> logs) {
        this.logs = logs;
    }
}
