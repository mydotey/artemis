package org.mydotey.artemis.registry.replication;

import java.util.List;

import org.mydotey.artemis.ErrorCodes;
import org.mydotey.artemis.Instance;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.config.RangePropertyConfig;
import org.mydotey.artemis.ratelimiter.ArtemisRateLimiterManager;
import org.mydotey.artemis.ratelimiter.RateLimiter;
import org.mydotey.artemis.ratelimiter.RateLimiterConfig;
import org.mydotey.artemis.registry.HeartbeatRequest;
import org.mydotey.artemis.registry.HeartbeatResponse;
import org.mydotey.artemis.registry.RegisterRequest;
import org.mydotey.artemis.registry.RegisterResponse;
import org.mydotey.artemis.registry.RegistryRepository;
import org.mydotey.artemis.registry.RegistryTool;
import org.mydotey.artemis.registry.UnregisterRequest;
import org.mydotey.artemis.registry.UnregisterResponse;
import org.mydotey.artemis.registry.RegistryTool.ExecutionResult;
import org.mydotey.artemis.registry.RegistryTool.Executor;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.artemis.util.StringUtil;
import org.mydotey.java.StringExtension;
import org.mydotey.util.TimeSequenceCircularBufferConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class RegistryReplicationServiceImpl implements RegistryReplicationService {

    private static final Logger _logger = LoggerFactory.getLogger(RegistryReplicationServiceImpl.class);

    private static RegistryReplicationServiceImpl _instance;

    public static RegistryReplicationServiceImpl getInstance() {
        if (_instance == null) {
            synchronized (RegistryReplicationServiceImpl.class) {
                if (_instance == null)
                    _instance = new RegistryReplicationServiceImpl();
            }
        }

        return _instance;
    }

    private RegistryRepository _repository = RegistryRepository.getInstance();

    private RateLimiter _rateLimiter = ArtemisRateLimiterManager.Instance.getRateLimiter(
        "artemis.service.registry.replication",
        new RateLimiterConfig(true, new RangePropertyConfig<Long>(1000 * 1000L, 1000L, 10 * 1000 * 1000L),
            new TimeSequenceCircularBufferConfig.Builder().setTimeWindow(10 * 1000).setBucketTtl(1000).build()));

    private RegistryReplicationServiceImpl() {

    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (_rateLimiter.isRateLimited("register"))
            return new RegisterResponse(null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        ExecutionResult result = RegistryTool.replicationExecute("artemis.service.registry.replication.register",
            request, new Executor() {

                @Override
                public String execute(Instance instance) {
                    _repository.register(instance);
                    return null;
                }

            });

        return new RegisterResponse(result.failedInstances(), result.responseStatus());
    }

    @Override
    public HeartbeatResponse heartbeat(HeartbeatRequest request) {
        if (_rateLimiter.isRateLimited("heartbeat"))
            return new HeartbeatResponse(null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        ExecutionResult result = RegistryTool.replicationExecute("artemis.service.registry.replication.heartbeat",
            request, new Executor() {

                @Override
                public String execute(Instance instance) {
                    boolean success = _repository.heartbeat(instance);
                    if (!success)
                        _repository.register(instance);
                    return null;
                }

            });

        return new HeartbeatResponse(result.failedInstances(), result.responseStatus());
    }

    @Override
    public UnregisterResponse unregister(UnregisterRequest request) {
        if (_rateLimiter.isRateLimited("unregister"))
            return new UnregisterResponse(null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        ExecutionResult result = RegistryTool.replicationExecute("artemis.service.registry.replication.unregister",
            request, new Executor() {

                @Override
                public String execute(Instance instance) {
                    _repository.unregister(instance);
                    return null;
                }

            });

        return new UnregisterResponse(result.failedInstances(), result.responseStatus());
    }

    @Override
    public GetServicesResponse getServices(final GetServicesRequest request) {
        if (_rateLimiter.isRateLimited("get-services"))
            return new GetServicesResponse(null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        try {
            return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.registry.replication.get-applications",
                () -> {
                    String errorMessage = RegistryTool.checkRegistryStatus(true);
                    if (!StringExtension.isBlank(errorMessage))
                        return new GetServicesResponse(null,
                            ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.SERVICE_UNAVAILABLE));

                    errorMessage = RegistryTool.checkSameZone(request.getRegionId(), request.getZoneId(), true);
                    if (!StringExtension.isBlank(errorMessage))
                        return new GetServicesResponse(null,
                            ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.NO_PERMISSION));

                    List<Service> applications = _repository.getServices();
                    return new GetServicesResponse(applications, ResponseStatusUtil.SUCCESS_STATUS);
                });
        } catch (Throwable ex) {
            _logger.error("GetApplications failed. Request: " + StringUtil.toJson(request), ex);
            return new GetServicesResponse(null,
                ResponseStatusUtil.newFailStatus(ex.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
        }
    }

}
