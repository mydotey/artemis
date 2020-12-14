package org.mydotey.artemis.registry;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.config.RangePropertyConfig;
import org.mydotey.artemis.ratelimiter.ArtemisRateLimiterManager;
import org.mydotey.artemis.ratelimiter.RateLimiter;
import org.mydotey.artemis.ratelimiter.RateLimiterConfig;
import org.mydotey.artemis.registry.RegistryTool.ExecutionResult;
import org.mydotey.artemis.registry.RegistryTool.Executor;
import org.mydotey.artemis.registry.replication.HeartbeatTask;
import org.mydotey.artemis.registry.replication.RegisterTask;
import org.mydotey.artemis.registry.replication.RegistryReplicationManager;
import org.mydotey.artemis.registry.replication.UnregisterTask;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.util.TimeSequenceCircularBufferConfig;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class RegistryServiceImpl implements RegistryService {

    private static RegistryServiceImpl _instance;

    public static RegistryServiceImpl getInstance() {
        if (_instance == null) {
            synchronized (RegistryServiceImpl.class) {
                if (_instance == null)
                    _instance = new RegistryServiceImpl();
            }
        }

        return _instance;
    }

    private RegistryRepository _repository = RegistryRepository.getInstance();

    private RateLimiter _rateLimiter = ArtemisRateLimiterManager.Instance.getRateLimiter("artemis.service.registry",
        new RateLimiterConfig(true, new RangePropertyConfig<Long>(100 * 1000L, 1000L, 1000 * 1000L),
            new TimeSequenceCircularBufferConfig.Builder().setTimeWindow(10 * 1000).setBucketTtl(1000).build()));

    private RegistryServiceImpl() {

    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (_rateLimiter.isRateLimited("register"))
            return new RegisterResponse(null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        ExecutionResult result = RegistryTool.execute("artemis.service.registry.register", request, new Executor() {
            @Override
            public String execute(Instance instance) {
                _repository.register(instance);
                RegistryReplicationManager.INSTANCE.replicate(new RegisterTask(instance));
                return null;
            }
        });

        return new RegisterResponse(result.failedInstances(), result.responseStatus());
    }

    @Override
    public HeartbeatResponse heartbeat(HeartbeatRequest request) {
        if (_rateLimiter.isRateLimited("heartbeat"))
            return new HeartbeatResponse(null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        ExecutionResult result = RegistryTool.execute("artemis.service.registry.heartbeat", request, new Executor() {
            @Override
            public String execute(Instance instance) {
                boolean success = _repository.heartbeat(instance);
                if (!success)
                    return "Instance is not exising! Need re-register.";

                RegistryReplicationManager.INSTANCE.replicate(new HeartbeatTask(instance));
                return null;
            }
        });

        return new HeartbeatResponse(result.failedInstances(), result.responseStatus());
    }

    @Override
    public UnregisterResponse unregister(UnregisterRequest request) {
        if (_rateLimiter.isRateLimited("unregister"))
            return new UnregisterResponse(null, ResponseStatusUtil.RATE_LIMITED_STATUS);

        ExecutionResult result = RegistryTool.execute("artemis.service.registry.unregister", request, new Executor() {
            @Override
            public String execute(Instance instance) {
                _repository.unregister(instance);
                RegistryReplicationManager.INSTANCE.replicate(new UnregisterTask(instance));
                return null;
            }
        });

        return new UnregisterResponse(result.failedInstances(), result.responseStatus());
    }

}
