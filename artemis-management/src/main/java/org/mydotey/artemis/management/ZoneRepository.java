package org.mydotey.artemis.management;

import com.google.common.collect.Lists;

import java.util.List;

import com.google.common.collect.*;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.scf.filter.RangeValueConfig;
import org.mydotey.artemis.management.common.OperationContext;
import org.mydotey.artemis.management.zone.ZoneKey;
import org.mydotey.artemis.management.zone.ZoneOperations;
import org.mydotey.artemis.management.zone.dao.ZoneOperationDao;
import org.mydotey.artemis.management.zone.dao.ZoneOperationLogDao;
import org.mydotey.artemis.management.zone.model.ZoneOperationModel;
import org.mydotey.artemis.management.zone.util.ZoneOperationsUtil;
import org.mydotey.artemis.registry.RegistryRepository;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.DynamicScheduledThread;
import org.mydotey.artemis.util.DynamicScheduledThreadConfig;
import org.mydotey.artemis.util.InstanceChanges;
import org.mydotey.java.collection.CollectionExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ZoneRepository {
    private static final Logger logger = LoggerFactory.getLogger(ZoneRepository.class);
    private static volatile ZoneRepository instance;

    public static ZoneRepository getInstance() {
        if (instance == null) {
            synchronized (ZoneRepository.class) {
                if (instance == null)
                    instance = new ZoneRepository();
            }
        }

        return instance;
    }

    private volatile long lastRefreshTime;
    private volatile boolean lastRefreshSuccess;
    private volatile Map<ZoneKey, ZoneOperations> zoneOperations = Maps.newHashMap();
    private volatile ListMultimap<String, ZoneOperations> serviceZoneOperations = ArrayListMultimap.create();
    private final DynamicScheduledThread cacheRefresher;
    private final ZoneOperationDao zoneOperationDao = ZoneOperationDao.INSTANCE;
    private final ZoneOperationLogDao zoneOperationLogDao = ZoneOperationLogDao.INSTANCE;
    private final RegistryRepository registryRepository = RegistryRepository.getInstance();

    private ZoneRepository() {
        DynamicScheduledThreadConfig dynamicScheduledThreadConfig = new DynamicScheduledThreadConfig(
            ArtemisConfig.properties(),
            new RangeValueConfig<Integer>(0, 0, 10 * 1000),
            new RangeValueConfig<Integer>(5 * 1000, 10, 60 * 1000));
        final String cacheRefreshKey = "artemis.management.zone.data.cache-refresher";
        cacheRefresher = new DynamicScheduledThread(cacheRefreshKey, () -> {
            lastRefreshTime = System.currentTimeMillis();
            lastRefreshSuccess = ArtemisTraceExecutor.INSTANCE.execute(cacheRefreshKey, () -> refreshCache());
        }, dynamicScheduledThreadConfig);
        cacheRefresher.setDaemon(true);
        cacheRefresher.start();
    }

    public boolean isZoneDown(ZoneKey zoneKey) {
        return getZoneOperations(zoneKey) != null;
    }

    public List<ZoneOperations> getAllZoneOperations(String regionId) {
        return Lists.newArrayList(zoneOperations.values());
    }

    public ZoneOperations getZoneOperations(ZoneKey zoneKey) {
        return zoneOperations.get(zoneKey);
    }

    public List<ZoneOperations> getServiceZoneOperations(String serviceId) {
        return serviceZoneOperations.get(serviceId);
    }

    public List<ZoneOperations> getZoneOperationsList(ZoneOperationModel filter) {
        return Lists.newArrayList(generateZoneOperations(zoneOperationDao.select(filter)).values());
    }

    public void operateGroupOperations(OperationContext operationContext, List<ZoneOperationModel> zoneOperations,
        boolean isOperationComplete) {
        if (isOperationComplete) {
            zoneOperationDao.delete(zoneOperations);
            zoneOperationLogDao.insert(
                ZoneOperationsUtil.newZoneOperationLogModels(operationContext, zoneOperations, isOperationComplete));
        } else {
            zoneOperationDao.insertOrUpdate(zoneOperations);
            zoneOperationLogDao.insert(
                ZoneOperationsUtil.newZoneOperationLogModels(operationContext, zoneOperations, isOperationComplete));
        }
    }

    public boolean isLastRefreshSuccess() {
        return lastRefreshSuccess;
    }

    public long getLastRefreshTime() {
        return lastRefreshTime;
    }

    protected boolean refreshCache() {
        Set<String> changedServices;

        try {
            changedServices = refreshZoneOperations();
        } catch (Throwable ex) {
            logger.error("zone operation cache refresh failed", ex);
            return false;
        }

        for (String serviceKey : changedServices) {
            registryRepository.addInstanceChange(InstanceChanges.newReloadInstanceChange(serviceKey));
        }
        return true;
    }

    private Set<String> refreshZoneOperations() {
        Map<ZoneKey, ZoneOperations> newZoneOperations = generateZoneOperations(zoneOperationDao.query());
        ListMultimap<String, ZoneOperations> newServiceZoneOperations = ArrayListMultimap.create();
        for (ZoneOperations z : newZoneOperations.values()) {
            newServiceZoneOperations.put(z.getZoneKey().getServiceId(), z);
        }
        Set<String> oldServiceKeys = serviceZoneOperations.keySet();
        zoneOperations = newZoneOperations;
        serviceZoneOperations = newServiceZoneOperations;
        Set<String> newServiceKeys = serviceZoneOperations.keySet();
        Set<String> diffs = Sets.newHashSet(Sets.difference(oldServiceKeys, newServiceKeys));
        diffs.addAll(Sets.difference(newServiceKeys, oldServiceKeys));
        return diffs;
    }

    private Map<ZoneKey, ZoneOperations> generateZoneOperations(List<ZoneOperationModel> models) {
        Map<ZoneKey, ZoneOperations> zoneOperations = Maps.newHashMap();
        if (CollectionExtension.isEmpty(models)) {
            return zoneOperations;
        }

        ListMultimap<ZoneKey, String> operations = ArrayListMultimap.create();
        for (ZoneOperationModel model : models) {
            ZoneKey zoneKey = new ZoneKey(model.getRegionId(), model.getServiceId(), model.getZoneId());
            operations.put(zoneKey, model.getOperation());
        }
        for (ZoneKey zoneKey : operations.keySet()) {
            ZoneOperations z = new ZoneOperations(zoneKey, operations.get(zoneKey));
            zoneOperations.put(zoneKey, z);
        }
        return zoneOperations;
    }

    protected void stopRefresh() {
        cacheRefresher.shutdown();
    }
}
