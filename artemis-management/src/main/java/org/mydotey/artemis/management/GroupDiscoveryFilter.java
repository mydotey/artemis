package org.mydotey.artemis.management;

import java.util.List;
import java.util.Map;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.RouteRule;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.ServiceGroup;
import org.mydotey.artemis.discovery.DiscoveryConfig;
import org.mydotey.artemis.discovery.DiscoveryFilter;
import org.mydotey.artemis.util.RouteRules;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GroupDiscoveryFilter implements DiscoveryFilter {

    private static volatile GroupDiscoveryFilter instance;

    public static GroupDiscoveryFilter getInstance() {
        if (instance == null) {
            synchronized (GroupDiscoveryFilter.class) {
                if (instance == null)
                    instance = new GroupDiscoveryFilter();
            }
        }

        return instance;
    }

    private final GroupRepository groupRepository = GroupRepository.getInstance();

    @Override
    public void filter(Service service, DiscoveryConfig discoveryConfig) {
        if (service == null || discoveryConfig == null) {
            return;
        }
        service.setLogicInstances(groupRepository.getServiceInstances(service.getServiceId()));

        List<RouteRule> routeRules = groupRepository.getServiceRouteRules(service.getServiceId(),
            discoveryConfig.getRegionId());
        if (!CollectionExtension.isEmpty(routeRules)) {
            for (RouteRule routeRule : routeRules) {
                if (routeRule == null) {
                    continue;
                }
                if (RouteRules.isCanaryRouteRule(routeRule)) {
                    Map<String, Instance> groupKey2Instance = RouteRules
                        .generateGroupKey2Instance(service.getInstances());
                    Map<String, Instance> instanceId2Instance = RouteRules
                        .generateInstanceId2Instance(service.getInstances(), service.getLogicInstances());
                    for (ServiceGroup serviceGroup : routeRule.getGroups()) {
                        serviceGroup.setInstances(
                            RouteRules.generateGroupInstances(serviceGroup, instanceId2Instance, groupKey2Instance));
                    }
                    break;
                }
            }
        }
        service.setRouteRules(routeRules);

    }
}
