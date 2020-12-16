package org.mydotey.artemis.taskdispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.artemis.metric.ArtemisMetricManagers;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.caravan.util.metric.AuditMetric;
import org.mydotey.caravan.util.metric.MetricConfig;
import org.mydotey.caravan.util.metric.MetricNames;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.java.collection.CollectionExtension;
import org.mydotey.scf.Property;

import com.google.common.collect.ImmutableMap;

class BatchingTaskAcceptor<T extends Task> extends TaskAcceptor<T, List<T>> {

    private Property<String, Integer> _maxBatchingSizeProperty;
    private Property<String, Integer> _maxBatchingDelayProperty;
    private final AuditMetric _batchingSizeAuditMetric;
    private AtomicInteger _pendingTaskCount = new AtomicInteger();

    public BatchingTaskAcceptor(String dispatcherId) {
        super(dispatcherId);

        _maxBatchingSizeProperty = ArtemisConfig.properties().getIntProperty(_acceptorId + ".max-batching-size", 250,
            new RangeValueFilter<>(10, 10 * 1000));
        _maxBatchingDelayProperty = ArtemisConfig.properties().getIntProperty(_acceptorId + ".max-batching-delay",
            2 * 1000, new RangeValueFilter<>(1000, 10 * 1000));

        final String batchingSizeMetricName = _acceptorId + ".batching-size.distribution";
        _batchingSizeAuditMetric = ArtemisMetricManagers.DEFAULT.valueMetricManager().getMetric(batchingSizeMetricName,
            new MetricConfig(ImmutableMap.of(MetricNames.METRIC_NAME_KEY_DISTRIBUTION, batchingSizeMetricName)));
    }

    @Override
    protected boolean isEmptyWork(List<T> work) {
        return CollectionExtension.isEmpty(work);
    }

    @Override
    protected List<T> filterExpiredTask(List<T> work) {
        List<T> result = new ArrayList<>();
        for (T task : work) {
            if (isExpiredTask(task)) {
                ArtemisTraceExecutor.INSTANCE.markEvent(_taskStatusEventType, "expired");
                _taskStatusEventMetric.addEvent("expired");
                continue;
            }

            result.add(task);
            ArtemisTraceExecutor.INSTANCE.markEvent(_taskStatusEventType, "normal");
            _taskStatusEventMetric.addEvent("normal");
        }

        _batchingSizeAuditMetric.addValue(result.size());

        return result;
    }

    @Override
    protected int getWorkSize(List<T> w) {
        return w.size();
    }

    @Override
    protected void assignWork() {
        while (hasEnoughTasks()) {
            if (isBufferFull()) {
                List<T> tasks = _workQueue.poll();
                _pendingTaskCount.addAndGet(-tasks.size());
                ArtemisTraceExecutor.INSTANCE.markEvent(_workStatusEventType, "buffer-full-dropped");
                _workStatusEventMetric.addEvent("buffer-full-dropped");
            }

            List<T> work = generateWork();
            _workQueue.add(work);
            _pendingTaskCount.addAndGet(work.size());
            ArtemisTraceExecutor.INSTANCE.markEvent(_workStatusEventType, "normal");
            _workStatusEventMetric.addEvent("normal");
        }
    }

    private boolean hasEnoughTasks() {
        if (_processingOrder.size() >= _maxBatchingSizeProperty.getValue().intValue())
            return true;

        if (_processingOrder.isEmpty())
            return false;

        String taskId = _processingOrder.peek();
        T task = _acceptedTasks.get(taskId);

        long delay = System.currentTimeMillis() - task.submitTime();
        return delay >= _maxBatchingDelayProperty.getValue().intValue();
    }

    private List<T> generateWork() {
        List<T> tasks = new ArrayList<>();
        while (tasks.size() < _maxBatchingSizeProperty.getValue().intValue()) {
            if (_processingOrder.isEmpty())
                break;

            String taskId = _processingOrder.poll();
            T task = _acceptedTasks.remove(taskId);
            tasks.add(task);
        }

        return tasks;
    }

}
