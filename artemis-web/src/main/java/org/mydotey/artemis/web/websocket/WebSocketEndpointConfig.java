package org.mydotey.artemis.web.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import org.mydotey.artemis.config.WebSocketPaths;
import org.mydotey.artemis.discovery.notify.NotificationCenter;

/**
 * Created by fang_j on 10/07/2016.
 */
@Configuration
@EnableWebSocket
public class WebSocketEndpointConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        HeartbeatWsHandler heartbeatWsHandler = new HeartbeatWsHandler();
        heartbeatWsHandler.start();
        registry.addHandler(heartbeatWsHandler, WebSocketPaths.HEARTBEAT_DESTINATION).setAllowedOrigins("*")
            .addInterceptors(new WsIPBlackList("service-heartbeat"));

        ServiceChangeWsHandler serviceChangeWsHandler = new ServiceChangeWsHandler();
        serviceChangeWsHandler.start();
        registry.addHandler(serviceChangeWsHandler, WebSocketPaths.SERVICE_CHANGE_DESTINATION).setAllowedOrigins("*")
            .addInterceptors(new WsIPBlackList("service-discovery"));

        AllServicesChangeWsHandler allServicesChangeWsHandler = new AllServicesChangeWsHandler();
        allServicesChangeWsHandler.start();
        registry.addHandler(allServicesChangeWsHandler, WebSocketPaths.ALL_SERVICES_CHANGE_DESTINATION)
            .setAllowedOrigins("*")
            .addInterceptors(new WsIPBlackList("service-discoveries"));

        NotificationCenter.getInstance().registerSubscriber(serviceChangeWsHandler);
        NotificationCenter.getInstance().registerSubscriber(allServicesChangeWsHandler);
    }
}
