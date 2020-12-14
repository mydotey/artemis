package org.mydotey.artemis.management.group.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;

import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 2017/3/20.
 */
class Converts {
    public static <T, V> List<V> convert(List<T> values, Function<T, V> converter) {
        List<V> res = Lists.newArrayList();
        if (CollectionExtension.isEmpty(values)) {
            return res;
        }
        for (T value : values) {
            if (value == null) {
                continue;
            }
            res.add(converter.apply(value));
        }
        return res;
    }
}
