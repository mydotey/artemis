package org.mydotey.artemis.util;

import java.util.List;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.ServiceGroup;
import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.java.StringExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public final class ServiceGroups {

    public static final String DEFAULT_GROUP_ID = "default";
    public static final int MAX_WEIGHT_VALUE = 10000;
    public static final int MIN_WEIGHT_VALUE = 0;
    public static final int DEFAULT_WEIGHT_VALUE = 5;

    private ServiceGroups() {

    }

    public static int fixWeight(Integer weight) {
        if (weight == null || weight < MIN_WEIGHT_VALUE) {
            return DEFAULT_WEIGHT_VALUE;
        }

        if (weight > MAX_WEIGHT_VALUE) {
            return MAX_WEIGHT_VALUE;
        }

        return weight;
    }

    public static boolean isDefaultGroupId(String groupId) {
        if (StringExtension.isBlank(groupId))
            return true;
        return DEFAULT_GROUP_ID.equalsIgnoreCase(groupId.trim());
    }

    public static boolean isGroupCanaryInstance(String groupKey, Instance instance) {
        if (StringExtension.isBlank(groupKey) || instance == null) {
            return false;
        }
        return RouteRules.DEFAULT_GROUP_KEY.equalsIgnoreCase(groupKey.trim()) ||
            ServiceGroupKeys.of(instance).getGroupKey().startsWith(groupKey);
    }

    public static boolean isLocalZone(ServiceGroup serviceGroup) {
        ValueCheckers.notNull(serviceGroup, "serviceGroup");
        if (StringExtension.isBlank(serviceGroup.getGroupKey())) {
            return true;
        }

        List<String> keys = ServiceGroupKeys.toGroupIdList(serviceGroup.getGroupKey());
        if (keys.size() >= 3) {
            String regionId = keys.get(1);
            String zoneId = keys.get(2);
            return SameRegionChecker.DEFAULT.isSameRegion(regionId) && SameZoneChecker.DEFAULT.isSameZone(zoneId);
        }

        return true;
    }
}
