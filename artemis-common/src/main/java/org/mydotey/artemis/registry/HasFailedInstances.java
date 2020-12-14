package org.mydotey.artemis.registry;

import java.util.List;

import org.mydotey.artemis.HasResponseStatus;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public interface HasFailedInstances extends HasResponseStatus {

    List<FailedInstance> getFailedInstances();

}
