package org.mydotey.artemis.taskdispatcher;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.artemis.metric.ArtemisMetricManagers;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.caravan.util.metric.EventMetric;
import org.mydotey.caravan.util.metric.MetricConfig;
import org.mydotey.caravan.util.metric.MetricNames;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.Loops;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.ThreadExtension;
import org.mydotey.java.collection.MultiWriteBatchReadList;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class TaskAcceptor<T extends Task, W> {

    private static final String IDENTITY_FORMAT = ".task-acceptor";

    private static final Logger _logger = LoggerFactory.getLogger(TaskAcceptor.class);

    protected Property<String, Integer> _maxBufferSizeProperty;
    protected Property<String, Integer> _writeCompleteWaitProperty;
    protected Property<String, Integer> _acceptListInitCapacityProperty;
    protected Property<String, Integer> _reacceptListInitCapacityProperty;

    private AtomicBoolean _isShutdown = new AtomicBoolean(false);
    private Thread _acceptorThread;

    private volatile MultiWriteBatchReadList<T> _acceptList;
    private volatile MultiWriteBatchReadList<T> _reacceptList;

    protected Map<String, T> _acceptedTasks = new HashMap<>();
    protected Deque<String> _processingOrder = new LinkedList<>();

    protected BlockingQueue<W> _workQueue = new LinkedBlockingQueue<>();

    protected AtomicInteger _pendingTaskCount = new AtomicInteger();

    private TrafficShaper _trafficShaper;

    protected final String _acceptorId;

    private String _assignWorkTraceKey;
    private String _drainAcceptTraceKey;

    protected String _workStatusEventType;
    protected EventMetric _workStatusEventMetric;
    protected String _pollWorkResultEventType;
    protected EventMetric _pollWorkResultEventMetric;
    protected String _taskStatusEventType;
    protected EventMetric _taskStatusEventMetric;
    private String _drainAcceptTaskTypeEventType;
    protected EventMetric _drainAcceptTaskTypeEventMetric;

    public TaskAcceptor(String dispatcherId) {
        ObjectExtension.requireNonBlank(dispatcherId, "dispatcherId");
        _acceptorId = dispatcherId + IDENTITY_FORMAT;

        _assignWorkTraceKey = _acceptorId + ".assign-work";
        _drainAcceptTraceKey = _acceptorId + ".drain-accept";

        _workStatusEventType = _acceptorId + ".work-status";
        _workStatusEventMetric = ArtemisMetricManagers.DEFAULT.eventMetricManager().getMetric(_workStatusEventType,
            new MetricConfig(ImmutableMap.of(MetricNames.METRIC_NAME_KEY_DISTRIBUTION, _workStatusEventType)));
        _pollWorkResultEventType = _acceptorId + ".poll-work-result";
        _pollWorkResultEventMetric = ArtemisMetricManagers.DEFAULT.eventMetricManager().getMetric(
            _pollWorkResultEventType,
            new MetricConfig(ImmutableMap.of(MetricNames.METRIC_NAME_KEY_DISTRIBUTION, _pollWorkResultEventType)));
        _taskStatusEventType = _acceptorId + ".task-status";
        _taskStatusEventMetric = ArtemisMetricManagers.DEFAULT.eventMetricManager().getMetric(_taskStatusEventType,
            new MetricConfig(ImmutableMap.of(MetricNames.METRIC_NAME_KEY_DISTRIBUTION, _taskStatusEventType)));
        _drainAcceptTaskTypeEventType = _acceptorId + ".task-Type";
        _drainAcceptTaskTypeEventMetric = ArtemisMetricManagers.DEFAULT.eventMetricManager().getMetric(
            _drainAcceptTaskTypeEventType,
            new MetricConfig(ImmutableMap.of(MetricNames.METRIC_NAME_KEY_DISTRIBUTION, _drainAcceptTaskTypeEventType)));

        _maxBufferSizeProperty = ArtemisConfig.properties().getIntProperty(_acceptorId + ".max-buffer-size", 10 * 1000,
            new RangeValueFilter<>(100, 100 * 1000));
        _writeCompleteWaitProperty = ArtemisConfig.properties().getIntProperty(_acceptorId + ".write-complete-wait", 5,
            new RangeValueFilter<>(0, 200));
        _acceptListInitCapacityProperty = ArtemisConfig.properties()
            .getIntProperty(_acceptorId + ".accept-list.init-capacity", 10 * 1000,
                new RangeValueFilter<>(0, 100 * 1000));
        _reacceptListInitCapacityProperty = ArtemisConfig.properties()
            .getIntProperty(_acceptorId + ".reaccept-list.init-capacity", 1000, new RangeValueFilter<>(0, 100 * 1000));

        resetAcceptList();

        _trafficShaper = new TrafficShaper(dispatcherId);

        _acceptorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                loopAssign();
            }
        });
        _acceptorThread.setDaemon(true);
        _acceptorThread.start();
    }

    public void accept(T task) {
        _acceptList.add(task);
    }

    public void reaccept(T task) {
        _reacceptList.add(task);
        _trafficShaper.markFail(task.errorCode());
    }

    public void shutdown() {
        if (_isShutdown.compareAndSet(false, true)) {
            _acceptorThread.interrupt();
        }
    }

    public W pollWork() {
        while (true) {
            try {
                if (_isShutdown.get())
                    throw new RuntimeException("TaskAcceptor has been shutdown.");

                W work = _workQueue.poll(1, TimeUnit.SECONDS);
                if (work == null) {
                    ArtemisTraceExecutor.INSTANCE.markEvent(_pollWorkResultEventType, "no-work");
                    _pollWorkResultEventMetric.addEvent("no-work");
                    continue;
                }

                _pendingTaskCount.addAndGet(-getWorkSize(work));

                work = filterExpiredTask(work);
                if (isEmptyWork(work)) {
                    ArtemisTraceExecutor.INSTANCE.markEvent(_pollWorkResultEventType, "work-expired");
                    _pollWorkResultEventMetric.addEvent("work-expired");
                    continue;
                }

                int delay = _trafficShaper.transmissionDelay();
                if (delay > 0) {
                    ArtemisTraceExecutor.INSTANCE.markEvent(_pollWorkResultEventType, "traffic-shaped");
                    _pollWorkResultEventMetric.addEvent("traffic-shaped");
                    ThreadExtension.sleep(delay);
                }

                ArtemisTraceExecutor.INSTANCE.markEvent(_pollWorkResultEventType, "work-available");
                _pollWorkResultEventMetric.addEvent("work-available");
                return work;
            } catch (RuntimeException | Error ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    protected abstract int getWorkSize(W work);

    protected abstract boolean isEmptyWork(W work);

    protected abstract W filterExpiredTask(W work);

    protected abstract void assignWork();

    protected boolean isExpiredTask(T task) {
        return task.expiryTime() <= System.currentTimeMillis();
    }

    protected boolean isBufferFull() {
        return _pendingTaskCount.get() >= _maxBufferSizeProperty.getValue().intValue();
    }

    private void loopAssign() {
        while (true) {
            if (_isShutdown.get())
                break;

            Loops.executeWithoutTightLoop(() -> {
                try {
                    final MultiWriteBatchReadList<T> acceptList = _acceptList;
                    final MultiWriteBatchReadList<T> reacceptList = _reacceptList;
                    resetAcceptList();
                    ThreadExtension.sleep(_writeCompleteWaitProperty.getValue().intValue());

                    ArtemisTraceExecutor.INSTANCE.execute(_drainAcceptTraceKey,
                        () -> drainAccept(acceptList.getAll(), reacceptList.getAll()));
                    ArtemisTraceExecutor.INSTANCE.execute(_assignWorkTraceKey, () -> assignWork());
                } catch (Throwable ex) {
                    _logger.error("Assign work failed.", ex);
                }
            });
        }
    }

    private void drainAccept(List<T> acceptList, List<T> reacceptList) {
        for (T task : acceptList) {
            T previousTask = _acceptedTasks.put(task.taskId(), task);
            if (previousTask == null) {
                _processingOrder.add(task.taskId());
                ArtemisTraceExecutor.INSTANCE.markEvent(_drainAcceptTaskTypeEventType, "accept-new");
                _drainAcceptTaskTypeEventMetric.addEvent("accept-new");
                continue;
            }

            task.resetSubmitTime(previousTask.submitTime());
            ArtemisTraceExecutor.INSTANCE.markEvent(_drainAcceptTaskTypeEventType, "accept-replace");
            _drainAcceptTaskTypeEventMetric.addEvent("accept-replace");
        }

        for (T task : Lists.reverse(reacceptList)) {
            if (_acceptedTasks.containsKey(task.taskId())) {
                ArtemisTraceExecutor.INSTANCE.markEvent(_drainAcceptTaskTypeEventType, "reaccept-drop");
                _drainAcceptTaskTypeEventMetric.addEvent("reaccept-drop");
                continue;
            }

            String taskId = _processingOrder.peek();
            if (taskId != null) {
                T oldestTask = _acceptedTasks.get(taskId);
                if (oldestTask != null)
                    task.resetSubmitTime(oldestTask.submitTime() - 1);
            }
            _acceptedTasks.put(task.taskId(), task);
            _processingOrder.addFirst(task.taskId());
            ArtemisTraceExecutor.INSTANCE.markEvent(_drainAcceptTaskTypeEventType, "reaccept-new");
            _drainAcceptTaskTypeEventMetric.addEvent("reaccept-new");
        }
    }

    private void resetAcceptList() {
        _acceptList = new MultiWriteBatchReadList<>(_acceptListInitCapacityProperty.getValue().intValue());
        _reacceptList = new MultiWriteBatchReadList<>(_reacceptListInitCapacityProperty.getValue().intValue());
    }

}
