package org.mydotey.artemis.trace;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class TraceExecutor {

    private TraceFactory _factory;
    private ThreadLocal<Trace> _currentTrace;

    public TraceExecutor(TraceFactory factory) {
        Objects.requireNonNull(factory, "factory");
        _factory = factory;
        _currentTrace = new ThreadLocal<>();
    }

    public <V> V execute(String traceKey, Supplier<V> executor) {
        Trace trace = _factory.newTrace(traceKey);
        _currentTrace.set(trace);
        try {
            trace.start();

            V value = executor.get();

            trace.markSuccess();
            return value;
        } catch (Throwable ex) {
            trace.markFail(ex);
            throw ex;
        } finally {
            trace.end();
            _currentTrace.remove();
        }
    }

    public void execute(String traceKey, Runnable executor) {
        Trace trace = _factory.newTrace(traceKey);
        _currentTrace.set(trace);
        try {
            trace.start();

            executor.run();

            trace.markSuccess();
        } catch (Throwable ex) {
            trace.markFail(ex);
            throw ex;
        } finally {
            trace.end();
            _currentTrace.remove();
        }
    }

    public void markEvent(String message) {
        Trace trace = _currentTrace.get();
        if (trace == null)
            return;

        trace.markEvent(message);
    }

    public void markEvent(String message, Map<String, String> data) {
        Trace trace = _currentTrace.get();
        if (trace == null)
            return;

        trace.markEvent(message, data);
    }

}
