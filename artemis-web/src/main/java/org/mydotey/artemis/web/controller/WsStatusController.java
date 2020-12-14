package org.mydotey.artemis.web.controller;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.mydotey.artemis.web.websocket.HeartbeatWsHandler;
import org.mydotey.artemis.web.websocket.ServiceChangeWsHandler;
import com.google.common.collect.ImmutableMap;

/**
 * Created by fang_j on 10/07/2016.
 */
@RestController
@RequestMapping(path = "/api/status/message")
public class WsStatusController {
    @Inject
    private HeartbeatWsHandler heartbeatWsHandler;
    @Inject
    private ServiceChangeWsHandler serviceChangeWsHandler;

    @RequestMapping(path = "connections.json", method = RequestMethod.GET, produces = "application/json")
    public Map<String, String> getConnections() {
        return ImmutableMap.of(heartbeatWsHandler.name(), Integer.toString(heartbeatWsHandler.connections()),
                serviceChangeWsHandler.name(), Integer.toString(serviceChangeWsHandler.connections()));
    }
}
