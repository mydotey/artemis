package org.mydotey.artemis.util;

import org.mydotey.artemis.Service;
import org.mydotey.artemis.checker.ValueChecker;
import org.mydotey.java.ObjectExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ServiceChecker implements ValueChecker<Service> {

    public static final ServiceChecker DEFAULT = new ServiceChecker();

    @Override
    public void check(Service value, String valueName) {
        ObjectExtension.requireNonNull(value, valueName);
        ObjectExtension.requireNonBlank(value.getServiceId(), valueName + ".serviceId");
    }

}
