package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class DeleteServiceInstancesRequest extends OperationContext {
    private List<Long> serviceInstanceIds;

    public List<Long> getServiceInstanceIds() {
        return serviceInstanceIds;
    }

    public void setServiceInstanceIds(List<Long> serviceInstanceIds) {
        this.serviceInstanceIds = serviceInstanceIds;
    }

    @Override
    public String toString() {
        return "DeleteServiceInstancesRequest{" +
                "serviceInstanceIds=" + serviceInstanceIds +
                "} " + super.toString();
    }
}
