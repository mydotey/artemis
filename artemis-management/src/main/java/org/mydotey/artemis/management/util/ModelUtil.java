package org.mydotey.artemis.management.util;

import org.mydotey.artemis.InstanceKey;
import org.mydotey.artemis.ServerKey;
import org.mydotey.artemis.management.dao.InstanceModel;
import org.mydotey.artemis.management.dao.ServerModel;
import org.mydotey.java.StringExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ModelUtil {

    private ModelUtil() {

    }

    public static CheckResult check(InstanceModel instance) {
        if (StringExtension.isBlank(instance.getRegionId()))
            return new CheckResult(false, "regionId is null or empty");

        if (StringExtension.isBlank(instance.getServiceId()))
            return new CheckResult(false, "serviceId is null or empty");

        if (StringExtension.isBlank(instance.getInstanceId()))
            return new CheckResult(false, "instanceId is null or empty");

        if (StringExtension.isBlank(instance.getOperation()))
            return new CheckResult(false, "operation is null or empty");

        if (StringExtension.isBlank(instance.getOperatorId()))
            return new CheckResult(false, "operatorId is null or empty");

        return new CheckResult(true, null);
    }

    public static CheckResult check(ServerModel server) {
        if (StringExtension.isBlank(server.getRegionId()))
            return new CheckResult(false, "regionId is null or empty");

        if (StringExtension.isBlank(server.getServerId()))
            return new CheckResult(false, "serverId is null or empty");

        if (StringExtension.isBlank(server.getOperation()))
            return new CheckResult(false, "operation is null or empty");

        if (StringExtension.isBlank(server.getOperatorId()))
            return new CheckResult(false, "operatorId is null or empty");

        return new CheckResult(true, null);
    }

    public static InstanceModel newInstance(InstanceKey instanceKey, String operation, String operatorId,
        String token) {
        return newInstance(instanceKey.getRegionId(), instanceKey.getServiceId(), instanceKey.getInstanceId(),
            operation, operatorId, token);
    }

    public static InstanceModel newInstance(String regionId, String serviceId, String instanceId, String operation,
        String operatorId, String token) {
        regionId = StringExtension.trim(regionId);
        serviceId = StringExtension.trim(serviceId);
        instanceId = StringExtension.trim(instanceId);
        operation = StringExtension.trim(operation);
        operatorId = StringExtension.trim(operatorId);
        token = StringExtension.trim(token);
        return new InstanceModel(regionId, serviceId, instanceId, operation, operatorId, token);
    }

    public static ServerModel newServer(ServerKey serverKey, String operation, String operatorId, String token) {
        return newServer(serverKey.getRegionId(), serverKey.getServerId(), operation, operatorId, token);
    }

    public static ServerModel newServer(String regionId, String serverId, String operation, String operatorId,
        String token) {
        regionId = StringExtension.trim(regionId);
        serverId = StringExtension.trim(serverId);
        operation = StringExtension.trim(operation);
        operatorId = StringExtension.trim(operatorId);
        token = StringExtension.trim(token);
        return new ServerModel(regionId, serverId, operation, operatorId, token);
    }
}
