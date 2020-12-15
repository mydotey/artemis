package org.mydotey.artemis.web.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.mydotey.artemis.InstanceChange;
import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.artemis.management.ManagementRepository;
import org.mydotey.artemis.registry.RegistryRepository;
import org.mydotey.scf.Property;

import com.google.common.collect.Sets;

/**
 * Created by fang_j on 10/07/2016.
 */
public class InstanceChangeManager {

    private static final Logger _logger = LoggerFactory.getLogger(InstanceChangeManager.class);

    private static InstanceChangeManager _instanceChangeManager;
    private static Set<String> _notrackChangeTypesWhenInstanceUnavailable = Sets
        .newHashSet(InstanceChange.ChangeType.NEW, InstanceChange.ChangeType.CHANGE);

    public static InstanceChangeManager getInstance() {
        if (_instanceChangeManager == null) {
            synchronized (InstanceChangeManager.class) {
                if (_instanceChangeManager == null) {
                    _instanceChangeManager = new InstanceChangeManager();
                }
            }
        }

        return _instanceChangeManager;
    }

    private Property<String, Integer> _registryChangePublisherThreadCount = ArtemisConfig.properties()
        .getIntProperty("artemis.service.registry.change-publisher.thread-count", 20, new RangeValueFilter<>(1, 50));

    private RegistryRepository _registryRepository = RegistryRepository.getInstance();
    private ManagementRepository _managementRepository = ManagementRepository.getInstance();
    private final AtomicBoolean _inited = new AtomicBoolean(false);
    private List<Publisher> _publishers;

    private InstanceChangeManager() {
    }

    public void init(final List<Publisher> publishers) {
        ValueCheckers.notNullOrEmpty(publishers, "publishers");
        for (Publisher publisher : publishers) {
            ValueCheckers.notNull(publisher, "publisher");
        }
        if (_inited.compareAndSet(false, true)) {
            _publishers = publishers;
            initPublisherThreadPool(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            final InstanceChange instanceChange = _registryRepository.pollInstanceChange();
                            if (shouldPublishInstanceChange(instanceChange)) {
                                for (Publisher publisher : _publishers) {
                                    try {
                                        publisher.publish(instanceChange);
                                    } catch (Throwable ex) {
                                        _logger.error("Publish instance change failed", ex);
                                    }
                                }
                            }
                        } catch (final Throwable ex) {
                            _logger.error("Publish registry change failed", ex);
                        }
                    }
                }
            }, "Registry change publisher - %s", _registryChangePublisherThreadCount.getValue());
        }
    }

    private void initPublisherThreadPool(Runnable runnable, String threadNameFormat, int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            Thread workerThread = new Thread(runnable, String.format(threadNameFormat, i));
            workerThread.setDaemon(true);
            workerThread.start();
        }
    }

    private boolean shouldPublishInstanceChange(InstanceChange instanceChange) {
        if (!_notrackChangeTypesWhenInstanceUnavailable.contains(instanceChange.getChangeType()))
            return true;

        return !_managementRepository.isInstanceDown(instanceChange.getInstance());
    }

}
