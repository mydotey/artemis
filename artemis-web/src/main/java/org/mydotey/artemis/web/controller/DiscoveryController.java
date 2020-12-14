package org.mydotey.artemis.web.controller;

import org.mydotey.artemis.web.util.*;
import org.mydotey.artemis.web.websocket.AllServicesChangeWsHandler;
import org.mydotey.artemis.web.websocket.ServiceChangeWsHandler;
import com.google.common.collect.Lists;

import org.mydotey.artemis.ErrorCodes;
import org.mydotey.artemis.ResponseStatus;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.config.RestPaths;
import org.mydotey.artemis.discovery.DiscoveryConfig;
import org.mydotey.artemis.discovery.DiscoveryServiceImpl;
import org.mydotey.artemis.discovery.GetServiceRequest;
import org.mydotey.artemis.discovery.GetServiceResponse;
import org.mydotey.artemis.discovery.GetServicesDeltaRequest;
import org.mydotey.artemis.discovery.GetServicesDeltaResponse;
import org.mydotey.artemis.discovery.GetServicesRequest;
import org.mydotey.artemis.discovery.GetServicesResponse;
import org.mydotey.artemis.discovery.LookupRequest;
import org.mydotey.artemis.discovery.LookupResponse;
import org.mydotey.artemis.metric.MetricLoggerHelper;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.artemis.util.StringUtil;
import org.mydotey.java.collection.CollectionExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
@RestController
@RequestMapping(path = RestPaths.DISCOVERY_PATH)
public class DiscoveryController {

    private static final Logger _logger = LoggerFactory.getLogger(DiscoveryController.class);

    private DiscoveryServiceImpl _discoveryService = DiscoveryServiceImpl.getInstance();

    @Inject
    private ServiceChangeWsHandler _publisher;

    @Inject
    private AllServicesChangeWsHandler _allServicePublisher;

    @Inject
    protected void init() {
        List<Publisher> publishers = Lists.newArrayList();
        publishers.add(_publisher);
        publishers.add(_allServicePublisher);
        InstanceChangeManager.getInstance().init(publishers);
        ;
    }

    @RequestMapping(path = RestPaths.DISCOVERY_LOOKUP_RELATIVE_PATH, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public LookupResponse lookup(@RequestBody LookupRequest request) {
        LookupResponse response;
        try {
            List<Service> services = new ArrayList<>();
            for (DiscoveryConfig discoveryConfig : request.getDiscoveryConfigs()) {
                String serviceId = discoveryConfig.getServiceId();
                LookupRequest singleServiceRequest = new LookupRequest(Lists.newArrayList(discoveryConfig),
                    request.getRegionId(), request.getZoneId());
                LookupResponse singleServiceResponse = this._discoveryService.lookup(singleServiceRequest);
                ResponseStatus responseStatus = singleServiceResponse.getResponseStatus();
                if (ResponseStatusUtil.isSuccess(responseStatus)
                    && !CollectionExtension.isEmpty(singleServiceResponse.getServices())) {
                    Service service = singleServiceResponse.getServices().get(0);
                    services.add(service);
                    continue;
                }

                services.add(new Service(serviceId));
            }

            response = new LookupResponse(services, ResponseStatusUtil.SUCCESS_STATUS);
        } catch (Throwable ex) {
            _logger.error("Lookup failed. Request: " + StringUtil.toJson(request), ex);
            response = new LookupResponse(null,
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
        }

        MetricLoggerHelper.logResponseEvent("discovery", "lookup", response);
        return response;
    }

    @RequestMapping(path = RestPaths.DISCOVERY_GET_SERVICE_RELATIVE_PATH, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public GetServiceResponse getService(@RequestBody GetServiceRequest request) {
        GetServiceResponse response = this._discoveryService.getService(request);
        MetricLoggerHelper.logResponseEvent("discovery", "get-service", response);
        return response;
    }

    @RequestMapping(path = RestPaths.DISCOVERY_GET_SERVICE_RELATIVE_PATH, method = RequestMethod.GET, produces = "application/json")
    public GetServiceResponse getService(@RequestParam(required = false) String regionId,
        @RequestParam(required = false) String zoneId,
        @RequestParam String serviceId) {
        GetServiceResponse response = getService(
            new GetServiceRequest(new DiscoveryConfig(serviceId), regionId, zoneId));
        MetricLoggerHelper.logResponseEvent("discovery", "get-service", response);
        return response;
    }

    @RequestMapping(path = RestPaths.DISCOVERY_GET_SERVICES_RELATIVE_PATH, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public GetServicesResponse getServices(@RequestBody GetServicesRequest request) {
        GetServicesResponse response = _discoveryService.getServices(request);
        MetricLoggerHelper.logResponseEvent("discovery", "get-services", response);
        return response;
    }

    @RequestMapping(path = RestPaths.DISCOVERY_GET_SERVICES_RELATIVE_PATH, method = RequestMethod.GET, produces = "application/json")
    public GetServicesResponse getServices(@RequestParam(required = false) String regionId,
        @RequestParam(required = false) String zoneId) {
        GetServicesResponse response = _discoveryService.getServices(new GetServicesRequest(regionId, zoneId));
        MetricLoggerHelper.logResponseEvent("discovery", "get-services", response);
        return response;
    }

    @RequestMapping(path = RestPaths.DISCOVERY_GET_SERVICES_DELTA_RELATIVE_PATH, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public GetServicesDeltaResponse getServicesDelta(@RequestBody GetServicesDeltaRequest request) {
        GetServicesDeltaResponse response = _discoveryService.getServicesDelta(request);
        MetricLoggerHelper.logResponseEvent("discovery", "get-services-delta", response);
        return response;
    }

}
