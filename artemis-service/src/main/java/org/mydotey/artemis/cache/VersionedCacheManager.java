package org.mydotey.artemis.cache;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.artemis.config.RangeValueFilter;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.java.ObjectExtension;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class VersionedCacheManager<T, D> {

    public static final long EMPTY_VERSION = 0L;

    private static final Logger _logger = LoggerFactory.getLogger(VersionedCacheManager.class);

    private Property<String, Integer> _cacheCountProperty;
    private Property<String, Integer> _cacheRefreshInitDelayProperty;
    private Property<String, Integer> _cacheRefreshIntervalProperty;

    private String _managerId;
    private Supplier<T> _dataGenerator;
    private BiFunction<T, T, D> _deltaGenerator;

    private volatile long _currentVersion = EMPTY_VERSION;
    private ConcurrentSkipListMap<Long, VersionedCache<T, D>> _cacheMap;
    private ScheduledExecutorService _cacheRefreshExecutorService;

    public VersionedCacheManager(String managerId, Supplier<T> dataGenerator, BiFunction<T, T, D> deltaGenerator) {
        ObjectExtension.requireNonBlank(managerId, "managerId");
        ObjectExtension.requireNonNull(dataGenerator, "dataGenerator");
        ObjectExtension.requireNonNull(deltaGenerator, "deltaGenerator");

        _managerId = managerId;
        _dataGenerator = dataGenerator;
        _deltaGenerator = deltaGenerator;

        _cacheCountProperty = ArtemisConfig.properties().getIntProperty(_managerId + ".versioned-cache.cache-count", 3,
            new RangeValueFilter<>(0, 10));
        _cacheRefreshInitDelayProperty = ArtemisConfig.properties().getIntProperty(
            _managerId + ".versioned-cache.cache-refresh.init-delay", 60 * 1000,
            new RangeValueFilter<>(0, 5 * 60 * 1000));
        _cacheRefreshIntervalProperty = ArtemisConfig.properties().getIntProperty(
            _managerId + ".versioned-cache.cache-refresh.interval", 30 * 1000,
            new RangeValueFilter<>(1 * 1000, 5 * 60 * 1000));

        _cacheMap = new ConcurrentSkipListMap<>();

        final String traceId = _managerId + ".data-cache.cache-refresh";
        _cacheRefreshExecutorService = Executors.newSingleThreadScheduledExecutor();
        _cacheRefreshExecutorService.scheduleWithFixedDelay(() -> {
            try {
                ArtemisTraceExecutor.INSTANCE.execute(traceId, this::updateCache);
            } catch (Throwable ex) {
                _logger.error("updateCache failed.", ex);
            }
        }, _cacheRefreshInitDelayProperty.getValue().intValue(), _cacheRefreshIntervalProperty.getValue().intValue(),
            TimeUnit.MILLISECONDS);
    }

    public VersionedData<T> get() {
        long version = _currentVersion;
        VersionedData<T> data = null;

        VersionedCache<T, D> cache = _cacheMap.get(version);
        if (cache != null)
            data = cache.getVersionedData();

        if (data == null)
            data = new VersionedData<T>(version, _dataGenerator.get());

        return data;
    }

    public VersionedData<D> getDelta(long version) {
        VersionedCache<T, D> cache = _cacheMap.get(version);
        return cache == null ? null : cache.getVersionedDelta();
    }

    private void updateCache() {
        long version = System.currentTimeMillis();
        T data = _dataGenerator.get();
        VersionedCache<T, D> cache = new VersionedCache<>(new VersionedData<>(version, data), null);
        _cacheMap.put(version, cache);
        while (_cacheMap.size() > _cacheCountProperty.getValue().intValue()) {
            Entry<Long, VersionedCache<T, D>> entry = _cacheMap.pollFirstEntry();
            if (entry == null)
                break;
        }

        for (Long key : _cacheMap.keySet()) {
            if (key >= version)
                continue;

            VersionedCache<T, D> oldCachedData = _cacheMap.get(key);
            D delta = _deltaGenerator.apply(oldCachedData.getVersionedData().getData(), data);
            oldCachedData.setVersionedDelta(new VersionedData<>(version, delta));
        }

        _currentVersion = version;
    }

}
