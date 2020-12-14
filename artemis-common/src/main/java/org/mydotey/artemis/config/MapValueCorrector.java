package org.mydotey.artemis.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.mydotey.java.ObjectExtension;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class MapValueCorrector<K, V> implements Function<Map<K, V>, Map<K, V>> {

    private Function<V, V> _valueCorrector;

    public MapValueCorrector(Function<V, V> valueCorrector) {
        ObjectExtension.requireNonNull(valueCorrector, "valueCorrector");
        _valueCorrector = valueCorrector;
    }

    @Override
    public Map<K, V> apply(Map<K, V> value) {
        if (value == null)
            return null;

        Map<K, V> result = new HashMap<>();
        for (Map.Entry<K, V> item : value.entrySet()) {
            result.put(item.getKey(), _valueCorrector.apply(item.getValue()));
        }

        return result;
    }

}
