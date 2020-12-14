package org.mydotey.artemis.metric;

import java.util.Map;

import org.mydotey.java.ObjectExtension;

/**
 * Created by w.jian on 2016/9/12.
 */
public class StatusMetricConfig<T> extends MetricConfig {

    private StatusProvider<T> _statusProvider;

    public StatusMetricConfig(StatusProvider<T> statusProvider, Map<String, String> metadata) {
        super(metadata);

        ObjectExtension.requireNonNull(statusProvider, "statusProvider");
        _statusProvider = statusProvider;
    }

    public StatusProvider<T> statusProvider() {
        return _statusProvider;
    }
}
