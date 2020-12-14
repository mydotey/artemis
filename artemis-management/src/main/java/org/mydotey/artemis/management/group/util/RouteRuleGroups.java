package org.mydotey.artemis.management.group.util;

import com.google.common.collect.Lists;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;
import org.mydotey.artemis.management.group.RouteRuleGroup;
import org.mydotey.artemis.management.group.model.RouteRuleGroupLogModel;
import org.mydotey.artemis.management.group.model.RouteRuleGroupModel;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class RouteRuleGroups {
    public static RouteRuleGroupModel newRouteRuleGroupModel(RouteRuleGroup routeRuleGroup) {
        return new RouteRuleGroupModel(routeRuleGroup.getRouteRuleId(), routeRuleGroup.getGroupId(),
            routeRuleGroup.getUnreleasedWeight());
    }

    public static RouteRuleGroup newRouteRuleGroup(RouteRuleGroupModel model) {
        return new RouteRuleGroup(model.getId(), model.getRouteRuleId(), model.getGroupId(), model.getWeight(),
            model.getUnreleasedWeight());
    }

    public static List<RouteRuleGroupModel> newRouteRuleGroupModels(List<RouteRuleGroup> routeRuleGroups) {
        return Converts.convert(routeRuleGroups, routeRuleGroup -> newRouteRuleGroupModel(routeRuleGroup));
    }

    public static List<RouteRuleGroup> newRouteRuleGroups(List<RouteRuleGroupModel> models) {
        return Converts.convert(models, model -> newRouteRuleGroup(model));
    }

    public static List<RouteRuleGroupLogModel> newRouteRuleGroupLogs(OperationContext operationContext,
        List<RouteRuleGroupModel> models) {
        List<RouteRuleGroupLogModel> logs = Lists.newArrayList();
        if (operationContext == null || CollectionExtension.isEmpty(models)) {
            return logs;
        }
        for (RouteRuleGroupModel model : models) {
            if (model == null) {
                continue;
            }
            logs.add(new RouteRuleGroupLogModel(model, operationContext));
        }
        return logs;
    }

}
