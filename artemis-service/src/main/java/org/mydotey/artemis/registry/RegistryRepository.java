package org.mydotey.artemis.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.InstanceChange;
import org.mydotey.artemis.InstanceKey;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.artemis.config.RangeValueFilter;
import org.mydotey.artemis.lease.Lease;
import org.mydotey.artemis.lease.LeaseCleanEventListener;
import org.mydotey.artemis.lease.LeaseManager;
import org.mydotey.artemis.util.InstanceChangeComparator;
import org.mydotey.artemis.util.InstanceChecker;
import org.mydotey.artemis.util.SameRegionChecker;
import org.mydotey.java.StringExtension;
import org.mydotey.java.ThreadExtension;
import org.mydotey.java.collection.CollectionExtension;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class RegistryRepository {

    private static final Logger _logger = LoggerFactory.getLogger(RegistryRepository.class);

    private static RegistryRepository _instance;

    public static RegistryRepository getInstance() {
        if (_instance == null) {
            synchronized (RegistryRepository.class) {
                if (_instance == null)
                    _instance = new RegistryRepository();
            }
        }

        return _instance;
    }

    private Property<String, Integer> _mapInitCapacityProperty = ArtemisConfig.properties().getIntProperty(
        "artemis.service.registry.data.init-capacity",
        10 * 1000, new RangeValueFilter<>(1 * 1000, 100 * 1000));

    private Property<String, Integer> _maxInstanceChangeBufferSizeProperty = ArtemisConfig.properties()
        .getIntProperty("artemis.service.registry.data.instance-change.max-buffer-size", 10 * 1000,
            new RangeValueFilter<>(1000, 1 * 1000 * 1000));

    private Property<String, Integer> _instanceChangePollWaitProperty = ArtemisConfig.properties()
        .getIntProperty("artemis.service.registry.data.instance-change.poll-wait", 20,
            new RangeValueFilter<>(1, 30 * 1000));

    private LeaseManager<Instance> _leaseManager = new LeaseManager<Instance>("artemis.service.registry.instance");
    private LeaseManager<Instance> _legacyInstanceLeaseManager = new LeaseManager<Instance>(
        "artemis.service.registry.legacy-instance");
    private ConcurrentHashMap<String, Service> _services = new ConcurrentHashMap<>(
        _mapInitCapacityProperty.getValue().intValue(), 0.9f);
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Lease<Instance>>> _leases = new ConcurrentHashMap<>(
        _mapInitCapacityProperty.getValue().intValue(), 0.9f);

    private ConcurrentSkipListSet<InstanceChange> _instanceChangeSet = new ConcurrentSkipListSet<>(
        InstanceChangeComparator.DEFAULT);

    private RegistryRepository() {
        _leaseManager.addLeaseCleanEventListener(new LeaseCleanEventListener<Instance>() {
            @Override
            public void onClean(List<Lease<Instance>> cleaned) {
                onLeaseClean(cleaned);
            }
        });

        _legacyInstanceLeaseManager.addLeaseCleanEventListener(new LeaseCleanEventListener<Instance>() {
            @Override
            public void onClean(List<Lease<Instance>> cleaned) {
                onLeaseClean(cleaned);
            }
        });
    }

    public InstanceChange pollInstanceChange() {
        while (true) {
            InstanceChange instanceChange = _instanceChangeSet.pollFirst();
            if (instanceChange != null)
                return instanceChange;

            ThreadExtension.sleep(_instanceChangePollWaitProperty.getValue().intValue());
        }
    }

    public void register(Instance instance) {
        InstanceChecker.DEFAULT.check(instance, "instance");
        SameRegionChecker.DEFAULT.check(instance.getRegionId(), "instance.regionId");

        String serviceId = instance.getServiceId();
        Service service = new Service(serviceId);
        _services.put(service.getServiceId(), service);

        Lease<Instance> lease = isLegacyInstance(instance) ? _legacyInstanceLeaseManager.register(instance)
            : _leaseManager.register(instance);
        ConcurrentHashMap<String, Lease<Instance>> serviceInstances = _leases.computeIfAbsent(serviceId,
            k -> new ConcurrentHashMap<>());

        serviceInstances.put(instance.getInstanceId(), lease);

        addInstanceChange(instance, InstanceChange.ChangeType.NEW);
    }

    public boolean heartbeat(Instance instance) {
        InstanceChecker.DEFAULT.check(instance, "instance");
        SameRegionChecker.DEFAULT.check(instance.getRegionId(), "instance.regionId");

        Lease<Instance> lease = isLegacyInstance(instance) ? _legacyInstanceLeaseManager.get(instance)
            : _leaseManager.get(instance);
        return lease != null && lease.renew();
    }

    public void unregister(Instance instance) {
        InstanceChecker.DEFAULT.check(instance, "instance");
        SameRegionChecker.DEFAULT.check(instance.getRegionId(), "instance.regionId");

        Lease<Instance> lease = isLegacyInstance(instance) ? _legacyInstanceLeaseManager.get(instance)
            : _leaseManager.get(instance);
        if (lease != null)
            lease.evict();
    }

    public Instance getInstance(String serviceId, String instanceId) {
        ConcurrentHashMap<String, Lease<Instance>> serviceInstances = _leases.get(serviceId);
        if (serviceInstances == null)
            return null;

        Lease<Instance> lease = serviceInstances.get(instanceId);
        if (lease == null)
            return null;

        return lease.data();
    }

    public Map<InstanceKey, Instance> getInstances() {
        Map<InstanceKey, Instance> instances = new HashMap<>();
        for (Map<String, Lease<Instance>> serviceLeases : _leases.values()) {
            for (Lease<Instance> lease : serviceLeases.values()) {
                instances.put(InstanceKey.of(lease.data()), lease.data());
            }
        }

        return instances;
    }

    public Service getService(String serviceId) {
        ConcurrentHashMap<String, Lease<Instance>> serviceInstances = _leases.get(serviceId);
        if (serviceInstances == null || serviceInstances.size() == 0)
            return null;

        List<Instance> instances = new ArrayList<Instance>();
        for (Lease<Instance> lease : serviceInstances.values()) {
            instances.add(lease.data());
        }

        if (instances.size() == 0)
            return null;

        Service service = getApplicationInternal(serviceId);
        service.setInstances(instances);
        return service;
    }

    public List<Service> getServices() {
        List<Service> services = new ArrayList<>();
        for (String serviceId : _leases.keySet()) {
            Service service = getService(serviceId);
            if (service == null)
                continue;

            services.add(service);
        }

        return services;
    }

    public LeaseManager<Instance> getLeaseManager() {
        return _leaseManager;
    }

    public LeaseManager<Instance> getLegacyInstanceLeaseManager() {
        return _legacyInstanceLeaseManager;
    }

    public ListMultimap<Service, Lease<Instance>> getLeases() {
        return getLeases(_leases.keySet());
    }

    public ListMultimap<Service, Lease<Instance>> getLeases(Collection<String> serviceIds) {
        ListMultimap<Service, Lease<Instance>> leases = ArrayListMultimap.create();
        for (String serviceId : serviceIds) {
            Service service = getApplicationInternal(serviceId);
            leases.putAll(service, getLeases(serviceId));
        }

        return leases;
    }

    public Collection<Lease<Instance>> getLeases(String serviceId) {
        Map<String, Lease<Instance>> serviceInstances = _leases.get(serviceId);
        if (serviceInstances == null)
            return Collections.emptyList();

        return serviceInstances.values();
    }

    public ListMultimap<Service, Lease<Instance>> getLeases(LeaseManager<Instance> leaseManager) {
        ListMultimap<Service, Lease<Instance>> leases = ArrayListMultimap.create();
        for (String serviceId : _leases.keySet()) {
            Service service = getApplicationInternal(serviceId);
            Collection<Lease<Instance>> serviceLeases = getLeases(serviceId, leaseManager);
            if (CollectionExtension.isEmpty(serviceLeases))
                continue;

            leases.putAll(service, serviceLeases);
        }

        return leases;
    }

    public ListMultimap<Service, Lease<Instance>> getLeases(Collection<String> serviceIds,
        LeaseManager<Instance> leaseManager) {
        ListMultimap<Service, Lease<Instance>> leases = ArrayListMultimap.create();
        for (String serviceId : serviceIds) {
            Service service = getApplicationInternal(serviceId);
            leases.putAll(service, getLeases(serviceId, leaseManager));
        }

        return leases;
    }

    public Collection<Lease<Instance>> getLeases(String serviceId, LeaseManager<Instance> leaseManager) {
        Map<String, Lease<Instance>> serviceInstances = _leases.get(serviceId);
        if (serviceInstances == null)
            return Collections.emptyList();

        List<Lease<Instance>> leases = new ArrayList<>();
        for (Lease<Instance> lease : serviceInstances.values()) {
            if (leaseManager.hasLeaseOf(lease.data()))
                leases.add(lease);
        }

        return leases;
    }

    public void addInstanceChange(InstanceChange instanceChange) {
        if (instanceChange == null)
            return;

        if (_instanceChangeSet.size() > _maxInstanceChangeBufferSizeProperty.getValue().intValue()) {
            if (!_instanceChangeSet.remove(instanceChange)) {
                _instanceChangeSet.pollFirst();
                _logger.warn("The instance change buffer is full. Maybe something is bad!");
            }
        }

        _instanceChangeSet.add(instanceChange);
    }

    private void onLeaseClean(List<Lease<Instance>> cleaned) {
        HashSet<String> serviceIds = new HashSet<>();
        for (Lease<Instance> lease : cleaned) {
            String serviceId = lease.data().getServiceId();
            ConcurrentHashMap<String, Lease<Instance>> instanceLeases = _leases.get(serviceId);
            if (instanceLeases == null)
                continue;

            Lease<Instance> instanceLease = instanceLeases.get(lease.data().getInstanceId());
            if (instanceLease == null)
                continue;

            Lease<Instance> existing = instanceLeases.remove(lease.data().getInstanceId());
            if (existing != null && existing.creationTime() > lease.creationTime()) {
                instanceLeases.putIfAbsent(lease.data().getInstanceId(), existing);
                continue;
            }

            serviceIds.add(serviceId);
            addInstanceChange(lease.data(), InstanceChange.ChangeType.DELETE);
        }

        for (String serviceId : serviceIds) {
            ConcurrentHashMap<String, Lease<Instance>> instanceLeases = _leases.get(serviceId);
            if (instanceLeases == null || instanceLeases.size() == 0) {
                _leases.remove(serviceId);
                _services.remove(serviceId);
            }
        }
    }

    private Service getApplicationInternal(String serviceId) {
        Service service = _services.get(serviceId);
        return service == null ? new Service(serviceId) : service.clone();
    }

    private void addInstanceChange(Instance instance, String changeType) {
        InstanceChange instanceChange = new InstanceChange(instance, changeType);
        addInstanceChange(instanceChange);
    }

    private boolean isLegacyInstance(Instance instance) {
        if (CollectionExtension.isEmpty(instance.getMetadata()))
            return false;

        return !StringExtension.isBlank(instance.getMetadata().get("java_registry"));
    }

}
