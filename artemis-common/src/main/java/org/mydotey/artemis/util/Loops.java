package org.mydotey.artemis.util;

import java.util.function.Supplier;

import org.mydotey.java.ObjectExtension;
import org.mydotey.java.ThreadExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public final class Loops {

    private static final Logger _logger = LoggerFactory.getLogger(Loops.class);

    private static final int DEFAULT_SLEEP_NANOS_IN_TIGHT_LOOP = 10 * 1000;

    private Loops() {

    }

    public static <V> V executeWithoutTightLoop(Supplier<V> func) {
        return executeWithoutTightLoop(func, 0, DEFAULT_SLEEP_NANOS_IN_TIGHT_LOOP);
    }

    public static <V> V executeWithoutTightLoop(Supplier<V> func, int ms, int nanos) {
        ObjectExtension.requireNonNull(func, "func");

        long startTime = System.currentTimeMillis();
        try {
            return func.get();
        } finally {
            if (System.currentTimeMillis() - startTime <= 0)
                preventTightLoop(ms, nanos);
        }
    }

    public static void executeWithoutTightLoop(Runnable action) {
        executeWithoutTightLoop(action, 0, DEFAULT_SLEEP_NANOS_IN_TIGHT_LOOP);
    }

    public static void executeWithoutTightLoop(Runnable action, int ms, int nanos) {
        ObjectExtension.requireNonNull(action, "action");

        long startTime = System.currentTimeMillis();
        try {
            action.run();
        } finally {
            if (System.currentTimeMillis() - startTime <= 0)
                preventTightLoop(ms, nanos);
        }
    }

    public static void preventTightLoop() {
        preventTightLoop(0, DEFAULT_SLEEP_NANOS_IN_TIGHT_LOOP);
    }

    public static void preventTightLoop(int ms, int nanos) {
        _logger.info("Sleep {} ms & {} nanos to prevent tight loop.", ms, nanos);
        ThreadExtension.sleep(ms, nanos);
    }

}
