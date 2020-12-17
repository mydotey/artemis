package org.mydotey.artemis.discovery.notify;

import org.mydotey.artemis.InstanceChange;

import com.google.common.base.Function;

public interface NotificationFilter extends Function<InstanceChange, InstanceChange> {

    default String getId() {
        return this.getClass().getSimpleName();
    }

}
