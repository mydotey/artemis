package org.mydotey.artemis.web.util;

import org.mydotey.artemis.InstanceChange;

/**
 * Created by fang_j on 10/07/2016.
 */
public interface Publisher {
    public boolean publish(InstanceChange instanceChange);

}
