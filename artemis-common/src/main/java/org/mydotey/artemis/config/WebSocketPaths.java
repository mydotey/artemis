package org.mydotey.artemis.config;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public interface WebSocketPaths extends ArtemisPaths {

    String WS_CONTEXT_PATH = CONTEXT_PATH + "/websocket";

    String HEARTBEAT_DESTINATION = WS_CONTEXT_PATH + "/registry/heartbeat";
    String SERVICE_CHANGE_DESTINATION = WS_CONTEXT_PATH + "/discovery/instance-change";
    String ALL_SERVICES_CHANGE_DESTINATION = WS_CONTEXT_PATH + "/discovery/all-instance-change";

}
