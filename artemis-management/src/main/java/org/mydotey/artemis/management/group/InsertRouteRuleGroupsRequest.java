package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class InsertRouteRuleGroupsRequest extends OperationContext {
    private List<RouteRuleGroup> routeRuleGroups;

    public List<RouteRuleGroup> getRouteRuleGroups() {
        return routeRuleGroups;
    }

    public void setRouteRuleGroups(List<RouteRuleGroup> routeRuleGroups) {
        this.routeRuleGroups = routeRuleGroups;
    }

    @Override
    public String toString() {
        return "InsertRouteRuleGroupsRequest{" +
                "routeRuleGroups=" + routeRuleGroups +
                "} " + super.toString();
    }
}
