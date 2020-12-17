package org.mydotey.artemis.management;

import org.mydotey.artemis.InstanceChange;
import org.mydotey.artemis.discovery.notify.NotificationFilter;

public class ManagementNotificationFilter implements NotificationFilter {

    @Override
    public InstanceChange apply(InstanceChange instanceChange) {
        return ManagementRepository.getInstance().isInstanceDown(instanceChange.getInstance()) ? null : instanceChange;
    }

}
