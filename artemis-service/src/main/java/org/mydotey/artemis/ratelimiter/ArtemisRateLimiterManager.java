package org.mydotey.artemis.ratelimiter;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.caravan.util.ratelimiter.RateLimiterManager;
import org.mydotey.caravan.util.ratelimiter.RateLimiterManagerConfig;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ArtemisRateLimiterManager extends RateLimiterManager {

    public static final ArtemisRateLimiterManager Instance = new ArtemisRateLimiterManager();

    private ArtemisRateLimiterManager() {
        super("artemis.service", new RateLimiterManagerConfig(ArtemisConfig.properties()));
    }

}
