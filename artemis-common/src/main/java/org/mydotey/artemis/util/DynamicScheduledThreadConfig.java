package org.mydotey.artemis.util;

import org.mydotey.artemis.config.RangePropertyConfig;
import org.mydotey.java.ObjectExtension;
import org.mydotey.scf.facade.StringProperties;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class DynamicScheduledThreadConfig {

    public static final String INIT_DELAY_PROPERTY_KEY = "dynamic-scheduled-thread.init-delay";
    public static final String RUN_INTERVAL_PROPERTY_KEY = "dynamic-scheduled-thread.run-interval";

    private StringProperties _properties;
    private RangePropertyConfig<Integer> _initDelayRange;
    private RangePropertyConfig<Integer> _runIntervalRange;

    public DynamicScheduledThreadConfig(StringProperties properties,
        RangePropertyConfig<Integer> initDelayRange,
        RangePropertyConfig<Integer> runIntervalRange) {
        ObjectExtension.requireNonNull(properties, "properties");
        ObjectExtension.requireNonNull(initDelayRange, "initDelayRange");
        ObjectExtension.requireNonNull(initDelayRange.defaultValue(), "initDelayRange.defaultValue");
        ObjectExtension.requireNonNull(runIntervalRange, "runIntervalRange");
        ObjectExtension.requireNonNull(runIntervalRange.defaultValue(), "runIntervalRange.defaultValue");

        _properties = properties;
        _initDelayRange = initDelayRange;
        _runIntervalRange = runIntervalRange;
    }

    public StringProperties properties() {
        return _properties;
    }

    public RangePropertyConfig<Integer> initDelayRange() {
        return _initDelayRange;
    }

    public RangePropertyConfig<Integer> runIntervalRange() {
        return _runIntervalRange;
    }

}
