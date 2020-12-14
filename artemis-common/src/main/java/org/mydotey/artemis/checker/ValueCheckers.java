package org.mydotey.artemis.checker;

import java.util.Collection;
import java.util.Map;

import org.mydotey.java.ObjectExtension;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 2017/3/1.
 */
public class ValueCheckers {

    public static void notNull(Object value, String valueName) {
        ObjectExtension.requireNonNull(value, valueName);
    }

    public static void notNullOrWhiteSpace(String value, String valueName) {
        ObjectExtension.requireNonBlank(value, valueName);
    }

    public static void notNullOrEmpty(Map<?, ?> value, String valueName) {
        ObjectExtension.requireNonEmpty(value, valueName);
    }

    public static void notNullOrEmpty(Collection<?> value, String valueName) {
        ObjectExtension.requireNonEmpty(value, valueName);
    }

    public static <T> void notNullOrEmpty(T[] value, String valueName) {
        if (CollectionExtension.isEmpty(value))
            throw new IllegalArgumentException("argument " + valueName + " is null or empty");
    }

}
