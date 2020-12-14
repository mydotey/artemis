package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class DeleteRouteRuleGroupsRequest extends OperationContext {
    private List<Long> routeRuleGroupIds;

    public List<Long> getRouteRuleGroupIds() {
        return routeRuleGroupIds;
    }

    public void setRouteRuleGroupIds(List<Long> routeRuleGroupIds) {
        this.routeRuleGroupIds = routeRuleGroupIds;
    }

    @Override
    public String toString() {
        return "DeleteRouteRuleGroupsRequest{" +
                "routeRuleGroupIds=" + routeRuleGroupIds +
                "} " + super.toString();
    }
}
