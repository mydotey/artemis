package org.mydotey.artemis.trace;

import java.util.Map;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.java.StringExtension;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public final class ArtemisTraceFactory implements TraceFactory {

    private static final Logger _logger = LoggerFactory.getLogger(ArtemisTraceFactory.class);

    public static final TraceFactory INSTANCE = new ArtemisTraceFactory();

    private Property<String, Boolean> _enableTraceProperty = ArtemisConfig.properties()
        .getBooleanProperty("artemis.trace.enabled", false);

    private Property<String, String> _factoryClassProperty = ArtemisConfig.properties()
        .getStringProperty("artemis.trace.factory-class");

    private TraceFactory _factory = NullTraceFactory.INSTANCE;

    private ArtemisTraceFactory() {
        String factoryClass = _factoryClassProperty.getValue();
        if (StringExtension.isBlank(factoryClass)) {
            _logger.info("No trace factory class is configured.");
            return;
        }

        try {
            Class<?> clazz = Class.forName(factoryClass.trim());
            _factory = (TraceFactory) clazz.newInstance();
            _logger.info("Init TraceFactory for class " + factoryClass + " succeeded.");
        } catch (Throwable ex) {
            _logger.error("Init TraceFactory for class " + factoryClass + " failed.", ex);
        }
    }

    @Override
    public Trace newTrace(String identity) {
        return traceFactory().newTrace(identity);
    }

    @Override
    public Trace newTrace(String identity, Map<String, String> data) {
        return traceFactory().newTrace(identity, data);
    }

    private TraceFactory traceFactory() {
        return _enableTraceProperty.getValue().booleanValue() ? _factory : NullTraceFactory.INSTANCE;
    }

}
