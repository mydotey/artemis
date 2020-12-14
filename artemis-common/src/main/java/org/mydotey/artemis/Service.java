package org.mydotey.artemis;

import java.util.*;

import org.mydotey.java.StringExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class Service implements Cloneable {

    private String serviceId;
    private Map<String, String> metadata;
    private List<Instance> instances;
    private List<Instance> logicInstances;
    private List<RouteRule> routeRules;

    public Service() {

    }

    public Service(String serviceId) {
        this(serviceId, null, null);
    }

    public Service(String serviceId, List<Instance> instances, Map<String, String> metadata) {
        this.serviceId = serviceId;
        this.metadata = metadata;
        this.instances = instances;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    public List<Instance> getLogicInstances() {
        return logicInstances;
    }

    public void setLogicInstances(List<Instance> logicInstances) {
        this.logicInstances = logicInstances;
    }

    public List<RouteRule> getRouteRules() {
        return routeRules;
    }

    public void setRouteRules(List<RouteRule> routeRules) {
        this.routeRules = routeRules;
    }

    @Override
    public String toString() {
        return serviceId;
    }

    @Override
    public int hashCode() {
        String serviceId = StringExtension.toLowerCase(this.serviceId);
        return serviceId == null ? 0 : serviceId.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;

        if (other.getClass() != this.getClass())
            return false;

        String serviceId = StringExtension.toLowerCase(this.serviceId);
        String otherServiceId = StringExtension.toLowerCase(((Service) other).serviceId);
        return Objects.equals(serviceId, otherServiceId);
    }

    @Override
    public Service clone() {
        Service cloned;
        try {
            cloned = (Service) super.clone();
        } catch (Throwable ex) {
            cloned = new Service(serviceId, instances, metadata);
        }

        Map<String, String> metadata = this.metadata;
        if (metadata != null)
            metadata = new HashMap<>(metadata);
        cloned.setMetadata(metadata);

        List<Instance> instances = this.instances;
        if (instances != null)
            instances = new ArrayList<>(instances);
        cloned.setInstances(instances);

        List<Instance> logicInstances = this.logicInstances;
        if (logicInstances != null)
            logicInstances = new ArrayList<>(logicInstances);
        cloned.setLogicInstances(logicInstances);
        return cloned;
    }
}
