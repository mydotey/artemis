package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetAllRouteRulesResponse implements HasResponseStatus {
    private ResponseStatus responseStatus;
    private List<ServiceRouteRule> routeRules;

    @Override
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    public List<ServiceRouteRule> getRouteRules() {
        return routeRules;
    }

    public void setRouteRules(List<ServiceRouteRule> routeRules) {
        this.routeRules = routeRules;
    }
}
