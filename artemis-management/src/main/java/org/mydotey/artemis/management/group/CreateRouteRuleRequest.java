package org.mydotey.artemis.management.group;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;

/**
 * Created by fang_j on 10/07/2016.
 */
public class CreateRouteRuleRequest  extends OperationContext {
    private ServiceRouteRule routeRule;
    private List<GroupWeight> groups;

    public ServiceRouteRule getRouteRule() {
        return routeRule;
    }

    public void setRouteRule(ServiceRouteRule routeRule) {
        this.routeRule = routeRule;
    }

    public List<GroupWeight> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupWeight> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "CreateRouteRuleRequest{" +
                "routeRule=" + routeRule +
                ", groups=" + groups +
                "} " + super.toString();
    }
}
