package org.mydotey.artemis.ratelimiter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.mydotey.artemis.config.MapValueCorrector;
import org.mydotey.scf.util.PropertyKeyGenerator;
import org.mydotey.java.BooleanExtension;
import org.mydotey.scf.Property;
import org.mydotey.scf.facade.StringProperties;
import org.mydotey.scf.filter.PipelineValueFilter;
import org.mydotey.scf.type.string.StringInplaceConverter;
import org.mydotey.scf.type.string.StringToLongConverter;
import org.mydotey.util.CounterBuffer;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
class DefaultRateLimiter implements RateLimiter {

    private String _rateLimiterId;
    private Property<String, Boolean> _enabledProperty;
    private Property<String, Long> _defaultRateLimitProperty;
    private Property<String, Map<String, Long>> _rateLimitMapProperty;
    private CounterBuffer<String> _counterBuffer;

    public DefaultRateLimiter(String rateLimiterId, StringProperties properties,
        RateLimiterConfig rateLimiterConfig) {
        _rateLimiterId = rateLimiterId;

        String propertyKey = PropertyKeyGenerator.generatePropertyKey(_rateLimiterId,
            RateLimiterConfig.ENABLED_PROPERTY_KEY);
        _enabledProperty = properties.getBooleanProperty(propertyKey, rateLimiterConfig.enabled());

        propertyKey = PropertyKeyGenerator.generatePropertyKey(_rateLimiterId,
            RateLimiterConfig.DEFAULT_RATE_LIMIT_PROPERTY_KEY);
        _defaultRateLimitProperty = properties.getLongProperty(propertyKey,
            rateLimiterConfig.rateLimitPropertyConfig());

        PipelineValueFilter<Long> rateLimitValueCorrector = new PipelineValueFilter<>(Arrays.asList(
            rateLimiterConfig.rateLimitPropertyConfig().toDefaultValueFilter(),
            rateLimiterConfig.rateLimitPropertyConfig().toRangeValueFilter()));
        MapValueCorrector<String, Long> rateLimitMapValueCorrector = new MapValueCorrector<>(rateLimitValueCorrector);
        propertyKey = PropertyKeyGenerator.generatePropertyKey(_rateLimiterId,
            RateLimiterConfig.RATE_LIMIT_MAP_PROPERTY_KEY);
        _rateLimitMapProperty = properties.getMapProperty(propertyKey, new HashMap<>(), StringInplaceConverter.DEFAULT,
            StringToLongConverter.DEFAULT, rateLimitMapValueCorrector);

        _counterBuffer = new CounterBuffer<>(rateLimiterConfig.bufferConfig());
    }

    @Override
    public String rateLimiterId() {
        return _rateLimiterId;
    }

    @Override
    public boolean isRateLimited(String identity) {
        if (BooleanExtension.isFalse(_enabledProperty.getValue()))
            return false;

        _counterBuffer.increment(identity);
        if (_counterBuffer.get(identity) <= getRateLimit(identity))
            return false;

        _counterBuffer.decrement(identity);
        return true;
    }

    private long getRateLimit(String identity) {
        Map<String, Long> rateLimitMap = _rateLimitMapProperty.getValue();
        Long rateLimit = null;
        if (rateLimitMap != null)
            rateLimit = rateLimitMap.get(identity);

        if (rateLimit == null)
            rateLimit = _defaultRateLimitProperty.getValue();

        return rateLimit == null ? Long.MAX_VALUE : rateLimit.longValue();
    }

}
