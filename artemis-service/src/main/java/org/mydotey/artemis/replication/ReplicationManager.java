package org.mydotey.artemis.replication;

import java.util.List;

import org.mydotey.artemis.taskdispatcher.TaskDispatcher;
import org.mydotey.artemis.taskdispatcher.TaskDispatchers;
import org.mydotey.artemis.taskdispatcher.TaskProcessor;
import org.mydotey.java.ObjectExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ReplicationManager<T extends ReplicationTask> {

    private static final String NON_BATCHING_KEY = "non-batching";
    private static final String BATCHING_KEY = "batching";

    private String _managerId;

    private TaskDispatcher<T> _singleItemTaskDispatcher;
    private TaskDispatcher<T> _batchingTaskDispatcher;

    public ReplicationManager(String managerId, TaskProcessor<T, T> singleItemTaskProcessor,
        TaskProcessor<T, List<T>> batchingTaskProcessor) {
        ObjectExtension.requireNonBlank(managerId, "managerId");
        ObjectExtension.requireNonNull(singleItemTaskProcessor, "singleItemTaskProcessor");
        ObjectExtension.requireNonNull(batchingTaskProcessor, "batchingTaskProcessor");

        _managerId = managerId;

        _singleItemTaskDispatcher = TaskDispatchers.newSingleItemTaskDispatcher(_managerId + "." + NON_BATCHING_KEY,
            singleItemTaskProcessor);
        _batchingTaskDispatcher = TaskDispatchers.newBatchingTaskDispatcher(_managerId + "." + BATCHING_KEY,
            batchingTaskProcessor);
    }

    public String managerId() {
        return _managerId;
    }

    public void replicate(T task) {
        if (task.batchingEnabled()) {
            _batchingTaskDispatcher.process(task);
            return;
        }

        _singleItemTaskDispatcher.process(task);
    }

}
