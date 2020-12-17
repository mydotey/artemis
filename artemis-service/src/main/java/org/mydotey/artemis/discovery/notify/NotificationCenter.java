package org.mydotey.artemis.discovery.notify;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mydotey.artemis.InstanceChange;
import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.artemis.registry.RegistryRepository;
import org.mydotey.java.ObjectExtension;
import org.mydotey.scf.Property;

import com.google.common.collect.Sets;

public class NotificationCenter {

    private static final Logger _logger = LoggerFactory.getLogger(NotificationCenter.class);

    private static NotificationCenter _instanceChangeManager;
    private static Set<String> _alwaysNotifyChangeTypes = Sets
        .newHashSet(InstanceChange.ChangeType.DELETE, InstanceChange.ChangeType.RELOAD);

    public static NotificationCenter getInstance() {
        if (_instanceChangeManager == null) {
            synchronized (NotificationCenter.class) {
                if (_instanceChangeManager == null) {
                    _instanceChangeManager = new NotificationCenter();
                }
            }
        }

        return _instanceChangeManager;
    }

    private Property<String, Integer> _registryChangePublisherThreadCount = ArtemisConfig.properties()
        .getIntProperty("artemis.service.discovery.notify.thread-count", 10, new RangeValueFilter<>(1, 100));

    private RegistryRepository _registryRepository = RegistryRepository.getInstance();
    private ConcurrentHashMap<String, InstanceChangeSubscriber> _subscribers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, NotificationFilter> _filters = new ConcurrentHashMap<>();

    private NotificationCenter() {
        for (int i = 0; i < _registryChangePublisherThreadCount.getValue(); i++) {
            Thread workerThread = new Thread(this::publish, String.format("notification-worker-%s", i));
            workerThread.setDaemon(true);
            workerThread.start();
        }
    }

    public void registerSubscriber(InstanceChangeSubscriber subscriber) {
        ObjectExtension.requireNonNull(subscriber, "subscriber");
        ObjectExtension.requireNonBlank(subscriber.getId(), "subscriber.id");
        InstanceChangeSubscriber existing = _subscribers.putIfAbsent(subscriber.getId(), subscriber);
        if (existing != null) {
            _logger.error("Existing subscriber {} is replaced by new subscriber, probably something bad!",
                existing.getId());
        }
    }

    public void registerFilter(NotificationFilter filter) {
        ObjectExtension.requireNonNull(filter, "filter");
        ObjectExtension.requireNonBlank(filter.getId(), "filter.id");
        NotificationFilter existing = _filters.putIfAbsent(filter.getId(), filter);
        if (existing != null) {
            _logger.error("Existing filter {} is replaced by new filter, probably something bad!",
                existing.getId());
        }
    }

    private void publish() {
        while (true) {
            try {
                InstanceChange instanceChange = _registryRepository.pollInstanceChange();
                instanceChange = filter(instanceChange);
                if (instanceChange == null) {
                    continue;
                }

                for (InstanceChangeSubscriber subscriber : _subscribers.values()) {
                    try {
                        subscriber.accept(instanceChange);
                    } catch (Throwable ex) {
                        _logger.error("Subscriber " + subscriber.getId() + " failed when handling instance change: "
                            + instanceChange, ex);
                    }
                }
            } catch (InterruptedException e) {
                break;
            } catch (Throwable ex) {
                _logger.error("Notification failed", ex);
            }
        }
    }

    private InstanceChange filter(InstanceChange instanceChange) {
        if (_alwaysNotifyChangeTypes.contains(instanceChange.getChangeType()))
            return instanceChange;

        for (NotificationFilter filter : _filters.values()) {
            try {
                instanceChange = filter.apply(instanceChange);
            } catch (Throwable ex) {
                _logger.error(
                    "Filter " + filter.getId() + " failed when handling instance change: " + instanceChange,
                    ex);
            }
            if (instanceChange == null) {
                break;
            }
        }

        return instanceChange;
    }
}
