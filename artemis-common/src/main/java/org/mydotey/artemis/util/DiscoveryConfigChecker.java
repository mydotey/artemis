package org.mydotey.artemis.util;

import org.mydotey.artemis.checker.ValueChecker;
import org.mydotey.artemis.discovery.DiscoveryConfig;
import org.mydotey.java.ObjectExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class DiscoveryConfigChecker implements ValueChecker<DiscoveryConfig> {

    public static final DiscoveryConfigChecker DEFAULT = new DiscoveryConfigChecker();

    @Override
    public void check(DiscoveryConfig value, String valueName) {
        ObjectExtension.requireNonNull(value, valueName);
        ObjectExtension.requireNonBlank(value.getServiceId(), "value.serviceId");
    }

}
