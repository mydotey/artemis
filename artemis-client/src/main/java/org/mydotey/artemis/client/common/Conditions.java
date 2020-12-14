package org.mydotey.artemis.client.common;

import java.util.Collection;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.Service;
import org.mydotey.java.StringExtension;
import org.springframework.util.CollectionUtils;

/**
 * Created by fang_j on 10/07/2016.
 */
public class Conditions {
    public static boolean verifyInstance(final Instance instance) {
        return (instance != null)
            && !StringExtension.isBlank(instance.getInstanceId())
            && !StringExtension.isBlank(instance.getServiceId())
            && !StringExtension.isBlank(instance.getUrl());
    }

    public static boolean verifyInstances(final Instance[] instances) {
        if ((instances == null) || (instances.length == 0)) {
            return false;
        }
        for (final Instance instance : instances) {
            if (!verifyInstance(instance)) {
                return false;
            }
        }
        return true;
    }

    public static boolean verifyInstances(final Collection<Instance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return false;
        }
        for (final Instance instance : instances) {
            if (!verifyInstance(instance)) {
                return false;
            }
        }
        return true;
    }

    public static boolean verifyService(final Service service) {
        return (service != null)
            && !StringExtension.isBlank(service.getServiceId());
    }

    public static boolean verifyServices(final Collection<Service> services) {
        if (CollectionUtils.isEmpty(services)) {
            return false;
        }
        for (final Service service : services) {
            if (!verifyService(service)) {
                return false;
            }
        }
        return true;
    }
}
