package org.mydotey.artemis.discovery;

import org.mydotey.artemis.Service;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public interface DiscoveryFilter {

    void filter(Service service, DiscoveryConfig discoveryConfig);

}
