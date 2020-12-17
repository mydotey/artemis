package org.mydotey.artemis.server.websocket;

import java.util.Map;

import javax.inject.Inject;

import org.mydotey.artemis.config.RestPaths;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

/**
 * Created by fang_j on 10/07/2016.
 */
@RestController
@RequestMapping(path = RestPaths.STATUS_WEBSOCKET_PATH)
public class WsStatusController {

    @Inject
    private HeartbeatWsHandler heartbeatWsHandler;

    @Inject
    private ServiceChangeWsHandler serviceChangeWsHandler;

    @RequestMapping(path = RestPaths.STATUS_WEBSOCKET_CONNECTION_RELATIVE_PATH, method = RequestMethod.GET, produces = "application/json")
    public Map<String, String> getConnections() {
        return ImmutableMap.of(heartbeatWsHandler.name(), Integer.toString(heartbeatWsHandler.connections()),
            serviceChangeWsHandler.name(), Integer.toString(serviceChangeWsHandler.connections()));
    }
}
