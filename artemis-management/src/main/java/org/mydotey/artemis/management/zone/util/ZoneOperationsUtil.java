package org.mydotey.artemis.management.zone.util;

import com.google.common.collect.Lists;

import java.util.List;

import org.mydotey.artemis.management.common.OperationContext;
import org.mydotey.artemis.management.zone.ZoneKey;
import org.mydotey.artemis.management.zone.ZoneOperations;
import org.mydotey.artemis.management.zone.model.ZoneOperationLogModel;
import org.mydotey.artemis.management.zone.model.ZoneOperationModel;
import org.mydotey.java.StringExtension;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ZoneOperationsUtil {
    public static List<ZoneOperationModel> newZoneOperationModels(ZoneOperations zoneOperations) {
        List<ZoneOperationModel> operations = Lists.newArrayList();
        if (zoneOperations == null || zoneOperations.getZoneKey() == null
            || CollectionExtension.isEmpty(zoneOperations.getOperations())) {
            return operations;
        }

        ZoneKey zoneKey = zoneOperations.getZoneKey();
        for (String operation : zoneOperations.getOperations()) {
            if (StringExtension.isBlank(operation)) {
                continue;
            }

            operations.add(
                new ZoneOperationModel(operation, zoneKey.getZoneId(), zoneKey.getServiceId(), zoneKey.getRegionId()));
        }

        return operations;
    }

    public static List<ZoneOperationModel> newZoneOperationModels(List<ZoneOperations> zoneOperationsList) {
        List<ZoneOperationModel> operations = Lists.newArrayList();
        if (CollectionExtension.isEmpty(zoneOperationsList)) {
            return operations;
        }

        for (ZoneOperations zoneOperations : zoneOperationsList) {
            if (zoneOperations == null) {
                continue;
            }

            operations.addAll(newZoneOperationModels(zoneOperations));
        }

        return operations;
    }

    public static List<ZoneOperationLogModel> newZoneOperationLogModels(OperationContext operationContext,
        List<ZoneOperationModel> models,
        boolean isComplete) {
        List<ZoneOperationLogModel> logs = Lists.newArrayList();
        if (operationContext == null || CollectionExtension.isEmpty(models)) {
            return logs;
        }
        for (ZoneOperationModel model : models) {
            if (model == null) {
                continue;
            }
            logs.add(new ZoneOperationLogModel(operationContext, model, isComplete));
        }

        return logs;
    }
}
