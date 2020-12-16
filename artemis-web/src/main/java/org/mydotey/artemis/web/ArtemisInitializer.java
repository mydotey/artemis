package org.mydotey.artemis.web;

import org.mydotey.artemis.cluster.ClusterManager;
import org.mydotey.artemis.cluster.NodeInitializer;
import org.mydotey.artemis.discovery.DiscoveryServiceImpl;
import org.mydotey.artemis.management.GroupDiscoveryFilter;
import org.mydotey.artemis.management.ManagementDiscoveryFilter;
import org.mydotey.artemis.management.ManagementInitializer;
import org.mydotey.artemis.management.ManagementRepository;
import org.mydotey.artemis.management.dao.DataConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public interface ArtemisInitializer {

    public static void init() throws Exception {
        Logger logger = LoggerFactory.getLogger(ArtemisInitializer.class);
        logger.info("===== Artemis is starting up! ========");
        DataConfig.init();

        DiscoveryServiceImpl.getInstance().addFilters(GroupDiscoveryFilter.getInstance(),
            ManagementDiscoveryFilter.getInstance());

        ManagementRepository.getInstance().addFilter(GroupDiscoveryFilter.getInstance());

        ClusterManager.INSTANCE.init(Lists.<NodeInitializer>newArrayList(ManagementInitializer.INSTANCE));
    }
}
