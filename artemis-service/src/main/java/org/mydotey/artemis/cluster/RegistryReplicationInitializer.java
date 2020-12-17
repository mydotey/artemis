package org.mydotey.artemis.cluster;

import java.util.List;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.config.DeploymentConfig;
import org.mydotey.artemis.registry.RegisterRequest;
import org.mydotey.artemis.registry.RegisterResponse;
import org.mydotey.artemis.registry.replication.GetServicesRequest;
import org.mydotey.artemis.registry.replication.GetServicesResponse;
import org.mydotey.artemis.registry.replication.RegistryReplicationService;
import org.mydotey.artemis.registry.replication.RegistryReplicationServiceClient;
import org.mydotey.artemis.registry.replication.RegistryReplicationServiceImpl;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.artemis.util.ServiceNodeUtil;
import org.mydotey.codec.json.JacksonJsonCodec;
import org.mydotey.java.collection.CollectionExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class RegistryReplicationInitializer implements NodeInitializer {

    private static final Logger _logger = LoggerFactory.getLogger(RegistryReplicationInitializer.class);

    public static final RegistryReplicationInitializer INSTANCE = new RegistryReplicationInitializer();

    private RegistryReplicationService _registryReplicationService = RegistryReplicationServiceImpl.getInstance();

    private RegistryReplicationInitializer() {

    }

    @Override
    public TargetType target() {
        return TargetType.REGISTRY;
    }

    @Override
    public boolean initialized() {
        List<ServiceNode> peerNodes = ClusterManager.INSTANCE.localZoneOtherNodes();
        boolean success = initRegistryDataFromPeerNodes(peerNodes);
        if (success)
            return true;

        peerNodes = ClusterManager.INSTANCE.otherZoneNodes();
        return initRegistryDataFromPeerNodes(peerNodes);
    }

    private boolean initRegistryDataFromPeerNodes(List<ServiceNode> peerNodes) {
        for (ServiceNode peerNode : peerNodes) {
            ServiceNodeStatus nodeStatus = ClusterManager.INSTANCE.getNodeStatus(peerNode);
            if (!ServiceNodeUtil.isUp(nodeStatus))
                continue;

            boolean success = initRegistryDataFromPeerNode(peerNode);
            _logger.info("initRegistryData from peerNode: [" + peerNode.getUrl() + "] success? " + success);
            if (success)
                return true;
        }

        return false;
    }

    private boolean initRegistryDataFromPeerNode(ServiceNode peerNode) {
        RegistryReplicationServiceClient client = new RegistryReplicationServiceClient(peerNode.getUrl());
        GetServicesResponse response;
        try {
            GetServicesRequest request = new GetServicesRequest();
            request.setRegionId(DeploymentConfig.regionId());
            request.setZoneId(DeploymentConfig.zoneId());
            response = client.getServices(request);
            if (!ResponseStatusUtil.isSuccess(response.getResponseStatus())) {
                _logger.info("Response: " + new String(JacksonJsonCodec.DEFAULT.encode(response)));
                return false;
            }
        } catch (Throwable ex) {
            _logger.error("NodeManager initRegistryDataFromPeerNode failed.", ex);
            return false;
        }

        List<Service> services = response.getServices();
        if (services == null) {
            _logger
                .info(String.format("The services list of peerNode: [%s] is null, return false!", peerNode.getUrl()));
            return false;
        }

        int replicationInstanceCount = 0;
        for (Service service : services) {
            if (service == null)
                continue;

            List<Instance> instances = service.getInstances();
            if (CollectionExtension.isEmpty(instances))
                continue;

            RegisterRequest request = new RegisterRequest(instances);
            RegisterResponse response2 = _registryReplicationService.register(request);
            if (!ResponseStatusUtil.isSuccess(response2.getResponseStatus())) {
                _logger.info("Response2: " + new String(JacksonJsonCodec.DEFAULT.encode(response2)));
                return false;
            }

            replicationInstanceCount++;
        }

        _logger.info("replicationInstanceCount: " + replicationInstanceCount);
        return replicationInstanceCount > 0;
    }

}
