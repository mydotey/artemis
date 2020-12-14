package org.mydotey.artemis.client;

import org.mydotey.artemis.Service;
import org.mydotey.artemis.discovery.DiscoveryConfig;

/**
 * Created by fang_j on 10/07/2016.
 */
public interface DiscoveryClient {

    Service getService(DiscoveryConfig discoveryConfig);

    void registerServiceChangeListener(DiscoveryConfig discoveryConfig, ServiceChangeListener listener);

}