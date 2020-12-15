package org.mydotey.artemis.lease;

import java.util.Map;
import java.util.function.Function;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.scf.filter.RangeValueConfig;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.artemis.metric.ArtemisMetricManagers;
import org.mydotey.artemis.metric.EventMetric;
import org.mydotey.artemis.metric.MetricConfig;
import org.mydotey.artemis.metric.MetricNames;
import org.mydotey.artemis.util.DynamicScheduledThread;
import org.mydotey.artemis.util.DynamicScheduledThreadConfig;
import org.mydotey.java.ObjectExtension;
import org.mydotey.scf.Property;
import org.mydotey.util.CounterBuffer;
import org.mydotey.util.TimeSequenceCircularBufferConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class LeaseUpdateSafeChecker {

    private static final Logger _logger = LoggerFactory.getLogger(LeaseUpdateSafeChecker.class);
    private static final String LEASE_UPDATE_IDENTITY = "lease_update";

    private Property<String, Boolean> _enabledProperty;
    private Property<String, Integer> _timeWindowProperty;
    private Property<String, Integer> _percentageThresholdProperty;
    private Property<String, Integer> _maxCountThresholdProperty;
    private Property<String, Integer> _maxCountResetIntervalProperty;
    private volatile CounterBuffer<String> _counterBuffer;
    private volatile long _maxCount;
    private volatile long _maxCountLastUpdateTime;
    private volatile boolean _isSafe = true;
    private DynamicScheduledThread _safeCheckThread;

    private EventMetric _eventMetric;

    private LeaseManager<?> _leaseManager;

    private String _safeCheckerId;

    private Function<Integer, Integer> _timeWindowFilter = value -> {
        if (value == null || value < 10 * 1000)
            return 10 * 1000;

        if (value > 5 * 60 * 1000)
            return 5 * 60 * 1000;

        int m = value % 1000;
        if (m != 0)
            value = value - m;

        return value;
    };

    public LeaseUpdateSafeChecker(LeaseManager<?> leaseManager) {
        ObjectExtension.requireNonNull(leaseManager, "leaseManager");
        _leaseManager = leaseManager;
        _safeCheckerId = _leaseManager.managerId() + ".lease-manager.lease-update-safe-checker";

        Map<String, String> metadata = ImmutableMap.of(MetricNames.METRIC_NAME_KEY_DISTRIBUTION,
            _safeCheckerId + ".event.distribution");
        _eventMetric = ArtemisMetricManagers.DEFAULT.eventMetricManager().getMetric(_safeCheckerId + ".event",
            new MetricConfig(metadata));

        initConfig();

        scheduleCheckTask();
    }

    public long maxCount() {
        return _maxCount;
    }

    public long maxCountLastUpdateTime() {
        return _maxCountLastUpdateTime;
    }

    public long countLastTimeWindow() {
        return _counterBuffer.get(LEASE_UPDATE_IDENTITY);
    }

    public boolean isSafe() {
        return _isSafe || !isEnabled();
    }

    public boolean isEnabled() {
        return _enabledProperty.getValue().booleanValue();
    }

    protected void markUpdate() {
        _counterBuffer.increment(LEASE_UPDATE_IDENTITY);
    }

    private void initConfig() {
        _enabledProperty = ArtemisConfig.properties().getBooleanProperty(_safeCheckerId + ".enabled", true);
        _timeWindowProperty = ArtemisConfig.properties().getIntProperty(_safeCheckerId + ".time-window", 10 * 1000,
            _timeWindowFilter);
        _percentageThresholdProperty = ArtemisConfig.properties()
            .getIntProperty(_safeCheckerId + ".percentage-threshold", 85, new RangeValueFilter<>(50, 100));
        _maxCountThresholdProperty = ArtemisConfig.properties().getIntProperty(_safeCheckerId + ".max-count-threshold",
            50, new RangeValueFilter<>(0, 1000 * 1000));
        _maxCountResetIntervalProperty = ArtemisConfig.properties().getIntProperty(
            _safeCheckerId + ".max-count-reset-interval", 10 * 60 * 1000,
            new RangeValueFilter<>(1 * 60 * 1000, 24 * 60 * 60 * 1000));

        _timeWindowProperty.addChangeListener(event -> resetLeaseUpdateCounterBuffer());

        _enabledProperty.addChangeListener(event -> resetMaxCount());
    }

    private void scheduleCheckTask() {
        resetLeaseUpdateCounterBuffer();
        DynamicScheduledThreadConfig dynamicScheduledThreadConfig = new DynamicScheduledThreadConfig(
            ArtemisConfig.properties(),
            new RangeValueConfig<Integer>(1000, 100, 60 * 1000),
            new RangeValueConfig<Integer>(1000, 100, 60 * 1000));
        _safeCheckThread = new DynamicScheduledThread(_safeCheckerId, new Runnable() {
            @Override
            public void run() {
                try {
                    safeCheck();
                } catch (Throwable ex) {
                    _logger.error("safe check failed.", ex);
                }
            }
        }, dynamicScheduledThreadConfig);
        _safeCheckThread.setDaemon(true);
        _safeCheckThread.start();
    }

    private void resetLeaseUpdateCounterBuffer() {
        int timeWindow = _timeWindowProperty.getValue();
        _counterBuffer = new CounterBuffer<>(new TimeSequenceCircularBufferConfig.Builder()
            .setTimeWindow(timeWindow).setBucketTtl(1000).build());
        resetMaxCount();
        _logger.info("counterBuffer updated. TimeWindow: " + timeWindow);
    }

    private void safeCheck() {
        long leaseUpdateCountLastTimeWindow = countLastTimeWindow();
        long maxCount = _maxCount;
        if (leaseUpdateCountLastTimeWindow > maxCount) {
            updateMaxCount(leaseUpdateCountLastTimeWindow);
            return;
        }

        if (maxCount < _maxCountThresholdProperty.getValue().intValue())
            return;

        if (maxCount <= 0)
            return;

        long percentage = leaseUpdateCountLastTimeWindow * 100 / maxCount;
        if (percentage < _percentageThresholdProperty.getValue().intValue()) {
            _isSafe = false;
            _eventMetric.addEvent("unsafe");
            String errorMessage = "Lease update count is too low! Maybe something bad happen! Renewal pencentage is: "
                + percentage;
            if (_enabledProperty.getValue().booleanValue())
                _logger.error(errorMessage);
            else
                _logger.warn(errorMessage);

            return;
        }

        _isSafe = true;
        _eventMetric.addEvent("safe");

        if (System.currentTimeMillis() - _maxCountLastUpdateTime > _maxCountResetIntervalProperty.getValue()
            .intValue()) {
            _logger.warn("Lease Manager lease update max count is changed from {} to {} for {} ms update", _maxCount,
                leaseUpdateCountLastTimeWindow,
                _maxCountResetIntervalProperty.getValue().intValue());
            updateMaxCount(leaseUpdateCountLastTimeWindow);
        }
    }

    private void resetMaxCount() {
        updateMaxCount(0);
    }

    private void updateMaxCount(long maxCount) {
        _logger.info("Lease Manager lease update max count is changed from {} to {}", _maxCount, maxCount);
        _maxCount = maxCount;
        _maxCountLastUpdateTime = System.currentTimeMillis();
    }

}
