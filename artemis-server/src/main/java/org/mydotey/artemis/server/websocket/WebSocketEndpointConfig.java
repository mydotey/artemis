package org.mydotey.artemis.server.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.inject.Inject;

import org.mydotey.artemis.config.WebSocketPaths;
import org.mydotey.artemis.discovery.notify.NotificationCenter;

/**
 * Created by fang_j on 10/07/2016.
 */
@Configuration
@EnableWebSocket
public class WebSocketEndpointConfig implements WebSocketConfigurer {

    @Inject
    private HeartbeatWsHandler heartbeatWsHandler;

    @Inject
    private ServiceChangeWsHandler serviceChangeWsHandler;

    @Inject
    private AllServicesChangeWsHandler allServicesChangeWsHandler;

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        heartbeatWsHandler.start();
        registry.addHandler(heartbeatWsHandler, WebSocketPaths.HEARTBEAT_DESTINATION).setAllowedOrigins("*")
            .addInterceptors(new WsIPBlackList("service-heartbeat"));

        serviceChangeWsHandler.start();
        registry.addHandler(serviceChangeWsHandler, WebSocketPaths.SERVICE_CHANGE_DESTINATION).setAllowedOrigins("*")
            .addInterceptors(new WsIPBlackList("service-discovery"));
        NotificationCenter.getInstance().registerSubscriber(serviceChangeWsHandler);

        allServicesChangeWsHandler.start();
        registry.addHandler(allServicesChangeWsHandler, WebSocketPaths.ALL_SERVICES_CHANGE_DESTINATION)
            .setAllowedOrigins("*")
            .addInterceptors(new WsIPBlackList("service-discoveries"));
        NotificationCenter.getInstance().registerSubscriber(allServicesChangeWsHandler);
    }
}
