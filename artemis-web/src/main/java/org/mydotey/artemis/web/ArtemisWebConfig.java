package org.mydotey.artemis.web;

import javax.annotation.PostConstruct;

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
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;

@Configuration
public class ArtemisWebConfig {

    private static final Logger logger = LoggerFactory.getLogger(ArtemisWebConfig.class);

    @PostConstruct
    public void init() throws Exception {
        logger.info("===== Application is starting up! ========");
        try {
            DataConfig.init();

            DiscoveryServiceImpl.getInstance().addFilters(GroupDiscoveryFilter.getInstance(),
                ManagementDiscoveryFilter.getInstance());

            ManagementRepository.getInstance().addFilter(GroupDiscoveryFilter.getInstance());

            ClusterManager.INSTANCE.init(Lists.<NodeInitializer>newArrayList(ManagementInitializer.INSTANCE));
        } catch (Throwable ex) {
            logger.error("WebApplicationInitalizer failed.", ex);
            ex.printStackTrace();
            throw ex;
        }
    }
}
