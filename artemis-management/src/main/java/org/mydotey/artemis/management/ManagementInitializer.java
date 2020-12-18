package org.mydotey.artemis.management;

import java.util.concurrent.atomic.AtomicBoolean;

import org.mydotey.artemis.cluster.NodeInitializer;
import org.mydotey.artemis.cluster.NodeManager;
import org.mydotey.artemis.discovery.DiscoveryFilters;
import org.mydotey.artemis.discovery.notify.NotificationCenter;
import org.mydotey.artemis.management.dao.DataConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ManagementInitializer implements NodeInitializer {

    private static Logger _logger = LoggerFactory.getLogger(ManagementInitializer.class);

    public static final ManagementInitializer INSTANCE = new ManagementInitializer();

    private AtomicBoolean _started = new AtomicBoolean();

    private ManagementInitializer() {

    }

    public void init() throws Exception {
        if (!_started.compareAndSet(false, true))
            return;

        _logger.info("===== Artemis Management is starting up! ========");

        DataConfig.init();

        GroupRepository.getInstance().init();
        ZoneRepository.getInstance().init();
        ManagementRepository.getInstance().init();

        ManagementRepository.getInstance().addFilter(GroupDiscoveryFilter.getInstance());

        NodeManager.INSTANCE.registerInitializer(this);

        DiscoveryFilters.INSTANCE.registerFilter(GroupDiscoveryFilter.getInstance(),
            ManagementDiscoveryFilter.getInstance());

        NotificationCenter.getInstance().registerFilter(new ManagementNotificationFilter());
    }

    @Override
    public TargetType target() {
        return TargetType.DISCOVERY;
    }

    @Override
    public boolean initialized() {
        return ManagementRepository.getInstance().isLastRefreshSuccess()
            && GroupRepository.getInstance().isLastRefreshSuccess();
    }

}
