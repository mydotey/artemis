package org.mydotey.artemis.metric;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public interface MetricReporter<T extends Metric> {

    void report(T metric);

}
