package org.mydotey.artemis.config;

import java.util.function.Function;

import org.mydotey.java.ObjectExtension;

public class DefaultValueFilter<T> implements Function<T, T> {

    private T _defaultValue;

    public DefaultValueFilter(T defaultValue) {
        ObjectExtension.requireNonNull(defaultValue, "defaultValue");
        _defaultValue = defaultValue;
    }

    @Override
    public T apply(T t) {
        return t == null || t instanceof Number && ((Number) t).doubleValue() == 0.0 ? _defaultValue : t;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_defaultValue == null) ? 0 : _defaultValue.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultValueFilter other = (DefaultValueFilter) obj;
        if (_defaultValue == null) {
            if (other._defaultValue != null)
                return false;
        } else if (!_defaultValue.equals(other._defaultValue))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DefaultValueFilter [_defaultValue=" + _defaultValue + "]";
    }

}
