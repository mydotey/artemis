package org.mydotey.artemis.util;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.InstanceChange;
import org.mydotey.artemis.InstanceChange.ChangeType;
import org.mydotey.java.ObjectExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public final class InstanceChanges {

    public static final String RELOAD_FAKE_INSTANCE_ID = ChangeType.RELOAD;
    public static final String RELOAD_FAKE_IP = "0.0.0.0";
    public static final String RELOAD_FAKE_URL = "http://serviceId/reload";

    private InstanceChanges() {

    }

    public static InstanceChange newReloadInstanceChange(String serviceId) {
        ObjectExtension.requireNonBlank(serviceId, "serviceId");
        Instance instance = new Instance();
        instance.setServiceId(serviceId);
        instance.setInstanceId(RELOAD_FAKE_INSTANCE_ID);
        instance.setIp(RELOAD_FAKE_IP);
        instance.setUrl(RELOAD_FAKE_URL);
        return new InstanceChange(instance, ChangeType.RELOAD);
    }

}
