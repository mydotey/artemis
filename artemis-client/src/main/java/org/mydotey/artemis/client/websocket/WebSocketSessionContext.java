package org.mydotey.artemis.client.websocket;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

import org.mydotey.artemis.client.common.AddressContext;
import org.mydotey.artemis.client.common.AddressManager;
import org.mydotey.artemis.client.common.ArtemisClientConfig;
import org.mydotey.scf.filter.RangeValueConfig;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.caravan.util.ratelimiter.RateLimiter;
import org.mydotey.caravan.util.ratelimiter.RateLimiterConfig;
import org.mydotey.caravan.util.concurrent.DynamicScheduledThread;
import org.mydotey.caravan.util.concurrent.DynamicScheduledThreadConfig;
import org.mydotey.scf.Property;
import org.mydotey.util.TimeSequenceCircularBufferConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.google.common.base.Preconditions;

/**
 * Created by fang_j on 10/07/2016.
 */
public abstract class WebSocketSessionContext {
    private static final Logger _logger = LoggerFactory.getLogger(WebSocketSessionContext.class);
    private final Property<String, Long> _ttl;
    private final Property<String, Long> _connectTimeout;
    private final Property<String, Long> pingTimeout;
    private final AtomicReference<WebSocketSession> _session = new AtomicReference<WebSocketSession>();
    private volatile long _lastUpdatedTime = System.currentTimeMillis();
    private final AtomicBoolean _isConnecting = new AtomicBoolean(false);
    private final WebSocketClient _wsClient;
    private final WebSocketHandler _handler;
    private final DynamicScheduledThread _healthChecker;
    private final AddressManager _addressManager;
    private final AtomicReference<AddressContext> _addressContext = new AtomicReference<AddressContext>();
    private final AtomicBoolean _isChecking = new AtomicBoolean(false);
    private final Object receivePong = new Object();
    private final RateLimiter rateLimiter;
    private final Property<String, Integer> defaultMaxTextMessageBufferSize;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public WebSocketSessionContext(final ArtemisClientConfig config) {
        Preconditions.checkArgument(config != null, "config");
        _ttl = config.properties().getLongProperty(config.key("websocket-session.ttl"), 5 * 60 * 1000L,
            new RangeValueFilter<>(5 * 60 * 1000L, 30 * 60 * 1000L));
        _connectTimeout = config.properties().getLongProperty(config.key("websocket-session.connect-timeout"),
            5 * 1000L, new RangeValueFilter<>(1000L, 30 * 1000L));
        pingTimeout = config.properties().getLongProperty(config.key("websocket-session.ping-timeout"), 1000L,
            new RangeValueFilter<>(50L, 10 * 1000L));
        defaultMaxTextMessageBufferSize = config.properties()
            .getIntProperty(config.key("websocket-session.text-message.buffer-size"), 8,
                new RangeValueFilter<>(8, 32));
        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxTextMessageBufferSize(defaultMaxTextMessageBufferSize.getValue() * 1024);
        _wsClient = new StandardWebSocketClient(container);
        _addressManager = config.addressManager();
        _addressContext.set(_addressManager.getContext());
        _handler = new WebSocketHandler() {
            @Override
            public void afterConnectionEstablished(final WebSocketSession session)
                throws Exception {
            }

            @Override
            public void handleMessage(final WebSocketSession session,
                final WebSocketMessage<?> message) throws Exception {
                if (message instanceof TextMessage) {
                    WebSocketSessionContext.this.handleMessage(session, message);
                } else if (message instanceof PongMessage) {
                    synchronized (receivePong) {
                        receivePong.notifyAll();
                    }
                }
            }

            @Override
            public void handleTransportError(final WebSocketSession session,
                final Throwable exception) throws Exception {
                markdown();
                _logger.error("WebSocketSession transport error", exception);
            }

            @Override
            public void afterConnectionClosed(final WebSocketSession session,
                final CloseStatus closeStatus) throws Exception {
                _logger.info("WebSocketSession closed: " + closeStatus);
                checkHealth();
            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }
        };
        rateLimiter = config.getRateLimiterManager().getRateLimiter(config.key("websocket-session.reconnect-times"),
            new RateLimiterConfig(true, new RangeValueConfig<Long>(5L, 3L, 60L),
                new TimeSequenceCircularBufferConfig.Builder().setTimeWindow(20 * 1000).setBucketTtl(2 * 1000)
                    .build()));

        final DynamicScheduledThreadConfig dynamicScheduledThreadConfig = new DynamicScheduledThreadConfig(
            config.properties(),
            new RangeValueConfig<Integer>(20, 0, 200), new RangeValueConfig<Integer>(1000, 100, 10 * 60 * 1000));
        _healthChecker = new DynamicScheduledThread(config.key("websocket-session.health-check"), new Runnable() {
            @Override
            public void run() {
                WebSocketSessionContext.this.checkHealth();
            }
        }, dynamicScheduledThreadConfig);
        _healthChecker.setDaemon(true);
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            _healthChecker.start();
        }
    }

    protected void connect() {
        if (_isConnecting.compareAndSet(false, true)) {
            try {
                if (rateLimiter.isRateLimited("connect")) {
                    _logger.error("WebSocketSessionContext reconnect times exceed expected value for a period time");
                    return;
                }
                final AddressContext context = _addressManager.getContext();
                if (!context.isAavailable()) {
                    return;
                }
                ListenableFuture<WebSocketSession> future = _wsClient.doHandshake(_handler,
                    context.getWebSocketEndPoint());
                future.addCallback(new WebSocketSessionCallback(this, context));
                try {
                    WebSocketSession session = future.get(_connectTimeout.getValue(), TimeUnit.MILLISECONDS);
                    final WebSocketSession oldSession = _session.getAndSet(session);
                    _lastUpdatedTime = System.currentTimeMillis();
                    _addressContext.set(context);
                    disconnect(oldSession);
                    WebSocketSessionContext.this.afterConnectionEstablished(session);
                    _logger.info("WebSocketSessionContext connected to: " + context.getWebSocketEndPoint());
                } catch (Throwable ex) {
                    context.markUnavailable();
                    _logger.warn("get WebSocketSession failed within the time specified", ex);
                }
            } catch (final Throwable e) {
                _addressContext.get().markUnavailable();
                _logger.warn("connect to websocket endpoint failed", e);
            } finally {
                _isConnecting.set(false);
            }
        }
    }

    protected synchronized void reset(WebSocketSession session, AddressContext context) {
        if (!context.isAavailable()) {
            disconnect(session);
            _logger.warn("WebSocketSession is not available now");
        }
    }

    public static void disconnect(final WebSocketSession session) {
        try {
            if (session != null) {
                if (session.isOpen()) {
                    session.close();
                }
            }
        } catch (final Throwable e) {
            _logger.error(" disconnect the WebSocketSession failed", e);
        }
    }

    protected boolean isAvailable() {
        WebSocketSession session = _session.get();
        return (session != null) && session.isOpen() && isAlive();
    }

    private boolean isAlive() {
        try {
            _session.get().sendMessage(new PingMessage());
            long start = System.currentTimeMillis();
            long timeout = pingTimeout.getValue();
            synchronized (receivePong) {
                receivePong.wait(timeout);
            }
            if (System.currentTimeMillis() > start + timeout) {
                _logger.info("ping WebSocketSession timeout");
                return false;
            }
            return true;
        } catch (final Throwable e) {
            _logger.warn("ping WebSocketSession failed", e);
        }
        return false;
    }

    protected boolean isExpired() {
        return System.currentTimeMillis() >= (_lastUpdatedTime + _ttl.getValue());
    }

    public WebSocketSession get() {
        return _session.get();
    }

    protected abstract void afterConnectionEstablished(final WebSocketSession session);

    protected abstract void handleMessage(final WebSocketSession session,
        final WebSocketMessage<?> message);

    public void checkHealth() {
        if (_isChecking.compareAndSet(false, true)) {
            try {
                boolean available = _addressContext.get().isAavailable()
                    && !isExpired()
                    && isAvailable();

                if (!available) {
                    connect();
                }
            } catch (final Throwable e) {
                _logger.warn("WebSocketSession check health failed", e);
            } finally {
                _isChecking.set(false);
            }
        }
    }

    public void markdown() {
        _addressContext.get().markUnavailable();
        checkHealth();
    }

    public void shutdown() {
        _healthChecker.shutdown();
    }
}
