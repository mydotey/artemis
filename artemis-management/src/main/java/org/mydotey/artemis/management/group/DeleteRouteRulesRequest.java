package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class DeleteRouteRulesRequest extends OperationContext{
    private List<Long> routeRuleIds;

    public List<Long> getRouteRuleIds() {
        return routeRuleIds;
    }

    public void setRouteRuleIds(List<Long> routeRuleIds) {
        this.routeRuleIds = routeRuleIds;
    }

    @Override
    public String toString() {
        return "DeleteRouteRulesRequest{" +
                "routeRuleIds=" + routeRuleIds +
                "} " + super.toString();
    }
}
