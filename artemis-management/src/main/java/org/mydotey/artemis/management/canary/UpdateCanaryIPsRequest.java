package org.mydotey.artemis.management.canary;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class UpdateCanaryIPsRequest extends OperationContext {
    private String serviceId;
    private String appId;
    private List<String> canaryIps;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<String> getCanaryIps() {
        return canaryIps;
    }

    public void setCanaryIps(List<String> canaryIps) {
        this.canaryIps = canaryIps;
    }
}
