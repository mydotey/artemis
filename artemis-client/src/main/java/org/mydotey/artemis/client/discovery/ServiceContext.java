package org.mydotey.artemis.client.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.artemis.client.ServiceChangeEvent;
import org.mydotey.artemis.client.ServiceChangeListener;
import org.mydotey.artemis.discovery.DiscoveryConfig;
import org.mydotey.artemis.util.RouteRules;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;
import org.mydotey.java.collection.CollectionExtension;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ServiceContext {
    private final DiscoveryConfig discoveryConfig;
    private final String serviceId;
    private final Set<ServiceChangeListener> listeners;
    private volatile Service service;

    public ServiceContext(DiscoveryConfig discoveryConfig) {
        ObjectExtension.requireNonNull(discoveryConfig, "discoveryConfig");
        ObjectExtension.requireNonBlank(discoveryConfig.getServiceId(), "discoveryConfig.serviceId");
        this.discoveryConfig = discoveryConfig;
        serviceId = StringExtension.toLowerCase(discoveryConfig.getServiceId());
        listeners = Sets.newConcurrentHashSet();
        service = new Service(this.discoveryConfig.getServiceId());
    }

    public DiscoveryConfig getDiscoveryConfig() {
        return discoveryConfig;
    }

    public synchronized Service newService() {
        Service newService = service.clone();
        newService.setRouteRules(RouteRules.newRouteRules(service));
        return newService;
    }

    public synchronized void setService(Service service) {
        ValueCheckers.notNull(service, "service");
        final String newServiceId = service.getServiceId();
        ValueCheckers.notNullOrWhiteSpace(newServiceId, "serviceId");
        Preconditions.checkArgument(serviceId.equals(newServiceId.toLowerCase()),
            "service's serviceId is not this same as discoveryConfig. expected: " + serviceId + ", actual: "
                + newServiceId);
        this.service = service;
    }

    public synchronized boolean deleteInstance(Instance instance) {
        if (instance == null)
            return false;

        List<Instance> instances = service.getInstances();
        if (instances == null)
            return false;

        return instances.remove(instance);
    }

    public synchronized boolean updateInstance(Instance instance) {
        return addInstance(instance);
    }

    public synchronized boolean addInstance(Instance instance) {
        if (instance == null) {
            return false;
        }
        String instanceServiceId = instance.getServiceId();
        if (instanceServiceId == null || !serviceId.equals(instanceServiceId.toLowerCase())) {
            return false;
        }

        List<Instance> instances = service.getInstances();
        if (instances == null) {
            instances = new ArrayList<>();
        }

        deleteInstance(instance);
        instances.add(instance);
        service.setInstances(instances);
        return true;
    }

    public synchronized boolean isAvailable() {
        Service service = this.service;
        return service != null && !CollectionExtension.isEmpty(service.getInstances());
    }

    public synchronized Set<ServiceChangeListener> getListeners() {
        return listeners;
    }

    public synchronized void addListener(final ServiceChangeListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    public ServiceChangeEvent newServiceChangeEvent(final String changeType) {
        Preconditions.checkArgument(!StringExtension.isBlank(changeType), "changeType");
        final Service service = newService();
        return new ServiceChangeEvent() {
            @Override
            public Service changedService() {
                return service;
            }

            @Override
            public String changeType() {
                return changeType;
            }
        };
    }

    @Override
    public String toString() {
        return serviceId;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }

        if (other.getClass() != this.getClass()) {
            return false;
        }

        return Objects.equal(toString(), other.toString());
    }

}
