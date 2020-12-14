package org.mydotey.artemis.cluster;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public interface ClusterService {

    GetServiceNodesResponse getUpRegistryNodes(GetServiceNodesRequest request);

    GetServiceNodesResponse getUpDiscoveryNodes(GetServiceNodesRequest request);

}
