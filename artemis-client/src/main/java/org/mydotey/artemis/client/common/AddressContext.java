package org.mydotey.artemis.client.common;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.mydotey.artemis.client.ArtemisClientManagerConfig;
import org.mydotey.artemis.util.StringUtil;
import org.mydotey.java.StringExtension;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Created by fang_j on 10/07/2016.
 */
public class AddressContext {
    private static final Logger _logger = LoggerFactory.getLogger(AddressContext.class);
    private static final String _wsPrefix = "ws://";
    private static final Pattern _httpSchema = Pattern.compile("(^http://|^https://)", Pattern.CASE_INSENSITIVE);
    private final long _createTime = System.currentTimeMillis();
    private final String _httpUrl;
    private final String _webSocketEndpoint;
    private final AtomicBoolean _available = new AtomicBoolean(false);
    private final Property<String, Integer> _ttl;

    public AddressContext(final String clientId, final ArtemisClientManagerConfig managerConfig) {
        this(clientId, managerConfig, StringExtension.EMPTY, StringExtension.EMPTY);
    }

    public AddressContext(final String clientId, final ArtemisClientManagerConfig managerConfig, final String httpUrl,
        final String wsEndpointSuffix) {
        Preconditions.checkArgument(!StringExtension.isBlank(clientId), "clientId");
        Preconditions.checkArgument(managerConfig != null, "manager config");
        _ttl = managerConfig.properties().getIntProperty(clientId + ".address.context-ttl", 60 * 60 * 1000,
            v -> v >= 60 * 1000 && v <= 24 * 60 * 60 * 1000 ? v : null);
        if (StringExtension.isBlank(httpUrl)) {
            _httpUrl = StringExtension.EMPTY;
            _webSocketEndpoint = StringExtension.EMPTY;
        } else {
            _httpUrl = httpUrl;
            _webSocketEndpoint = StringUtil.concatPathParts(_httpSchema.matcher(httpUrl).replaceAll(_wsPrefix),
                wsEndpointSuffix);
            _available.set(true);
        }
    }

    public String getHttpUrl() {
        return _httpUrl;
    }

    public String customHttpUrl(final String path) {
        return StringUtil.concatPathParts(getHttpUrl(), path);
    }

    public String getWebSocketEndPoint() {
        return _webSocketEndpoint;
    }

    public boolean isAavailable() {
        return _available.get();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= (_ttl.getValue() + _createTime);
    }

    public void markUnavailable() {
        if (_available.compareAndSet(true, false)) {
            _logger.info(_httpUrl + " mark unavailable");
        }
    }
}
