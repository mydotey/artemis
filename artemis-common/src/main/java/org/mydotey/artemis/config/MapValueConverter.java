package org.mydotey.artemis.config;

import java.util.HashMap;
import java.util.Map;

import org.mydotey.scf.type.AbstractTypeConverter;
import org.mydotey.scf.type.TypeConverter;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class MapValueConverter<K, S, D> extends AbstractTypeConverter<Map<K, S>, Map<K, D>> {

    private TypeConverter<S, D> _valueConverter;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MapValueConverter(TypeConverter<S, D> valueConverter) {
        super((Class) Map.class, (Class) Map.class);
        _valueConverter = valueConverter;
    }

    @Override
    public Map<K, D> convert(Map<K, S> source) {
        if (source == null)
            return null;

        Map<K, D> result = new HashMap<>();
        for (Map.Entry<K, S> item : source.entrySet()) {
            result.put(item.getKey(), _valueConverter.convert(item.getValue()));
        }

        return result;
    }

}
