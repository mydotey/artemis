package org.mydotey.artemis.checker;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public interface ValueChecker<T> {

    void check(T value, String valueName);

}
