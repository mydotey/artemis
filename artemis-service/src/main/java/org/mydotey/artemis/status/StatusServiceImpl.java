package org.mydotey.artemis.status;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mydotey.artemis.ErrorCodes;
import org.mydotey.artemis.Instance;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.cluster.ClusterManager;
import org.mydotey.artemis.cluster.NodeManager;
import org.mydotey.artemis.cluster.ServiceNode;
import org.mydotey.artemis.cluster.ServiceNodeStatus;
import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.artemis.config.DeploymentConfig;
import org.mydotey.artemis.config.PropertyComparator;
import org.mydotey.artemis.config.RangePropertyConfig;
import org.mydotey.artemis.lease.Lease;
import org.mydotey.artemis.lease.LeaseManager;
import org.mydotey.artemis.lease.LeaseUpdateSafeChecker;
import org.mydotey.artemis.ratelimiter.ArtemisRateLimiterManager;
import org.mydotey.artemis.ratelimiter.RateLimiter;
import org.mydotey.artemis.ratelimiter.RateLimiterConfig;
import org.mydotey.artemis.registry.RegistryRepository;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.java.collection.CollectionExtension;
import org.mydotey.scf.ConfigurationSource;
import org.mydotey.scf.Property;
import org.mydotey.util.TimeSequenceCircularBufferConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class StatusServiceImpl implements StatusService {

    private static final Logger _logger = LoggerFactory.getLogger(StatusServiceImpl.class);

    private static StatusServiceImpl _instance;

    public static StatusServiceImpl getInstance() {
        if (_instance == null) {
            synchronized (StatusServiceImpl.class) {
                if (_instance == null)
                    _instance = new StatusServiceImpl();
            }
        }

        return _instance;
    }

    private RateLimiter _rateLimiter = ArtemisRateLimiterManager.Instance.getRateLimiter("artemis.service.status",
        new RateLimiterConfig(true, new RangePropertyConfig<Long>(30L, 1L, 10 * 1000L),
            new TimeSequenceCircularBufferConfig.Builder().setTimeWindow(10 * 1000).setBucketTtl(1000).build()));

    private RegistryRepository _registryRepository = RegistryRepository.getInstance();

    private StatusServiceImpl() {

    }

    @Override
    public GetClusterNodeStatusResponse getClusterNodeStatus(GetClusterNodeStatusRequest request) {
        try {
            return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.status.get-cluster-node",
                () -> {
                    return new GetClusterNodeStatusResponse(NodeManager.INSTANCE.nodeStatus(),
                        ResponseStatusUtil.SUCCESS_STATUS);
                });
        } catch (Throwable ex) {
            _logger.warn("GetClusterNodeStatus failed. request: " + request, ex);
            return new GetClusterNodeStatusResponse(null,
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
        }
    }

    @Override
    public GetClusterStatusResponse getClusterStatus(GetClusterStatusRequest request) {
        if (_rateLimiter.isRateLimited("get-cluster"))
            return new GetClusterStatusResponse(0, null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        try {
            return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.status.get-cluster",
                () -> {
                    List<ServiceNodeStatus> nodesStatus = new ArrayList<>();
                    for (ServiceNode node : ClusterManager.INSTANCE.allNodes()) {
                        nodesStatus.add(ClusterManager.INSTANCE.getNodeStatus(node));
                    }

                    return new GetClusterStatusResponse(nodesStatus.size(), nodesStatus,
                        ResponseStatusUtil.SUCCESS_STATUS);
                });
        } catch (Throwable ex) {
            _logger.warn("GetClusterStatus failed. request: " + request, ex);
            return new GetClusterStatusResponse(0, null,
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
        }
    }

    @Override
    public GetLeasesStatusResponse getLeasesStatus(final GetLeasesStatusRequest request) {
        return getLeasesStatus("get-leases", "artemis.service.status.get-leases", request,
            _registryRepository.getLeaseManager());
    }

    @Override
    public GetLeasesStatusResponse getLegacyLeasesStatus(final GetLeasesStatusRequest request) {
        return getLeasesStatus("get-legacy-leases", "artemis.service.status.get-legacy-leases", request,
            _registryRepository.getLegacyInstanceLeaseManager());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public GetConfigStatusResponse getConfigStatus(GetConfigStatusRequest request) {
        if (_rateLimiter.isRateLimited("get-config"))
            return new GetConfigStatusResponse(null, null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        try {
            return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.status.get-config",
                () -> {
                    List<Property> propertyCache = Lists
                        .<Property>newArrayList(ArtemisConfig.properties().getManager().getProperties());
                    Collections.sort(propertyCache, PropertyComparator.DEFAULT);
                    Map<String, String> properties = new LinkedHashMap<>();
                    for (Property property : propertyCache) {
                        properties.put(property.getConfig().getKey().toString(), property.toString());
                    }

                    Map<String, Integer> sources = new LinkedHashMap<>();
                    for (Entry<Integer, ConfigurationSource> entry : ArtemisConfig.properties().getManager().getConfig()
                        .getSources().entrySet()) {
                        sources.put(entry.getValue().getConfig().getName(), entry.getKey());
                    }

                    return new GetConfigStatusResponse(sources, properties, ResponseStatusUtil.SUCCESS_STATUS);
                });
        } catch (Throwable ex) {
            _logger.warn("GetConfigStatus failed. request: " + request, ex);
            return new GetConfigStatusResponse(null, null,
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public GetDeploymentStatusResponse getDeploymentStatus(GetDeploymentStatusRequest request) {
        if (_rateLimiter.isRateLimited("get-deployment")) {
            GetDeploymentStatusResponse response = new GetDeploymentStatusResponse();
            response.setResponseStatus(ResponseStatusUtil.RATE_LIMITED_STATUS);
            return response;
        }

        try {
            return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.status.get-deployment",
                () -> {
                    List<Property> propertyCache = Lists
                        .<Property>newArrayList(DeploymentConfig.properties().getManager().getProperties());
                    Collections.sort(propertyCache, PropertyComparator.DEFAULT);
                    Map<String, String> properties = new LinkedHashMap<>();
                    for (Property property : propertyCache) {
                        properties.put(property.getConfig().getKey().toString(), property.toString());
                    }

                    Map<String, Integer> sources = new LinkedHashMap<>();
                    for (Entry<Integer, ConfigurationSource> entry : DeploymentConfig.properties().getManager()
                        .getConfig()
                        .getSources().entrySet()) {
                        sources.put(entry.getValue().getConfig().getName(), entry.getKey());
                    }

                    return new GetDeploymentStatusResponse(DeploymentConfig.regionId(), DeploymentConfig.zoneId(),
                        DeploymentConfig.appId(),
                        DeploymentConfig.machineName(), DeploymentConfig.ip(), DeploymentConfig.port(),
                        DeploymentConfig.protocol(),
                        DeploymentConfig.path(), sources, properties, ResponseStatusUtil.SUCCESS_STATUS);
                });
        } catch (Throwable ex) {
            _logger.warn("GetDeploymentStatus failed. request: " + request, ex);
            GetDeploymentStatusResponse response = new GetDeploymentStatusResponse();
            response.setResponseStatus(
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
            return response;
        }
    }

    private GetLeasesStatusResponse getLeasesStatus(String opName, final String traceKey,
        final GetLeasesStatusRequest request,
        final LeaseManager<Instance> leaseManager) {
        if (_rateLimiter.isRateLimited(opName))
            return new GetLeasesStatusResponse(0, 0, 0, false, false, 0, null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        try {
            return ArtemisTraceExecutor.INSTANCE.execute(traceKey, () -> {
                Map<Service, List<LeaseStatus>> leasesStatusMap = new HashMap<>();
                List<String> serviceIds = request.getServiceIds();
                ListMultimap<Service, Lease<Instance>> leaseMultiMap = CollectionExtension.isEmpty(serviceIds)
                    ? _registryRepository.getLeases(leaseManager)
                    : _registryRepository.getLeases(serviceIds, leaseManager);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                int leaseCount = 0;
                for (Service service : leaseMultiMap.keySet()) {
                    List<LeaseStatus> leasesStatus = new ArrayList<>();
                    List<Lease<Instance>> leases = leaseMultiMap.get(service);
                    for (Lease<Instance> lease : leases) {
                        leasesStatus
                            .add(new LeaseStatus(lease.data().toString(), dateFormat.format(lease.creationTime()),
                                dateFormat.format(lease.renewalTime()), dateFormat.format(lease.evictionTime()),
                                lease.ttl()));
                    }

                    leasesStatusMap.put(service, leasesStatus);
                    leaseCount += leasesStatus.size();
                }

                LeaseUpdateSafeChecker leaseUpdateSafeChecker = leaseManager.leaseUpdateSafeChecker();
                return new GetLeasesStatusResponse(leaseUpdateSafeChecker.maxCount(),
                    leaseUpdateSafeChecker.maxCountLastUpdateTime(),
                    leaseUpdateSafeChecker.countLastTimeWindow(), leaseUpdateSafeChecker.isSafe(),
                    leaseUpdateSafeChecker.isEnabled(),
                    leaseCount, leasesStatusMap, ResponseStatusUtil.SUCCESS_STATUS);
            });
        } catch (Throwable ex) {
            _logger.warn("GetLeasesStatus failed. request: " + request, ex);
            return new GetLeasesStatusResponse(0, 0, 0, false, false, 0, null,
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
        }
    }

}
