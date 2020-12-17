package org.mydotey.artemis.server.websocket;

import org.mydotey.artemis.InstanceChange;
import org.mydotey.artemis.discovery.notify.InstanceChangeSubscriber;
import org.mydotey.artemis.metric.MetricLoggerHelper;
import org.mydotey.artemis.util.StringUtil;
import org.mydotey.java.StringExtension;
import org.mydotey.java.collection.CollectionExtension;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

import javax.inject.Named;

/**
 * Created by fang_j on 10/07/2016.
 */
@Named
public class AllServicesChangeWsHandler extends ArtemisWsHandler implements InstanceChangeSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(AllServicesChangeWsHandler.class);

    @Override
    public void accept(final InstanceChange instanceChange) {
        try {
            if ((instanceChange == null) || (instanceChange.getInstance() == null)) {
                return;
            }

            final String serviceId = instanceChange.getInstance().getServiceId();
            if (StringExtension.isBlank(serviceId)) {
                return;
            }

            final String instanceId = instanceChange.getInstance().getInstanceId();
            final String changeType = instanceChange.getChangeType();

            if (CollectionExtension.isEmpty(sessions)) {
                return;
            }
            final TextMessage message = new TextMessage(StringUtil.toJson(instanceChange));
            List<WebSocketSession> allSessions = Lists.newArrayList(sessions.values());
            for (final WebSocketSession session : allSessions) {
                if (session.isOpen()) {
                    try {
                        synchronized (session) {
                            session.sendMessage(message);
                            MetricLoggerHelper.logPublishEvent("success", serviceId, instanceId, changeType);
                        }
                    } catch (final Exception sendException) {
                        MetricLoggerHelper.logPublishEvent("failed", serviceId, instanceId, changeType);
                        logger.error("websocket session send message failed", sendException);
                        try {
                            session.close();
                        } catch (final Exception closeException) {
                            logger.warn("close websocket session failed", closeException);
                        }
                    }
                }
            }
            logger.info(
                String.format("send instance change message to %d sessions: %s", sessions.size(), instanceChange));
        } catch (final Exception e) {
            logger.error("send instance change failed", e);
        }
    }

    @Override
    public String name() {
        return "discoveries";
    }
}
