package org.mydotey.artemis.util;

import java.util.List;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.Region;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.ServiceGroupKey;
import org.mydotey.artemis.Zone;
import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;
import org.mydotey.java.io.file.FileExtension;

import com.google.common.base.Splitter;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public final class ServiceGroupKeys {

    private ServiceGroupKeys() {

    }

    public static ServiceGroupKey of(Service service) {
        ObjectExtension.requireNonNull(service, "service");

        return of(service.getServiceId());
    }

    public static ServiceGroupKey of(Service service, Region region) {
        ObjectExtension.requireNonNull(service, "service");
        ObjectExtension.requireNonNull(region, "region");

        return of(service.getServiceId(), region.getRegionId());
    }

    public static ServiceGroupKey of(Service service, Zone zone) {
        ObjectExtension.requireNonNull(service, "service");
        ObjectExtension.requireNonNull(zone, "zone");

        return of(service.getServiceId(), zone.getRegionId(), zone.getZoneId());
    }

    public static ServiceGroupKey of(Instance instance) {
        ObjectExtension.requireNonNull(instance, "instance");
        String groupId = instance.getGroupId();
        groupId = ServiceGroups.isDefaultGroupId(groupId) ? ServiceGroups.DEFAULT_GROUP_ID : groupId;
        return of(instance.getServiceId(), instance.getRegionId(), instance.getZoneId(), groupId,
            instance.getInstanceId());
    }

    public static ServiceGroupKey of(String... groupIds) {
        ValueCheckers.notNullOrEmpty(groupIds, "groupIds");

        String groupKey = StringExtension.toLowerCase(FileExtension.concatPathParts(groupIds));
        return new ServiceGroupKey(groupKey);
    }

    public static List<String> toGroupIdList(ServiceGroupKey serviceGroupKey) {
        ObjectExtension.requireNonNull(serviceGroupKey, "serviceGroupKey");
        return toGroupIdList(serviceGroupKey.getGroupKey());
    }

    public static List<String> toGroupIdList(String serviceGroupKey) {
        ObjectExtension.requireNonNull(serviceGroupKey, "serviceGroupKey");
        return Splitter.on('/').omitEmptyStrings().splitToList(serviceGroupKey.toLowerCase());
    }

}
