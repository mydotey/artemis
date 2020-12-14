package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class InsertRouteRulesRequest extends OperationContext {
    private List<ServiceRouteRule> routeRules;

    public List<ServiceRouteRule> getRouteRules() {
        return routeRules;
    }

    public void setRouteRules(List<ServiceRouteRule> routeRules) {
        this.routeRules = routeRules;
    }

    @Override
    public String toString() {
        return "InsertRouteRulesRequest{" +
                "routeRules=" + routeRules +
                "} " + super.toString();
    }
}
