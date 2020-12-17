package org.mydotey.artemis.config;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public interface RestPaths extends ArtemisPaths {

    String REGISTRY_PATH = CONTEXT_PATH + "api/registry/";
    String REGISTRY_REGISTER_RELATIVE_PATH = "register.json";
    String REGISTRY_UNREGISTER_RELATIVE_PATH = "unregister.json";
    String REGISTRY_HEARTBEAT_RELATIVE_PATH = "heartbeat.json";
    String REGISTRY_REGISTER_FULL_PATH = REGISTRY_PATH + REGISTRY_REGISTER_RELATIVE_PATH;
    String REGISTRY_UNREGISTER_FULL_PATH = REGISTRY_PATH + REGISTRY_UNREGISTER_RELATIVE_PATH;
    String REGISTRY_HEARTBEAT_FULL_PATH = REGISTRY_PATH + REGISTRY_HEARTBEAT_RELATIVE_PATH;

    String REPLICATION_PATH = CONTEXT_PATH + "api/replication/";
    String REPLICATION_REGISTRY_PATH = REPLICATION_PATH + "registry/";
    String REPLICATION_REGISTRY_REGISTER_RELATIVE_PATH = "register.json";
    String REPLICATION_REGISTRY_UNREGISTER_RELATIVE_PATH = "unregister.json";
    String REPLICATION_REGISTRY_HEARTBEAT_RELATIVE_PATH = "heartbeat.json";
    String REPLICATION_REGISTRY_GET_SERVICES_RELATIVE_PATH = "services.json";
    String REPLICATION_REGISTRY_REGISTER_FULL_PATH = REPLICATION_REGISTRY_PATH
        + REPLICATION_REGISTRY_REGISTER_RELATIVE_PATH;
    String REPLICATION_REGISTRY_UNREGISTER_FULL_PATH = REPLICATION_REGISTRY_PATH
        + REPLICATION_REGISTRY_UNREGISTER_RELATIVE_PATH;
    String REPLICATION_REGISTRY_HEARTBEAT_FULL_PATH = REPLICATION_REGISTRY_PATH
        + REPLICATION_REGISTRY_HEARTBEAT_RELATIVE_PATH;
    String REPLICATION_REGISTRY_GET_SERVICES_FULL_PATH = REPLICATION_REGISTRY_PATH
        + REPLICATION_REGISTRY_GET_SERVICES_RELATIVE_PATH;

    String CLUSTER_PATH = CONTEXT_PATH + "api/cluster/";
    String CLUSTER_NODES_RELATIVE_PATH = "nodes.json";
    String CLUSTER_UP_REGISTRY_NODES_RELATIVE_PATH = "up-registry-nodes.json";
    String CLUSTER_UP_DISCOVERY_NODES_RELATIVE_PATH = "up-discovery-nodes.json";
    String CLUSTER_NODES_FULL_PATH = CLUSTER_PATH + CLUSTER_NODES_RELATIVE_PATH;
    String CLUSTER_UP_REGISTRY_NODES_FULL_PATH = CLUSTER_PATH + CLUSTER_UP_REGISTRY_NODES_RELATIVE_PATH;
    String CLUSTER_UP_DISCOVERY_NODES_FULL_PATH = CLUSTER_PATH + CLUSTER_UP_DISCOVERY_NODES_RELATIVE_PATH;

    String STATUS_PATH = CONTEXT_PATH + "api/status/";
    String STATUS_NODE_RELATIVE_PATH = "node.json";
    String STATUS_CLUSTER_RELATIVE_PATH = "cluster.json";
    String STATUS_LEASES_RELATIVE_PATH = "leases.json";
    String STATUS_LEGACY_LEASES_RELATIVE_PATH = "legacy-leases.json";
    String STATUS_CONFIG_RELATIVE_PATH = "config.json";
    String STATUS_DEPLOYMENT_RELATIVE_PATH = "deployment.json";
    String STATUS_NODE_FULL_PATH = STATUS_PATH + STATUS_NODE_RELATIVE_PATH;
    String STATUS_CLUSTER_FULL_PATH = STATUS_PATH + STATUS_CLUSTER_RELATIVE_PATH;
    String STATUS_LEASES_FULL_PATH = STATUS_PATH + STATUS_LEASES_RELATIVE_PATH;
    String STATUS_LEGACY_LEASES_FULL_PATH = STATUS_PATH + STATUS_LEGACY_LEASES_RELATIVE_PATH;
    String STATUS_CONFIG_FULL_PATH = STATUS_PATH + STATUS_CONFIG_RELATIVE_PATH;
    String STATUS_DEPLOYMENT_FULL_PATH = STATUS_PATH + STATUS_DEPLOYMENT_RELATIVE_PATH;

    String STATUS_WEBSOCKET_PATH = STATUS_PATH + "websocket";
    String STATUS_WEBSOCKET_CONNECTION_RELATIVE_PATH = "connection.json";

    String DISCOVERY_PATH = CONTEXT_PATH + "api/discovery/";
    String DISCOVERY_LOOKUP_RELATIVE_PATH = "lookup.json";
    String DISCOVERY_GET_SERVICE_RELATIVE_PATH = "service.json";
    String DISCOVERY_GET_SERVICES_RELATIVE_PATH = "services.json";
    String DISCOVERY_GET_SERVICES_DELTA_RELATIVE_PATH = "services-delta.json";
    String DISCOVERY_LOOKUP_FULL_PATH = DISCOVERY_PATH + DISCOVERY_LOOKUP_RELATIVE_PATH;
    String DISCOVERY_GET_SERVICE_FULL_PATH = DISCOVERY_PATH + DISCOVERY_GET_SERVICE_RELATIVE_PATH;
    String DISCOVERY_GET_SERVICES_FULL_PATH = DISCOVERY_PATH + DISCOVERY_GET_SERVICES_RELATIVE_PATH;
    String DISCOVERY_GET_SERVICES_DELTA_FULL_PATH = DISCOVERY_PATH + DISCOVERY_GET_SERVICES_DELTA_RELATIVE_PATH;
}
