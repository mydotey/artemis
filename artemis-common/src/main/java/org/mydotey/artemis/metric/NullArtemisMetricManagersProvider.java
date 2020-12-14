package org.mydotey.artemis.metric;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class NullArtemisMetricManagersProvider implements ArtemisMetricManagersProvider {

    public static final NullArtemisMetricManagersProvider INSTANCE = new NullArtemisMetricManagersProvider();

    private NullArtemisMetricManagersProvider() {

    }

    @Override
    public EventMetricManager getEventMetricManager() {
        return NullEventMetricManager.INSTANCE;
    }

    @Override
    public AuditMetricManager getValueMetricManager() {
        return NullAuditMetricManager.INSTANCE;
    }

    @Override
    public StatusMetricManager<Double> getStatusMetricManager() {
        return NullStatusMetricManager.DOUBLE_NULL_STATUS_METRIC_MANAGER;
    }
}
