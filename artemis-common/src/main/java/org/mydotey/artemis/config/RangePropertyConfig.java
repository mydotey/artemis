package org.mydotey.artemis.config;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class RangePropertyConfig<T extends Comparable<T>> {

    private T _defaultValue;
    private T _lowerBound;
    private T _upperBound;

    public RangePropertyConfig(T defaultValue, T lowerBound, T upperBound) {
        _defaultValue = defaultValue;
        _lowerBound = lowerBound;
        _upperBound = upperBound;
    }

    public T defaultValue() {
        return _defaultValue;
    }

    public T lowerBound() {
        return _lowerBound;
    }

    public T upperBound() {
        return _upperBound;
    }

    public RangeValueFilter<T> toValueFilter() {
        return new RangeValueFilter<T>(_lowerBound, _upperBound);
    }

}
