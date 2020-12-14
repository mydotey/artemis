package org.mydotey.artemis.management.group.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import org.mydotey.artemis.management.common.OperationContext;
import org.mydotey.artemis.management.group.ServiceInstance;
import org.mydotey.artemis.management.group.model.ServiceInstanceLogModel;
import org.mydotey.artemis.management.group.model.ServiceInstanceModel;
import org.mydotey.artemis.util.StringUtil;
import org.mydotey.codec.json.JacksonJsonCodec;
import org.mydotey.java.StringExtension;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ServiceInstances {
    @SuppressWarnings("unchecked")
    public static ServiceInstance newServiceInstance(ServiceInstanceModel model) {
        Map<String, String> metadata = Maps.newHashMap();
        if (!StringExtension.isBlank(model.getMetadata())) {
            try {
                metadata = JacksonJsonCodec.DEFAULT.decode(model.getMetadata().getBytes(), metadata.getClass());
            } catch (Throwable ex) {
            }
        }
        return new ServiceInstance(model.getId(), model.getServiceId(), model.getInstanceId(), model.getIp(),
            model.getMachineName(), metadata, model.getPort(),
            model.getProtocol(), model.getRegionId(), model.getZoneId(), model.getHealthCheckUrl(), model.getUrl(),
            model.getDescription(),
            model.getGroupId());
    }

    public static ServiceInstanceModel newServiceInstanceModel(ServiceInstance serviceInstance) {
        String metadata = StringExtension.EMPTY;
        if (serviceInstance.getMetadata() != null) {
            metadata = StringUtil.toJson(serviceInstance.getMetadata());
        }
        return new ServiceInstanceModel(serviceInstance.getServiceId(), serviceInstance.getInstanceId(),
            serviceInstance.getIp(),
            serviceInstance.getMachineName(), metadata, serviceInstance.getPort(), serviceInstance.getProtocol(),
            serviceInstance.getRegionId(),
            serviceInstance.getZoneId(), serviceInstance.getHealthCheckUrl(), serviceInstance.getUrl(),
            serviceInstance.getDescription(),
            serviceInstance.getGroupId());
    }

    public static List<ServiceInstanceModel> newServiceInstanceModels(List<ServiceInstance> groupInstances) {
        return Converts.convert(groupInstances, serviceInstance -> newServiceInstanceModel(serviceInstance));
    }

    public static List<ServiceInstance> newServiceInstances(List<ServiceInstanceModel> models) {
        return Converts.convert(models, model -> newServiceInstance(model));
    }

    public static List<ServiceInstanceLogModel> newServiceInstanceLogModels(OperationContext operationContext,
        List<ServiceInstanceModel> groupInstances) {
        List<ServiceInstanceLogModel> logs = Lists.newArrayList();
        if (operationContext == null || CollectionExtension.isEmpty(groupInstances)) {
            return logs;
        }
        for (ServiceInstanceModel ServiceInstance : groupInstances) {
            if (ServiceInstance == null) {
                continue;
            }
            logs.add(new ServiceInstanceLogModel(operationContext, ServiceInstance));
        }

        return logs;
    }
}
