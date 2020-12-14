package org.mydotey.artemis.management.group.util;

import com.google.common.collect.Lists;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;
import org.mydotey.artemis.management.group.ServiceRouteRule;
import org.mydotey.artemis.management.group.model.RouteRuleLogModel;
import org.mydotey.artemis.management.group.model.RouteRuleModel;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ServiceRouteRules {
    public static ServiceRouteRule newServiceRouteRule(RouteRuleModel model) {
        return new ServiceRouteRule(model.getId(), model.getServiceId(), model.getName(), model.getDescription(),
            model.getStatus(), model.getStrategy());
    }

    public static RouteRuleModel newRouteRuleModel(ServiceRouteRule serviceRouteRule) {
        return new RouteRuleModel(serviceRouteRule.getServiceId(), serviceRouteRule.getName(),
            serviceRouteRule.getDescription(), serviceRouteRule.getStatus(), serviceRouteRule.getStrategy());
    }

    public static List<ServiceRouteRule> newServiceRouteRules(List<RouteRuleModel> models) {
        return Converts.convert(models, model -> newServiceRouteRule(model));
    }

    public static List<RouteRuleModel> newRouteRuleModels(List<ServiceRouteRule> serviceRouteRules) {
        return Converts.convert(serviceRouteRules, serviceRouteRule -> newRouteRuleModel(serviceRouteRule));
    }

    public static List<RouteRuleLogModel> newRouteRuleLogModels(OperationContext operator,
        List<RouteRuleModel> routeRules) {
        List<RouteRuleLogModel> logs = Lists.newArrayList();
        if (operator == null && CollectionExtension.isEmpty(routeRules)) {
            return logs;
        }
        for (RouteRuleModel routeRule : routeRules) {
            if (routeRule == null) {
                continue;
            }
            logs.add(new RouteRuleLogModel(routeRule, operator));
        }
        return logs;
    }
}
