package org.mydotey.artemis.management.canary;

import org.mydotey.artemis.RouteRule;
import org.mydotey.artemis.config.DeploymentConfig;
import org.mydotey.artemis.management.GroupRepository;
import org.mydotey.artemis.management.group.model.GroupModel;
import org.mydotey.artemis.management.group.model.RouteRuleModel;
import org.mydotey.artemis.util.RouteRules;

/**
 * Created by fang_j on 10/07/2016.
 */
public class CanaryServices {
    public static final String CANARY_ZONE_ID = "canary";

    public static RouteRuleModel generateCanaryRouteRule(String serviceId) {
        return new RouteRuleModel(serviceId, RouteRules.CANARY_ROUTE_RULE, null, GroupRepository.RouteRuleStatus.ACTIVE, RouteRule.Strategy.WEIGHTED_ROUND_ROBIN);
    }

    public static GroupModel generateCanaryGroup(String serviceId, String appId) {
        return new GroupModel(serviceId, DeploymentConfig.regionId(), CANARY_ZONE_ID, appId, appId, null, GroupRepository.GroupStatus.ACTIVE);
    }
}
