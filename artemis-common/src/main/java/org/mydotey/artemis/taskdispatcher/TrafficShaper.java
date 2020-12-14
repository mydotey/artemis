
package org.mydotey.artemis.taskdispatcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.artemis.config.MapValueConverter;
import org.mydotey.artemis.config.MapValueCorrector;
import org.mydotey.java.ObjectExtension;
import org.mydotey.scf.Property;
import org.mydotey.scf.filter.PipelineValueFilter;
import org.mydotey.scf.type.AbstractTypeConverter;
import org.mydotey.scf.type.TypeConverter;
import org.mydotey.scf.type.string.StringToIntConverter;
import org.mydotey.scf.type.string.StringToMapConverter;

class TrafficShaper {

    private static final String IDENTITY_FORMAT = ".traffic-shaper";

    private static final int DEFAULT_FAIL_DELAY = 10;

    private static final int MAX_FAIL_DELAY = 10 * 1000;

    private String _trafficShaperId;

    private TypeConverter<Map<String, String>, Map<TaskErrorCode, String>> _failDelayValueConverter = createValueConverter();
    private TypeConverter<String, Map<TaskErrorCode, Integer>> _failDelayValueParser = createValueParser();
    private Function<Map<TaskErrorCode, Integer>, Map<TaskErrorCode, Integer>> _failDelayValueCorrector = createValueCorrector();
    private Property<String, Map<TaskErrorCode, Integer>> _failDelayProperty;

    private ConcurrentHashMap<TaskErrorCode, AtomicLong> _lastFailTimeMap = new ConcurrentHashMap<>();

    public TrafficShaper(String dispatchId) {
        ObjectExtension.requireNonBlank(dispatchId, "dispatchId");

        _trafficShaperId = dispatchId + IDENTITY_FORMAT;

        _failDelayProperty = ArtemisConfig.properties().getProperty(_trafficShaperId + ".fail-delay", new HashMap<>(),
            _failDelayValueParser, _failDelayValueCorrector);
    }

    public void markFail(TaskErrorCode errorCode) {
        ObjectExtension.requireNonNull(errorCode, "errorCode");

        AtomicLong lastResultTime = _lastFailTimeMap.computeIfAbsent(errorCode, k -> new AtomicLong());
        lastResultTime.set(System.currentTimeMillis());
    }

    public int transmissionDelay() {
        for (TaskErrorCode errorCode : _lastFailTimeMap.keySet()) {
            AtomicLong lastResultTime = _lastFailTimeMap.get(errorCode);
            long time = lastResultTime.get();
            if (time == 0)
                continue;

            int failDelay = getFailDelay(errorCode);
            long delay = System.currentTimeMillis() - time;
            if (delay >= 0 && delay < failDelay)
                return (int) (failDelay - delay);

            lastResultTime.set(0);
        }

        return 0;
    }

    private int getFailDelay(TaskErrorCode errorCode) {
        Map<TaskErrorCode, Integer> getValue = _failDelayProperty.getValue();
        Integer delay = getValue.get(errorCode);
        return delay == null ? 0 : delay.intValue();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TypeConverter<Map<String, String>, Map<TaskErrorCode, String>> createValueConverter() {
        return new AbstractTypeConverter<Map<String, String>, Map<TaskErrorCode, String>>((Class) Map.class,
            (Class) Map.class) {
            @Override
            public Map<TaskErrorCode, String> convert(Map<String, String> source) {
                Map<TaskErrorCode, String> errorCodeMap = new HashMap<TaskErrorCode, String>();
                for (Map.Entry<String, String> item : source.entrySet()) {
                    errorCodeMap.put(TaskErrorCode.valueOf(item.getKey()), item.getValue());
                }

                return errorCodeMap;
            }
        };
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TypeConverter<String, Map<TaskErrorCode, Integer>> createValueParser() {
        return new AbstractTypeConverter<String, Map<TaskErrorCode, Integer>>(String.class, (Class) Map.class) {
            @Override
            public Map<TaskErrorCode, Integer> convert(String value) {
                Map<String, String> mapValue = StringToMapConverter.DEFAULT.convert(value);
                Map<TaskErrorCode, String> mapValue2 = _failDelayValueConverter.convert(mapValue);
                return new MapValueConverter<TaskErrorCode, String, Integer>(StringToIntConverter.DEFAULT)
                    .convert(mapValue2);
            }
        };
    }

    private Function<Map<TaskErrorCode, Integer>, Map<TaskErrorCode, Integer>> createValueCorrector() {
        PipelineValueFilter<Integer> corrector = new PipelineValueFilter<>(Arrays.asList(
            v -> v == null ? DEFAULT_FAIL_DELAY : v,
            v -> v >= 0 && v <= MAX_FAIL_DELAY ? v : null));
        return new PipelineValueFilter<>(Arrays.asList(
            v -> v == null ? new HashMap<TaskErrorCode, Integer>() : v,
            new MapValueCorrector<TaskErrorCode, Integer>(corrector)));
    }

}
