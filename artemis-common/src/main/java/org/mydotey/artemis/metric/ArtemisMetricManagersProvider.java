package org.mydotey.artemis.metric;

import org.mydotey.caravan.util.metric.AuditMetricManager;
import org.mydotey.caravan.util.metric.EventMetricManager;
import org.mydotey.caravan.util.metric.StatusMetricManager;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public interface ArtemisMetricManagersProvider {

    EventMetricManager getEventMetricManager();

    AuditMetricManager getValueMetricManager();

    StatusMetricManager<Double> getStatusMetricManager();
}
