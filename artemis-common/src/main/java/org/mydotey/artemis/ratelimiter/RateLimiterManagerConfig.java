package org.mydotey.artemis.ratelimiter;

import org.mydotey.java.ObjectExtension;
import org.mydotey.scf.facade.StringProperties;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class RateLimiterManagerConfig {

    private StringProperties _properties;

    public RateLimiterManagerConfig(StringProperties properties) {
        ObjectExtension.requireNonNull(properties, "properties");
        _properties = properties;
    }

    public StringProperties properties() {
        return _properties;
    }

}
