package org.mydotey.artemis.management.group.util;

import com.google.common.collect.Lists;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;
import org.mydotey.artemis.management.group.GroupInstance;
import org.mydotey.artemis.management.group.model.GroupInstanceLogModel;
import org.mydotey.artemis.management.group.model.GroupInstanceModel;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GroupInstances {
    public static GroupInstance newGroupInstance(GroupInstanceModel model) {
        return new GroupInstance(model.getId(), model.getGroupId(), model.getInstanceId());
    }

    public static GroupInstanceModel newGroupInstanceModel(GroupInstance groupInstance) {
        return new GroupInstanceModel(groupInstance.getGroupId(), groupInstance.getInstanceId());
    }

    public static List<GroupInstanceModel> newGroupInstanceModels(List<GroupInstance> groupInstances) {
        return Converts.convert(groupInstances, groupInstance -> newGroupInstanceModel(groupInstance));
    }

    public static List<GroupInstance> newGroupInstances(List<GroupInstanceModel> models) {
        return Converts.convert(models, model -> newGroupInstance(model));
    }

    public static List<GroupInstanceLogModel> newGroupLogModels(OperationContext operationContext,
        List<GroupInstanceModel> groupInstances) {
        List<GroupInstanceLogModel> logs = Lists.newArrayList();
        if (operationContext == null || CollectionExtension.isEmpty(groupInstances)) {
            return logs;
        }
        for (GroupInstanceModel groupInstance : groupInstances) {
            if (groupInstance == null) {
                continue;
            }
            logs.add(new GroupInstanceLogModel(operationContext, groupInstance));
        }

        return logs;
    }

}
