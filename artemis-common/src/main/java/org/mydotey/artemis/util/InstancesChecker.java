package org.mydotey.artemis.util;

import java.util.Collection;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.checker.ValueChecker;
import org.mydotey.artemis.checker.ValueCheckers;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class InstancesChecker implements ValueChecker<Collection<Instance>> {

    public static final InstancesChecker DEFAULT = new InstancesChecker();

    @Override
    public void check(final Collection<Instance> value, final String valueName) {
        ValueCheckers.notNullOrEmpty(value, "instances");
        for (final Instance instance : value) {
            InstanceChecker.DEFAULT.check(instance, "instance");
        }
    }
}
