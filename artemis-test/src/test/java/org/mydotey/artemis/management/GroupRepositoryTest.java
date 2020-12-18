package org.mydotey.artemis.management;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mydotey.artemis.RouteRule;
import org.mydotey.artemis.ServiceGroup;
import org.mydotey.artemis.management.common.OperationContext;
import org.mydotey.artemis.management.group.Group;
import org.mydotey.artemis.management.group.RouteRuleGroup;
import org.mydotey.artemis.management.group.ServiceRouteRule;
import org.mydotey.artemis.management.group.model.GroupModel;
import org.mydotey.artemis.management.group.model.RouteRuleGroupModel;
import org.mydotey.artemis.management.group.model.RouteRuleModel;
import org.mydotey.artemis.test.ArtemisTest;
import org.mydotey.artemis.util.ServiceGroups;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GroupRepositoryTest extends ArtemisTest {
    private static final String regionId = "lab";
    private static GroupRepository groupRepository = GroupRepository.getInstance();
    private static List<String> serviceIds = Lists.newArrayList("GroupRepositoryTest.service1",
        "GroupRepositoryTest.service2");
    private static List<String> zones = Lists.newArrayList("zone1", "zone2", "zone3");
    private static List<String> routeIds = Lists.newArrayList("routeId1", "routeId2", "routeId3", "routeId4");
    private static final int minWeight = 100;
    private static List<RouteRuleModel> routeRuleModels = Lists.newArrayList();
    private static List<GroupModel> groupModels = Lists.newArrayList();
    private static List<RouteRuleGroupModel> routeRuleGroupModels = Lists.newArrayList();
    private static Map<String, Integer> routeRuleGroupWeights = Maps.newHashMap();
    private static OperationContext operationContext = new OperationContext();

    @Before
    public void setUp() {
        groupRepository.stopRefresh();
        groupRepository.refreshCache();
        operationContext.setOperatorId("repository");
        operationContext.setToken("repository");
        operationContext.setOperation("operation");
        operationContext.setReason("reason");
        AtomicInteger count = new AtomicInteger(0);
        for (String serviceId : serviceIds) {
            for (String routeId : routeIds) {
                RouteRuleModel routeRuleModel = new RouteRuleModel(serviceId, routeId, "description",
                    GroupRepository.RouteRuleStatus.ACTIVE, RouteRule.Strategy.WEIGHTED_ROUND_ROBIN);
                routeRuleModels.add(routeRuleModel);
            }

            for (String zone : zones) {
                for (int i = 0; i < 3; i++) {
                    String groupName = "custom" + count.getAndIncrement();
                    GroupModel groupModel = new GroupModel(serviceId, regionId, zone, groupName, "appId", "description",
                        GroupRepository.GroupStatus.ACTIVE);
                    groupModels.add(groupModel);
                }
            }
        }

        groupRepository.insertRouteRules(operationContext, routeRuleModels);
        groupRepository.insertGroups(operationContext, groupModels);
        initWeight();
    }

    @After
    public void tearDown() {
        RouteRuleModel rrFilter = new RouteRuleModel();
        GroupModel gFilter = new GroupModel();
        RouteRuleGroupModel rrgFilter = new RouteRuleGroupModel();
        for (String serviceId : serviceIds) {
            rrFilter.setServiceId(serviceId);
            gFilter.setServiceId(serviceId);
            List<ServiceRouteRule> routeRules = groupRepository.getRouteRules(rrFilter);
            List<Group> groups = groupRepository.getGroups(gFilter);
            for (ServiceRouteRule routeRule : routeRules) {
                rrgFilter.setRouteRuleId(routeRule.getRouteRuleId());
                List<RouteRuleGroup> routeRuleGroups = groupRepository.getRouteRuleGroups(rrgFilter);
                List<Long> routeRuleGroupIds = routeRuleGroups.stream().map(v -> v.getRouteRuleGroupId())
                    .collect(Collectors.toList());
                groupRepository.deleteServiceRouteRuleGroups(operationContext, routeRuleGroupIds);
            }
            List<Long> routeRuleIds = routeRules.stream().map(v -> v.getRouteRuleId())
                .collect(Collectors.toList());
            groupRepository.deleteRouteRules(operationContext, routeRuleIds);
            List<Long> groupIds = groups.stream().map(v -> v.getGroupId())
                .collect(Collectors.toList());
            groupRepository.deleteGroups(operationContext, groupIds);
        }
    }

    private void initWeight() {
        AtomicInteger weight = new AtomicInteger(minWeight);
        for (String serviceId : serviceIds) {
            RouteRuleModel rrFilter = new RouteRuleModel();
            rrFilter.setServiceId(serviceId);
            GroupModel gFilter = new GroupModel();
            gFilter.setServiceId(serviceId);
            List<ServiceRouteRule> routeRules = groupRepository.getRouteRules(rrFilter);
            List<Group> groups = groupRepository.getGroups(gFilter);
            for (ServiceRouteRule routeRule : routeRules) {
                for (Group group : groups) {
                    Integer w = weight.getAndIncrement();
                    routeRuleGroupWeights.put(routeRule.getRouteRuleId() + "-" + group.getGroupId(), w);
                    routeRuleGroupModels
                        .add(new RouteRuleGroupModel(routeRule.getRouteRuleId(), group.getGroupId(), w));
                }
            }
        }
        groupRepository.insertServiceRouteRuleGroups(operationContext, routeRuleGroupModels);
    }

    @Test
    public void testRefreshGroupCache() {
        groupRepository.refreshCache();
        Assert.assertTrue(groupRepository.getAllGroups(regionId).size() > 0);
        Assert.assertTrue(groupRepository.getAllRouteRules(regionId).size() > 0);
        for (String serviceId : serviceIds) {
            List<RouteRule> routeRules = groupRepository.getServiceRouteRules(serviceId, null);
            Assert.assertEquals(routeIds.size(), routeRules.size());
            for (RouteRule routeRule : routeRules) {
                Assert.assertTrue(routeIds.contains(routeRule.getRouteId()));
                Assert.assertEquals(zones.size() * 3, routeRule.getGroups().size());
                for (ServiceGroup group : routeRule.getGroups()) {
                    Assert.assertEquals(ServiceGroups.DEFAULT_WEIGHT_VALUE, group.getWeight().intValue());
                }
            }
        }

        groupRepository.releaseServiceRouteRuleGroups(operationContext, routeRuleGroupModels);
        groupRepository.refreshCache();
        for (String serviceId : serviceIds) {
            List<RouteRule> routeRules = groupRepository.getServiceRouteRules(serviceId, null);
            Assert.assertEquals(routeIds.size(), routeRules.size());
            for (RouteRule routeRule : routeRules) {
                Assert.assertTrue(routeIds.contains(routeRule.getRouteId()));
                Assert.assertEquals(zones.size() * 3, routeRule.getGroups().size());
                for (ServiceGroup group : routeRule.getGroups()) {
                    Assert.assertTrue(group.getWeight().intValue() >= minWeight);
                }
            }
        }
    }
}
