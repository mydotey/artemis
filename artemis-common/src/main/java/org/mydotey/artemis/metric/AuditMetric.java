package org.mydotey.artemis.metric;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public interface AuditMetric extends Metric {

    void addValue(double value);

    long getCount();

    long getCountInRange(long lowerBound, long upperBound);

    long getPercentile(double percent);

    AuditData getAuditData();

}
