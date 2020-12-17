package org.mydotey.artemis.server.websocket;

import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by fang_j on 10/07/2016.
 */
public class WsIPBlackList implements HandshakeInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(WsIPBlackList.class);
    private final Property<String, Boolean> ipBlackListEnabled;
    private final Property<String, List<String>> ipBlackList;

    public WsIPBlackList(final String ipBlackListId) {
        ValueCheckers.notNullOrWhiteSpace(ipBlackListId, "ipBlackListId");
        ipBlackListEnabled = ArtemisConfig.properties()
            .getBooleanProperty("artemis.service." + ipBlackListId + ".ws-ip.black-list.enabled", true);
        ipBlackList = ArtemisConfig.properties()
            .getListProperty("artemis.service." + ipBlackListId + ".ws-ip.black-list", new ArrayList<String>());
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
        Map<String, Object> attributes) throws Exception {
        try {
            if (ipBlackListEnabled.getValue()) {
                String ip = InetSocketAddressHelper.getRemoteIP(request);
                for (String blackIp : ipBlackList.getValue()) {
                    if (ip.equalsIgnoreCase(blackIp)) {
                        return false;
                    }
                }
            }
        } catch (Throwable ex) {
            logger.warn("process WebSocket ip black list failed" + ex.getMessage(), ex);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
        Exception exception) {
    }
}
