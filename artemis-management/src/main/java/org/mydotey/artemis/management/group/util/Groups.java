package org.mydotey.artemis.management.group.util;

import com.google.common.collect.Lists;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;
import org.mydotey.artemis.management.group.Group;
import org.mydotey.artemis.management.group.model.GroupLogModel;
import org.mydotey.artemis.management.group.model.GroupModel;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class Groups {
    public static Group newGroup(GroupModel model) {
        return new Group(model.getId(), model.getServiceId(), model.getRegionId(), model.getZoneId(), model.getName(),
            model.getAppId(),
            model.getDescription(), model.getStatus(), null);
    }

    public static GroupModel newGroupModel(Group group) {
        return new GroupModel(group.getServiceId(), group.getRegionId(), group.getZoneId(), group.getName(),
            group.getAppId(),
            group.getDescription(), group.getStatus());
    }

    public static List<GroupModel> newGroupModels(List<Group> groups) {
        return Converts.convert(groups, group -> newGroupModel(group));
    }

    public static List<Group> newGroups(List<GroupModel> models) {
        return Converts.convert(models, model -> newGroup(model));
    }

    public static List<GroupLogModel> newGroupLogModels(OperationContext operationContext, List<GroupModel> groups) {
        List<GroupLogModel> logs = Lists.newArrayList();
        if (operationContext == null || CollectionExtension.isEmpty(groups)) {
            return logs;
        }
        for (GroupModel group : groups) {
            if (group == null) {
                continue;
            }
            logs.add(new GroupLogModel(group, operationContext));
        }

        return logs;
    }
}
