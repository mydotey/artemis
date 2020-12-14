package org.mydotey.artemis.util;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.checker.ValueChecker;
import org.mydotey.java.ObjectExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class InstanceChecker implements ValueChecker<Instance> {

    public static final InstanceChecker DEFAULT = new InstanceChecker();

    @Override
    public void check(Instance value, String valueName) {
        ObjectExtension.requireNonNull(value, valueName);
        ObjectExtension.requireNonBlank(value.getServiceId(), valueName + ".serviceId");
        ObjectExtension.requireNonBlank(value.getInstanceId(), valueName + ".instanceId");
        ObjectExtension.requireNonBlank(value.getUrl(), valueName + ".url");
    }

}
