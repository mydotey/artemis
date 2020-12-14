package org.mydotey.artemis.ratelimiter;

import org.mydotey.artemis.config.RangePropertyConfig;
import org.mydotey.java.ObjectExtension;
import org.mydotey.util.TimeSequenceCircularBufferConfig;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class RateLimiterConfig {

    public static final String ENABLED_PROPERTY_KEY = "rate-limiter.enabled";
    public static final String DEFAULT_RATE_LIMIT_PROPERTY_KEY = "rate-limiter.default-rate-limit";
    public static final String RATE_LIMIT_MAP_PROPERTY_KEY = "rate-limiter.rate-limit-map";

    private boolean _enabled;
    private RangePropertyConfig<Long> _rateLimitPropertyConfig;
    private TimeSequenceCircularBufferConfig _bufferConfig;

    public RateLimiterConfig(boolean enabled, RangePropertyConfig<Long> rateLimitPropertyConfig,
        TimeSequenceCircularBufferConfig bufferConfig) {
        ObjectExtension.requireNonNull(rateLimitPropertyConfig, "rateLimitPropertyConfig");
        ObjectExtension.requireNonNull(bufferConfig, "bufferConfig");

        _enabled = enabled;
        _rateLimitPropertyConfig = rateLimitPropertyConfig;
        _bufferConfig = bufferConfig;
    }

    public boolean enabled() {
        return _enabled;
    }

    public RangePropertyConfig<Long> rateLimitPropertyConfig() {
        return _rateLimitPropertyConfig;
    }

    public TimeSequenceCircularBufferConfig bufferConfig() {
        return _bufferConfig;
    }

}
