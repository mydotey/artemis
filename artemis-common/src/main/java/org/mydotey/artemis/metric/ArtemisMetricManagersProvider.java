package org.mydotey.artemis.metric;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public interface ArtemisMetricManagersProvider {

    EventMetricManager getEventMetricManager();

    AuditMetricManager getValueMetricManager();

    StatusMetricManager<Double> getStatusMetricManager();
}
