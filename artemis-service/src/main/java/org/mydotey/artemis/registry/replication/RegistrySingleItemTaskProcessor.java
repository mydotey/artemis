package org.mydotey.artemis.registry.replication;

import java.util.List;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.taskdispatcher.ProcessingResult;
import org.mydotey.artemis.taskdispatcher.TaskErrorCode;
import org.mydotey.artemis.taskdispatcher.TaskProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class RegistrySingleItemTaskProcessor implements TaskProcessor<RegistryReplicationTask, RegistryReplicationTask> {

    private static final Logger _logger = LoggerFactory.getLogger(RegistrySingleItemTaskProcessor.class);

    @Override
    public ProcessingResult<RegistryReplicationTask> process(RegistryReplicationTask work) {
        try {
            Class<?> clazz = work.getClass();
            if (!RegistryReplicationTool.supportedSubTaskTypes().contains(clazz)) {
                String errorMessage = "Unsupported task: " + clazz;
                _logger.error(errorMessage);
                return new ProcessingResult<>(Lists.newArrayList(RegistryReplicationTool.toFailedTask(work, TaskErrorCode.PermanentFail)));
            }

            List<RegistryReplicationTask> failedTasks = RegistryReplicationTool.replicate(clazz, work.serviceUrl(), Lists.newArrayList(work.instance()),
                    ImmutableMap.<Instance, Long> of(work.instance(), work.expiryTime()));
            return new ProcessingResult<>(failedTasks);

        } catch (Throwable ex) {
            _logger.error("Replication failed. Maybe a bug. Task: " + work.taskId(), ex);
            return new ProcessingResult<>(Lists.newArrayList(RegistryReplicationTool.toFailedTask(work, TaskErrorCode.PermanentFail)));
        }
    }

}
