package org.mydotey.artemis.metric;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ArtemisMetricManagers {

    private static final Logger _logger = LoggerFactory.getLogger(ArtemisMetricManagers.class);

    public static final ArtemisMetricManagers DEFAULT = createDefault();

    private static ArtemisMetricManagers createDefault() {
        Property<String, String> providerProperty = ArtemisConfig.properties()
            .getStringProperty("artemis.metric.default.managers-provider");

        ArtemisMetricManagersProvider provider = null;
        String providerType = StringExtension.trim(providerProperty.getValue());
        if (!StringExtension.isBlank(providerType)) {
            try {
                Class<?> clazz = Class.forName(providerType);
                provider = (ArtemisMetricManagersProvider) clazz.newInstance();
            } catch (Throwable ex) {
                _logger.error("Init metric managers provider failed.", ex);
            }
        }

        if (provider == null)
            provider = NullArtemisMetricManagersProvider.INSTANCE;

        _logger.info("Inited defualt ArtemisMetricManagers with provider " + provider.getClass().getName());
        return new ArtemisMetricManagers(provider);
    }

    private ArtemisMetricManagersProvider _provider;

    public ArtemisMetricManagers(ArtemisMetricManagersProvider provider) {
        ObjectExtension.requireNonNull(provider, "provider");
        _provider = provider;
    }

    public EventMetricManager eventMetricManager() {
        return _provider.getEventMetricManager();
    }

    public AuditMetricManager valueMetricManager() {
        return _provider.getValueMetricManager();
    }

    public StatusMetricManager<Double> getStatusMetricManager() {
        return _provider.getStatusMetricManager();
    }
}
