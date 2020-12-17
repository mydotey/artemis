package org.mydotey.artemis.discovery.notify;

import java.util.function.Consumer;

import org.mydotey.artemis.InstanceChange;

public interface InstanceChangeSubscriber extends Consumer<InstanceChange> {

    default String getId() {
        return this.getClass().getSimpleName();
    }

}
