package org.mydotey.artemis.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;

import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class RateLimiterManager {

    private String _managerId;
    private RateLimiterManagerConfig _config;
    private ConcurrentHashMap<String, RateLimiter> _rateLimiterCache;

    public RateLimiterManager(String managerId, RateLimiterManagerConfig config) {
        ObjectExtension.requireNonBlank(managerId, "managerId");
        ObjectExtension.requireNonNull(config, "config");
        _managerId = managerId;
        _config = config;
        _rateLimiterCache = new ConcurrentHashMap<>();
    }

    public String managerId() {
        return _managerId;
    }

    public RateLimiterManagerConfig config() {
        return _config;
    }

    public RateLimiter getRateLimiter(String rateLimiterId, final RateLimiterConfig rateLimiterConfig) {
        ObjectExtension.requireNonBlank(rateLimiterId, "rateLimiterId");
        ObjectExtension.requireNonNull(rateLimiterConfig, "rateLimiterConfig");

        final String trimmedRateLimiterId = StringExtension.trim(rateLimiterId);
        return _rateLimiterCache.computeIfAbsent(trimmedRateLimiterId,
            k -> new DefaultRateLimiter(trimmedRateLimiterId, _config.properties(), rateLimiterConfig));
    }

}
