package org.mydotey.artemis.management.log;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;
import org.mydotey.artemis.management.group.log.GroupInstanceLog;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetGroupInstanceLogsResponse  implements HasResponseStatus {
    private ResponseStatus responseStatus;
    private List<GroupInstanceLog> logs;

    @Override
    public ResponseStatus getResponseStatus() {
        return this.responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<GroupInstanceLog> getLogs() {
        return logs;
    }

    public void setLogs(List<GroupInstanceLog> logs) {
        this.logs = logs;
    }
}
