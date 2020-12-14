package org.mydotey.artemis.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.InstanceChange;
import org.mydotey.artemis.Service;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ServicesDeltaGenerator
    implements BiFunction<List<Service>, List<Service>, Map<Service, List<InstanceChange>>> {

    public static final ServicesDeltaGenerator DEFAULT = new ServicesDeltaGenerator();

    @Override
    public Map<Service, List<InstanceChange>> apply(List<Service> old, List<Service> current) {
        ObjectExtension.requireNonNull(old, "old");
        ObjectExtension.requireNonNull(current, "current");

        Map<Service, List<InstanceChange>> delta = new HashMap<>();

        Set<Service> oldSet = new HashSet<>(old);
        Set<Service> currentSet = new HashSet<>(current);
        Map<String, Service> oldMap = new HashMap<>();
        Map<String, Service> currentMap = new HashMap<>();
        for (Service service : oldSet) {
            oldMap.put(service.getServiceId(), service);

            if (currentSet.contains(service))
                continue;

            currentSet.add(new Service(service.getServiceId()));
        }
        for (Service service : currentSet) {
            currentMap.put(service.getServiceId(), service);

            if (oldSet.contains(service))
                continue;

            Service emptyService = new Service(service.getServiceId());
            oldSet.add(emptyService);
            oldMap.put(service.getServiceId(), emptyService);
        }

        for (String serviceId : oldMap.keySet()) {
            Service oldService = oldMap.get(serviceId);
            Service currentService = currentMap.get(serviceId);
            List<InstanceChange> instanceChanges = diff(oldService, currentService);
            if (CollectionExtension.isEmpty(instanceChanges))
                continue;

            Service emptyService = currentService.clone();
            emptyService.setInstances(null);
            delta.put(emptyService, instanceChanges);
        }

        return delta;
    }

    private List<InstanceChange> diff(Service old, Service current) {
        List<InstanceChange> instanceChanges = new ArrayList<>();
        Set<Instance> oldInstances = (old == null || old.getInstances() == null ? new HashSet<Instance>()
            : new HashSet<>(old.getInstances()));
        Set<Instance> currentInstances = (current == null || current.getInstances() == null ? new HashSet<Instance>()
            : new HashSet<>(current.getInstances()));
        for (Instance instance : oldInstances) {
            if (currentInstances.contains(instance))
                continue;

            instanceChanges.add(new InstanceChange(instance, InstanceChange.ChangeType.DELETE));
        }

        for (Instance instance : currentInstances) {
            if (oldInstances.contains(instance))
                continue;

            instanceChanges.add(new InstanceChange(instance, InstanceChange.ChangeType.NEW));
        }

        return instanceChanges;
    }

}
