package org.mydotey.artemis.util;

import java.util.concurrent.atomic.AtomicBoolean;

import org.mydotey.scf.util.PropertyKeyGenerator;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;
import org.mydotey.java.ThreadExtension;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class DynamicScheduledThread extends Thread {

    private static final Logger _logger = LoggerFactory.getLogger(DynamicScheduledThread.class);

    private String _threadId;
    private Runnable _runnable;
    private final Property<String, Integer> _initDelayProperty;
    private final Property<String, Integer> _runIntervalProperty;
    private final AtomicBoolean _isShutdown = new AtomicBoolean();

    public DynamicScheduledThread(String threadId, Runnable runnable, DynamicScheduledThreadConfig config) {
        ObjectExtension.requireNonBlank(threadId, "threadId");
        ObjectExtension.requireNonNull(runnable, "runnable");
        ObjectExtension.requireNonNull(config, "config");

        _threadId = StringExtension.trim(threadId);
        _runnable = runnable;

        setName(_threadId);

        String propertyKey = PropertyKeyGenerator.generatePropertyKey(_threadId,
            DynamicScheduledThreadConfig.INIT_DELAY_PROPERTY_KEY);
        _initDelayProperty = config.properties().getIntProperty(propertyKey, config.initDelayRange());

        propertyKey = PropertyKeyGenerator.generatePropertyKey(_threadId,
            DynamicScheduledThreadConfig.RUN_INTERVAL_PROPERTY_KEY);
        _runIntervalProperty = config.properties().getIntProperty(propertyKey, config.runIntervalRange());
    }

    @Override
    public final void run() {
        int initdelay = _initDelayProperty.getValue();
        if (initdelay > 0)
            ThreadExtension.sleep(initdelay);

        while (!this.isInterrupted()) {
            if (_isShutdown.get())
                return;

            Loops.executeWithoutTightLoop(() -> {
                try {
                    _runnable.run();
                } catch (Throwable ex) {
                    _logger.error("failed to run scheduled runnable", ex);
                }

                if (_isShutdown.get())
                    return;

                int runInterval = _runIntervalProperty.getValue();
                if (runInterval > 0)
                    ThreadExtension.sleep(runInterval);
            });
        }
    }

    public void shutdown() {
        _isShutdown.set(true);
    }

}
