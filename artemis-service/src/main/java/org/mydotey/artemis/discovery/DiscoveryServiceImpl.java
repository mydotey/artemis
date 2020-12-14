package org.mydotey.artemis.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mydotey.artemis.ErrorCodes;
import org.mydotey.artemis.InstanceChange;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.cache.ServicesDeltaGenerator;
import org.mydotey.artemis.cache.VersionedCacheManager;
import org.mydotey.artemis.cache.VersionedData;
import org.mydotey.artemis.cluster.NodeManager;
import org.mydotey.artemis.cluster.ServiceNodeStatus;
import org.mydotey.artemis.config.DeploymentConfig;
import org.mydotey.artemis.registry.RegistryRepository;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.artemis.util.SameRegionChecker;
import org.mydotey.artemis.util.SameZoneChecker;
import org.mydotey.artemis.util.ServiceNodeUtil;
import org.mydotey.artemis.util.StringUtil;
import org.mydotey.java.BooleanExtension;
import org.mydotey.java.StringExtension;
import org.mydotey.java.collection.CollectionExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class DiscoveryServiceImpl implements DiscoveryService {

    private static final Logger _logger = LoggerFactory.getLogger(DiscoveryServiceImpl.class);

    private static DiscoveryServiceImpl _instance;

    public static DiscoveryServiceImpl getInstance() {
        if (_instance == null) {
            synchronized (DiscoveryServiceImpl.class) {
                if (_instance == null)
                    _instance = new DiscoveryServiceImpl();
            }
        }

        return _instance;
    }

    private VersionedCacheManager<List<Service>, Map<Service, List<InstanceChange>>> _versionedCacheManager = new VersionedCacheManager<>(
        "artemis.service.discovery", new ServicesDataGenerator(), ServicesDeltaGenerator.DEFAULT);

    private RegistryRepository _registryRepository = RegistryRepository.getInstance();

    private List<DiscoveryFilter> _filters = new ArrayList<>();

    private DiscoveryServiceImpl() {

    }

    public synchronized void addFilters(DiscoveryFilter... filters) {
        if (CollectionExtension.isEmpty(filters))
            return;

        for (DiscoveryFilter filter : filters) {
            if (filter == null)
                continue;

            _filters.add(filter);
        }
    }

    @Override
    public LookupResponse lookup(final LookupRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.discovery.lookup",
            () -> lookupImpl(request));
    }

    @Override
    public GetServiceResponse getService(final GetServiceRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.discovery.service",
            () -> getServiceImpl(request));
    }

    @Override
    public GetServicesResponse getServices(final GetServicesRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.discovery.services",
            () -> getServicesImpl(request));
    }

    private LookupResponse lookupImpl(final LookupRequest request) {
        try {
            String errorMessage = null;
            if (request == null || CollectionExtension.isEmpty(request.getDiscoveryConfigs())) {
                errorMessage = "Request is null or request.discoveryConfigs is empty.";
                return new LookupResponse(null, ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.BAD_REQUEST));
            }

            errorMessage = checkDiscoveryStatus();
            if (!StringExtension.isBlank(errorMessage))
                return new LookupResponse(null,
                    ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.SERVICE_UNAVAILABLE));

            errorMessage = checkSameZone(request.getRegionId(), request.getZoneId());
            if (!StringExtension.isBlank(errorMessage))
                return new LookupResponse(null,
                    ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.NO_PERMISSION));

            List<Service> services = new ArrayList<>();
            for (DiscoveryConfig discoveryConfig : request.getDiscoveryConfigs()) {
                if (discoveryConfig == null || StringExtension.isBlank(discoveryConfig.getServiceId()))
                    continue;

                Service service = _registryRepository.getService(discoveryConfig.getServiceId());
                if (service == null)
                    service = new Service(discoveryConfig.getServiceId());

                filterService(service, discoveryConfig);
                services.add(service);
            }

            return new LookupResponse(services, ResponseStatusUtil.SUCCESS_STATUS);
        } catch (Throwable ex) {
            _logger.error("Lookup failed. Request: " + StringUtil.toJson(request), ex);
            return new LookupResponse(null,
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
        }
    }

    private GetServiceResponse getServiceImpl(GetServiceRequest request) {
        try {
            String errorMessage = null;
            if (request == null || request.getDiscoveryConfig() == null
                || StringExtension.isBlank(request.getDiscoveryConfig().getServiceId())) {
                errorMessage = "Request is null or request.discoveryConfig is null.";
                return new GetServiceResponse(null,
                    ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.BAD_REQUEST));
            }

            errorMessage = checkDiscoveryStatus();
            if (!StringExtension.isBlank(errorMessage))
                return new GetServiceResponse(null,
                    ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.SERVICE_UNAVAILABLE));

            errorMessage = checkSameZone(request.getRegionId(), request.getZoneId());
            if (!StringExtension.isBlank(errorMessage))
                return new GetServiceResponse(null,
                    ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.NO_PERMISSION));

            Service service = _registryRepository.getService(request.getDiscoveryConfig().getServiceId());
            filterService(service, request.getDiscoveryConfig());
            if (service == null)
                service = new Service(request.getDiscoveryConfig().getServiceId());
            return new GetServiceResponse(service, ResponseStatusUtil.SUCCESS_STATUS);
        } catch (Throwable ex) {
            _logger.error("GetService failed. Request: " + StringUtil.toJson(request), ex);
            return new GetServiceResponse(null,
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
        }
    }

    private GetServicesResponse getServicesImpl(final GetServicesRequest request) {
        try {
            String errorMessage = checkDiscoveryStatus();
            if (!StringExtension.isBlank(errorMessage))
                return new GetServicesResponse(null, 0,
                    ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.SERVICE_UNAVAILABLE));

            VersionedData<List<Service>> data = _versionedCacheManager.get();
            return new GetServicesResponse(data.getData(), data.getVersion(), ResponseStatusUtil.SUCCESS_STATUS);
        } catch (Throwable ex) {
            _logger.error("GetServices failed. Request: " + StringUtil.toJson(request), ex);
            return new GetServicesResponse(null, 0,
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
        }
    }

    @Override
    public GetServicesDeltaResponse getServicesDelta(final GetServicesDeltaRequest request) {
        try {
            String errorMessage = checkDiscoveryStatus();
            if (!StringExtension.isBlank(errorMessage))
                return new GetServicesDeltaResponse(null, 0,
                    ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.SERVICE_UNAVAILABLE));

            VersionedData<Map<Service, List<InstanceChange>>> delta = _versionedCacheManager
                .getDelta(request.getVersion());
            if (delta == null)
                return new GetServicesDeltaResponse(null, 0,
                    ResponseStatusUtil.newFailStatus("Delta is not found.", ErrorCodes.DATA_NOT_FOUND));

            return new GetServicesDeltaResponse(delta.getData(), delta.getVersion(), ResponseStatusUtil.SUCCESS_STATUS);
        } catch (Throwable ex) {
            _logger.error("GetServicesDelta failed. Request: " + StringUtil.toJson(request), ex);
            return new GetServicesDeltaResponse(null, 0,
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
        }
    }

    private String checkDiscoveryStatus() {
        ServiceNodeStatus nodeStatus = NodeManager.INSTANCE.nodeStatus();
        if (ServiceNodeUtil.canServiceDiscovery(nodeStatus))
            return null;

        return "Serivce discovery is not in up state. Current status: " + nodeStatus;
    }

    private String checkSameZone(String regionId, String zoneId) {
        if (!SameRegionChecker.DEFAULT.isSameRegion(regionId))
            return String.format(
                "regionId is not the same as the registry node. regionId: %s, registry node.regionId: %s", regionId,
                DeploymentConfig.regionId());

        if (BooleanExtension.isTrue(NodeManager.INSTANCE.nodeStatus().isAllowDiscoveryFromOtherZone()))
            return null;

        if (SameZoneChecker.DEFAULT.isSameZone(zoneId))
            return null;

        return String.format("zoneId is not the same as the registry node. zoneId: %s, registry node.zoneId: %s",
            zoneId,
            DeploymentConfig.zoneId());
    }

    private class ServicesDataGenerator implements Supplier<List<Service>> {
        @Override
        public List<Service> get() {
            List<Service> services = new ArrayList<>();
            List<Service> origin = _registryRepository.getServices();
            if (CollectionExtension.isEmpty(origin))
                return services;

            DiscoveryConfig genericConfig = DiscoveryConfig.GENERIC.clone();
            for (Service service : origin) {
                filterService(service, genericConfig);
                if (CollectionExtension.isEmpty(service.getInstances()))
                    continue;
                services.add(service);
            }

            return services;
        }
    }

    private void filterService(Service service, DiscoveryConfig discoveryConfig) {
        if (service == null)
            return;

        for (DiscoveryFilter filter : _filters) {
            try {
                filter.filter(service, discoveryConfig);
            } catch (Throwable ex) {
                _logger.error("Failed to execute filter " + filter, ex);
            }
        }
    }

}
