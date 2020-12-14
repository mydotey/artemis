package org.mydotey.artemis.web.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import org.mydotey.artemis.metric.MetricLoggerHelper;
import org.mydotey.artemis.registry.HeartbeatRequest;
import org.mydotey.artemis.registry.HeartbeatResponse;
import org.mydotey.artemis.registry.RegistryService;
import org.mydotey.artemis.registry.RegistryServiceImpl;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.artemis.util.StringUtil;
import org.mydotey.codec.json.JacksonJsonCodec;

/**
 * Created by fang_j on 10/07/2016.
 */
public class HeartbeatWsHandler extends MetricWsHandler {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatWsHandler.class);
    private static final TextMessage defaultMessage;
    private final RegistryService registryService = RegistryServiceImpl.getInstance();

    public HeartbeatWsHandler() {
        super();
    }

    static {
        final HeartbeatResponse response = new HeartbeatResponse();
        response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
        defaultMessage = new TextMessage(StringUtil.toJson(response));
    }

    @Override
    protected void handleTextMessage(final WebSocketSession session, final TextMessage message) throws Exception {
        TextMessage returnMessage = null;
        try {
            final HeartbeatResponse response = registryService
                .heartbeat(JacksonJsonCodec.DEFAULT.decode(message.getPayload().getBytes(), HeartbeatRequest.class));
            returnMessage = new TextMessage(StringUtil.toJson(response));
            MetricLoggerHelper.logRegistryEvent(response.getResponseStatus().getErrorCode());
        } catch (final Exception e) {
            logger.error("convert heartbeat message failed", e);
        }
        if (returnMessage != null) {
            session.sendMessage(returnMessage);
        } else {
            session.sendMessage(defaultMessage);
        }
    }

    @Override
    public String name() {
        return "registry";
    }
}
