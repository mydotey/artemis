package org.mydotey.artemis.taskdispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.artemis.config.RangeValueFilter;
import org.mydotey.artemis.metric.ArtemisMetricManagers;
import org.mydotey.artemis.metric.EventMetric;
import org.mydotey.artemis.metric.MetricConfig;
import org.mydotey.artemis.metric.MetricNames;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.Loops;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.collection.CollectionExtension;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

class TaskExecutor<T extends Task, W> {

    private static final String IDENTITY_FORMAT = ".task-executor";

    private static final Logger _logger = LoggerFactory.getLogger(TaskExecutor.class);

    private String _executorId;

    private Property<String, Integer> _threadCountProperty;
    private List<Thread> _workerThreadExtension = new ArrayList<>();

    protected AtomicBoolean _isShutdown = new AtomicBoolean();

    private TaskAcceptor<T, W> _taskAcceptor;
    private TaskProcessor<T, W> _taskProcessor;

    private String _executeTraceKey;
    private String _executeFailedTaskEventType;
    private EventMetric _executeFailedTaskEventMetric;

    public TaskExecutor(String dispatcherId, TaskAcceptor<T, W> taskAcceptor, TaskProcessor<T, W> taskProcessor) {
        ObjectExtension.requireNonBlank(dispatcherId, "dispatcherId");
        ObjectExtension.requireNonNull(taskAcceptor, "acceptorExecutor");
        ObjectExtension.requireNonNull(taskProcessor, "taskProcessor");

        _executorId = dispatcherId + IDENTITY_FORMAT;
        _taskAcceptor = taskAcceptor;
        _taskProcessor = taskProcessor;

        _executeTraceKey = _executorId + ".execute";
        _executeFailedTaskEventType = _executeTraceKey + ".failed-task";
        _executeFailedTaskEventMetric = ArtemisMetricManagers.DEFAULT.eventMetricManager().getMetric(
            _executeFailedTaskEventType,
            new MetricConfig(ImmutableMap.of(MetricNames.METRIC_NAME_KEY_DISTRIBUTION, _executeFailedTaskEventType)));

        _threadCountProperty = ArtemisConfig.properties().getIntProperty(_executorId + ".thread-count", 20,
            new RangeValueFilter<>(1, 100));

        Runnable executiontask = new Runnable() {
            @Override
            public void run() {
                loopExecute();
            }
        };

        for (int i = 0; i < _threadCountProperty.getValue().intValue(); i++) {
            Thread workerThread = new Thread(executiontask);
            _workerThreadExtension.add(workerThread);
            workerThread.setDaemon(true);
            workerThread.start();
        }
    }

    public void shutdown() {
        if (!_isShutdown.compareAndSet(false, true))
            return;

        for (Thread workerThread : _workerThreadExtension)
            workerThread.interrupt();
    }

    private void loopExecute() {
        while (true) {
            if (_isShutdown.get())
                break;

            Loops.executeWithoutTightLoop(() -> {
                try {
                    final W work = _taskAcceptor.pollWork();
                    ArtemisTraceExecutor.INSTANCE.execute(_executeTraceKey, () -> TaskExecutor.this.execute(work));
                } catch (Throwable ex) {
                    _logger.error("Execute task error.", ex);
                }
            });
        }
    }

    private void execute(W work) {
        ProcessingResult<T> result = _taskProcessor.process(work);
        if (CollectionExtension.isEmpty(result.failedTasks()))
            return;

        for (T task : result.failedTasks()) {
            String taskErrorCode = task.errorCode().toString();
            ArtemisTraceExecutor.INSTANCE.markEvent(_executeFailedTaskEventType, taskErrorCode);
            _executeFailedTaskEventMetric.addEvent(taskErrorCode);

            if (TaskErrorCode.RERUNNABLE_ERROR_CODES.contains(task.errorCode())) {
                _taskAcceptor.reaccept(task);
                return;
            }

            _logger.warn("Discarding a task due to non-rerunnable error. Task ErrorCode: {}", task.errorCode());
        }
    }

}
