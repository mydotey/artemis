package org.mydotey.artemis.management.group.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

import org.mydotey.artemis.management.group.GroupTags;
import org.mydotey.artemis.management.group.model.GroupTagModel;
import org.mydotey.java.StringExtension;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GroupTagsUtil {
    public static List<GroupTagModel> newGroupTags(GroupTags groupTags) {
        List<GroupTagModel> tags = Lists.newArrayList();
        if (groupTags == null || groupTags.getGroupId() == null || CollectionExtension.isEmpty(groupTags.getTags())) {
            return tags;
        }
        Long groupId = groupTags.getGroupId();

        for (Map.Entry<String, String> entry : groupTags.getTags().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringExtension.isBlank(key) || StringExtension.isBlank(value)) {
                continue;
            }
            tags.add(new GroupTagModel(groupId, key, value));
        }

        return tags;
    }

    public static List<GroupTagModel> newGroupTags(List<GroupTags> groupTagsList) {
        List<GroupTagModel> tags = Lists.newArrayList();
        if (CollectionExtension.isEmpty(groupTagsList)) {
            return tags;
        }

        for (GroupTags groupTags : groupTagsList) {
            if (groupTags == null) {
                continue;
            }

            tags.addAll(newGroupTags(groupTags));
        }

        return tags;
    }
}
