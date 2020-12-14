package org.mydotey.artemis.management.group.util;

import com.google.common.collect.Lists;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;
import org.mydotey.artemis.management.group.GroupOperations;
import org.mydotey.artemis.management.group.model.GroupOperationLogModel;
import org.mydotey.artemis.management.group.model.GroupOperationModel;
import org.mydotey.java.StringExtension;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GroupOperationsUtil {
    public static List<GroupOperationModel> newGroupOperationModels(GroupOperations groupOperations) {
        List<GroupOperationModel> operations = Lists.newArrayList();
        if (groupOperations == null || groupOperations.getGroupId() == null
            || CollectionExtension.isEmpty(groupOperations.getOperations())) {
            return operations;
        }
        Long groupId = groupOperations.getGroupId();

        for (String operation : groupOperations.getOperations()) {
            if (StringExtension.isBlank(operation)) {
                continue;
            }

            operations.add(new GroupOperationModel(groupId, operation));
        }

        return operations;
    }

    public static List<GroupOperationModel> newGroupOperationModels(List<GroupOperations> groupOperationsList) {
        List<GroupOperationModel> operations = Lists.newArrayList();
        if (CollectionExtension.isEmpty(groupOperationsList)) {
            return operations;
        }

        for (GroupOperations groupOperations : groupOperationsList) {
            if (groupOperations == null) {
                continue;
            }

            operations.addAll(newGroupOperationModels(groupOperations));
        }

        return operations;
    }

    public static List<GroupOperationLogModel> newGroupOperationLogModels(OperationContext operationContext,
        List<GroupOperationModel> groupOperations, boolean complete) {
        List<GroupOperationLogModel> logs = Lists.newArrayList();
        if (operationContext == null || CollectionExtension.isEmpty(groupOperations)) {
            return logs;
        }
        for (GroupOperationModel groupOperation : groupOperations) {
            logs.add(new GroupOperationLogModel(groupOperation, operationContext, complete));
        }
        return logs;
    }
}
