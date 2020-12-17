package org.mydotey.artemis.server.rest.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.mydotey.artemis.config.RestPaths;
import org.mydotey.artemis.metric.MetricLoggerHelper;
import org.mydotey.artemis.registry.HeartbeatRequest;
import org.mydotey.artemis.registry.HeartbeatResponse;
import org.mydotey.artemis.registry.RegisterRequest;
import org.mydotey.artemis.registry.RegisterResponse;
import org.mydotey.artemis.registry.RegistryService;
import org.mydotey.artemis.registry.RegistryServiceImpl;
import org.mydotey.artemis.registry.UnregisterRequest;
import org.mydotey.artemis.registry.UnregisterResponse;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
@RestController
@RequestMapping(path = RestPaths.REGISTRY_PATH)
public class RegistryController {

    private RegistryService _registryService = RegistryServiceImpl.getInstance();

    @RequestMapping(path = RestPaths.REGISTRY_REGISTER_RELATIVE_PATH, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        RegisterResponse response = _registryService.register(request);
        MetricLoggerHelper.logResponseEvent("registry", "register", response);
        return response;
    }

    @RequestMapping(path = RestPaths.REGISTRY_HEARTBEAT_RELATIVE_PATH, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public HeartbeatResponse heartbeat(@RequestBody HeartbeatRequest request) {
        HeartbeatResponse response = _registryService.heartbeat(request);
        MetricLoggerHelper.logResponseEvent("registry", "heartbeat", response);
        return response;
    }

    @RequestMapping(path = RestPaths.REGISTRY_UNREGISTER_RELATIVE_PATH, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public UnregisterResponse unregister(@RequestBody UnregisterRequest request) {
        UnregisterResponse response = _registryService.unregister(request);
        MetricLoggerHelper.logResponseEvent("registry", "unregister", response);
        return response;
    }
}
