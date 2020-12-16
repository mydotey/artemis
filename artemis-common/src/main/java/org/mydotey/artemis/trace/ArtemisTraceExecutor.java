package org.mydotey.artemis.trace;

import org.mydotey.caravan.util.trace.TraceExecutor;

import com.google.common.collect.ImmutableMap;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ArtemisTraceExecutor extends TraceExecutor {

    public static final ArtemisTraceExecutor INSTANCE = new ArtemisTraceExecutor();

    private ArtemisTraceExecutor() {
        super(ArtemisTraceFactory.INSTANCE);
    }

    public void markEvent(String eventType, String eventName) {
        markEvent(null, ImmutableMap.of("event_type", eventType, "event_name", eventName));
    }

}
