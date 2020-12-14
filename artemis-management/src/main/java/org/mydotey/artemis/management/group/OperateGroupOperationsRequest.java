package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class OperateGroupOperationsRequest extends OperationContext{
    private List<GroupOperations> groupOperationsList;
    private boolean operationComplete;

    public List<GroupOperations> getGroupOperationsList() {
        return groupOperationsList;
    }

    public void setGroupOperationsList(List<GroupOperations> groupOperationsList) {
        this.groupOperationsList = groupOperationsList;
    }

    public boolean isOperationComplete() {
        return operationComplete;
    }

    public void setOperationComplete(boolean operationComplete) {
        this.operationComplete = operationComplete;
    }

    @Override
    public String toString() {
        return "OperateGroupOperationsRequest{" +
                "groupOperationsList=" + groupOperationsList +
                ", operationComplete=" + operationComplete +
                "} " + super.toString();
    }
}
