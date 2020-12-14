package org.mydotey.artemis.replication;

import org.mydotey.artemis.ErrorCodes;
import org.mydotey.artemis.taskdispatcher.TaskErrorCode;
import org.mydotey.java.StringExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ReplicationTool {

    public static boolean shouldReplicateToAllPeerNodes(String serviceUrl) {
        return StringExtension.isBlank(serviceUrl);
    }

    public static TaskErrorCode responseErrorCodeToTaskErrorCode(String responseErrorCode) {
        switch (responseErrorCode) {
            case ErrorCodes.BAD_REQUEST:
            case ErrorCodes.NO_PERMISSION:
            case ErrorCodes.INTERNAL_SERVICE_ERROR:
            case ErrorCodes.SERVICE_UNAVAILABLE:
                return TaskErrorCode.PermanentFail;
            case ErrorCodes.RATE_LIMITED:
                return TaskErrorCode.RateLimited;
            case ErrorCodes.UNKNOWN:
                return TaskErrorCode.RerunnableFail;
            default:
                return TaskErrorCode.RerunnableFail;
        }
    }

    protected ReplicationTool() {

    }

}
