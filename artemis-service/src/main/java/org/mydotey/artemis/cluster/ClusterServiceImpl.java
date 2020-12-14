package org.mydotey.artemis.cluster;

import java.util.ArrayList;
import java.util.List;

import org.mydotey.artemis.ErrorCodes;
import org.mydotey.artemis.config.RangePropertyConfig;
import org.mydotey.artemis.ratelimiter.ArtemisRateLimiterManager;
import org.mydotey.artemis.ratelimiter.RateLimiter;
import org.mydotey.artemis.ratelimiter.RateLimiterConfig;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.artemis.util.ServiceNodeUtil;
import org.mydotey.java.BooleanExtension;
import org.mydotey.java.collection.CollectionExtension;
import org.mydotey.util.TimeSequenceCircularBufferConfig;

import com.google.common.base.Objects;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ClusterServiceImpl implements ClusterService {

    private static ClusterServiceImpl _instance;

    public static ClusterService getInstance() {
        if (_instance == null) {
            synchronized (ClusterServiceImpl.class) {
                if (_instance == null)
                    _instance = new ClusterServiceImpl();
            }
        }

        return _instance;
    }

    private RateLimiter _rateLimiter = ArtemisRateLimiterManager.Instance.getRateLimiter("artemis.service.cluster",
        new RateLimiterConfig(true, new RangePropertyConfig<Long>(10 * 1000L, 100L, 100 * 1000L),
            new TimeSequenceCircularBufferConfig.Builder().setTimeWindow(10 * 1000L).setBucketTtl(1000L).build()));

    private ClusterServiceImpl() {

    }

    @Override
    public GetServiceNodesResponse getUpRegistryNodes(final GetServiceNodesRequest request) {
        if (_rateLimiter.isRateLimited("up-registry-nodes"))
            return new GetServiceNodesResponse(null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.cluster.up-registry-nodes",
            () -> {
                List<ServiceNode> serviceNodes = new ArrayList<>();
                for (ServiceNode node : ClusterManager.INSTANCE.allNodes()) {
                    ServiceNodeStatus nodeStatus = ClusterManager.INSTANCE.getNodeStatus(node);
                    if (!ServiceNodeUtil.canServiceRegistry(nodeStatus))
                        continue;

                    if (BooleanExtension.isTrue(nodeStatus.isAllowRegistryFromOtherZone())
                        || fromSameZone(node, request))
                        serviceNodes.add(node);
                }

                return createResponse(serviceNodes);
            });
    }

    @Override
    public GetServiceNodesResponse getUpDiscoveryNodes(final GetServiceNodesRequest request) {
        if (_rateLimiter.isRateLimited("up-discovery-nodes"))
            return new GetServiceNodesResponse(null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.cluster.up-discovery-nodes",
            () -> {
                List<ServiceNode> serviceNodes = new ArrayList<>();
                for (ServiceNode node : ClusterManager.INSTANCE.allNodes()) {
                    ServiceNodeStatus nodeStatus = ClusterManager.INSTANCE.getNodeStatus(node);
                    if (!ServiceNodeUtil.canServiceDiscovery(nodeStatus))
                        continue;

                    if (BooleanExtension.isTrue(nodeStatus.isAllowDiscoveryFromOtherZone())
                        || fromSameZone(node, request))
                        serviceNodes.add(node);
                }

                return createResponse(serviceNodes);
            });
    }

    private GetServiceNodesResponse createResponse(List<ServiceNode> serviceNodes) {
        if (CollectionExtension.isEmpty(serviceNodes))
            return new GetServiceNodesResponse(null,
                ResponseStatusUtil.newFailStatus("No available service nodes.", ErrorCodes.DATA_NOT_FOUND));

        return new GetServiceNodesResponse(serviceNodes, ResponseStatusUtil.SUCCESS_STATUS);
    }

    private boolean fromSameZone(ServiceNode node, GetServiceNodesRequest request) {
        if (node.getZone() == null)
            return false;

        if (!Objects.equal(node.getZone().getZoneId(), request.getZoneId()))
            return false;

        if (!Objects.equal(node.getZone().getRegionId(), request.getRegionId()))
            return false;

        return true;
    }

}
