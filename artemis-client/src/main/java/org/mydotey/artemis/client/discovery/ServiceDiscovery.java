package org.mydotey.artemis.client.discovery;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.mydotey.artemis.InstanceChange;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.client.common.ArtemisClientConfig;
import org.mydotey.artemis.client.websocket.WebSocketSessionContext;
import org.mydotey.scf.filter.RangeValueConfig;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.artemis.discovery.DiscoveryConfig;
import org.mydotey.artemis.util.DynamicScheduledThread;
import org.mydotey.artemis.util.DynamicScheduledThreadConfig;
import org.mydotey.codec.json.JacksonJsonCodec;
import org.mydotey.java.StringExtension;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    private final ServiceRepository serviceRepository;
    private final ArtemisDiscoveryHttpClient discoveryHttpClient;
    private final Property<String, Long> ttl;
    private volatile long lastUpdateTime = System.currentTimeMillis();
    private final WebSocketSessionContext sessionContext;
    private final DynamicScheduledThread poller;
    protected final Map<String, DiscoveryConfig> reloadFailedDiscoveryConfigs = Maps.newConcurrentMap();

    public ServiceDiscovery(final ServiceRepository serviceRepository, final ArtemisClientConfig config) {
        Preconditions.checkArgument(serviceRepository != null, "ServiceRepository should not be null");
        this.serviceRepository = serviceRepository;
        this.discoveryHttpClient = new ArtemisDiscoveryHttpClient(config);
        ttl = config.properties().getLongProperty(config.key("service-discovery.ttl"), 15 * 60 * 1000L,
            new RangeValueFilter<>(60 * 1000L, 24 * 60 * 60 * 1000L));
        sessionContext = new WebSocketSessionContext(config) {
            @Override
            protected void afterConnectionEstablished(final WebSocketSession session) {
                subscribe(session);
            }

            @Override
            protected void handleMessage(final WebSocketSession session, final WebSocketMessage<?> message) {
                try {
                    InstanceChange instanceChange = JacksonJsonCodec.DEFAULT
                        .decode(((String) message.getPayload()).getBytes(), InstanceChange.class);
                    onInstanceChange(instanceChange);
                } catch (final Throwable e) {
                    logger.warn("convert message failed", e);
                }
            }
        };
        sessionContext.start();

        final DynamicScheduledThreadConfig dynamicScheduledThreadConfig = new DynamicScheduledThreadConfig(
            config.properties(),
            new RangeValueConfig<Integer>(0, 0, 200),
            new RangeValueConfig<Integer>(60 * 1000, 60 * 1000, 24 * 60 * 60 * 1000));
        poller = new DynamicScheduledThread(config.key("service-discovery"), () -> {
            try {
                reload(getReloadDiscoveryConfigs());
            } catch (Throwable t) {
                logger.warn("reload services failed", t);
            }
        }, dynamicScheduledThreadConfig);
        poller.setDaemon(true);
        poller.start();
    }

    public void registerDiscoveryConfig(DiscoveryConfig config) {
        subscribe(sessionContext.get(), config);
    }

    public Service getService(DiscoveryConfig config) {
        return discoveryHttpClient.getService(config);
    }

    protected void onServiceChange(Service service) {
        serviceRepository.update(service);
    }

    protected void onInstanceChange(InstanceChange instanceChange) {
        final String serviceId = instanceChange.getInstance().getServiceId();
        if (InstanceChange.ChangeType.RELOAD.equals(instanceChange.getChangeType())) {
            reload(serviceRepository.getDiscoveryConfig(serviceId));
        } else {
            serviceRepository.update(instanceChange);
        }
    }

    protected List<DiscoveryConfig> getReloadDiscoveryConfigs() {
        if (expired()) {
            return serviceRepository.getDiscoveryConfigs();
        }

        Map<String, DiscoveryConfig> discoveryConfigs = Maps.newHashMap(reloadFailedDiscoveryConfigs);
        List<DiscoveryConfig> configs = Lists.newArrayList(discoveryConfigs.values());
        for (ServiceContext serviceContext : serviceRepository.getServices()) {
            if (discoveryConfigs.containsKey(serviceContext.getDiscoveryConfig().getServiceId())
                || serviceContext.isAvailable()) {
                continue;
            }
            configs.add(serviceContext.getDiscoveryConfig());
        }

        return configs;
    }

    protected void reload(DiscoveryConfig... configs) {
        reload(Lists.newArrayList(configs));
    }

    protected void reload(List<DiscoveryConfig> configs) {
        try {
            if (CollectionUtils.isEmpty(configs))
                return;

            logger.info("start reload services.");
            List<Service> services = discoveryHttpClient.getServices(configs);
            for (Service service : services) {
                if (service == null) {
                    continue;
                }
                final String serviceId = service.getServiceId();
                if (StringExtension.isBlank(serviceId)) {
                    continue;
                }
                onServiceChange(service);
                reloadFailedDiscoveryConfigs.remove(serviceId.toLowerCase());
            }

            lastUpdateTime = System.currentTimeMillis();
            logger.info("end reload services");
        } catch (Throwable t) {
            for (DiscoveryConfig config : configs) {
                if (config == null) {
                    continue;
                }
                final String serviceId = config.getServiceId();
                if (StringExtension.isBlank(serviceId)) {
                    continue;
                }
                reloadFailedDiscoveryConfigs.put(serviceId.toLowerCase(), config);
            }
            throw t;
        }
    }

    protected void subscribe(final WebSocketSession session) {
        try {
            for (final DiscoveryConfig discoveryConfig : serviceRepository.getDiscoveryConfigs()) {
                subscribe(session, discoveryConfig);
            }
        } catch (final Throwable e) {
            logger.warn("subscribe services failed", e);
        }
    }

    protected void subscribe(final WebSocketSession session, final DiscoveryConfig discoveryConfig) {
        try {
            if (discoveryConfig == null) {
                return;
            }
            if (session == null) {
                return;
            }
            final TextMessage message = new TextMessage(new String(
                JacksonJsonCodec.DEFAULT.encode(discoveryConfig)));
            session.sendMessage(message);
        } catch (final Throwable e) {
            logger.warn("subscribe service failed", e);
        }
    }

    protected boolean expired() {
        return System.currentTimeMillis() - lastUpdateTime >= ttl.getValue();
    }
}
