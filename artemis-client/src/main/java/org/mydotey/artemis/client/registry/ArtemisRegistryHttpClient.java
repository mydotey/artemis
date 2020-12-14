package org.mydotey.artemis.client.registry;

import java.util.Set;

import org.mydotey.artemis.Instance;
import org.mydotey.artemis.client.common.ArtemisClientConfig;
import org.mydotey.artemis.client.common.ArtemisHttpClient;
import org.mydotey.artemis.config.RestPaths;
import org.mydotey.artemis.registry.RegisterRequest;
import org.mydotey.artemis.registry.RegisterResponse;
import org.mydotey.artemis.registry.UnregisterRequest;
import org.mydotey.artemis.registry.UnregisterResponse;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.codec.json.JacksonJsonCodec;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ArtemisRegistryHttpClient extends ArtemisHttpClient {
    public ArtemisRegistryHttpClient(final ArtemisClientConfig config) {
        super(config, config.key("registry"));
    }

    public void register(final Set<Instance> instances) {
        try {
            Preconditions.checkArgument(!CollectionUtils.isEmpty(instances), "instances");
            final RegisterRequest request = new RegisterRequest(Lists.newArrayList(instances));
            final RegisterResponse response = this.request(RestPaths.REGISTRY_REGISTER_FULL_PATH, request,
                RegisterResponse.class);
            if (ResponseStatusUtil.isFail(response.getResponseStatus())) {
                _logger
                    .error(
                        "register instances failed. Response:" + new String(JacksonJsonCodec.DEFAULT.encode(response)));
            } else if (ResponseStatusUtil.isPartialFail(response.getResponseStatus())) {
                _logger.warn(
                    "register instances patial failed. Response:"
                        + new String(JacksonJsonCodec.DEFAULT.encode(response)));
            }
            logEvent(response.getResponseStatus(), "registry", "register");
        } catch (final Throwable e) {
            _logger.warn("register instances failed", e);
            logEvent("registry", "register");
        }
    }

    public void unregister(final Set<Instance> instances) {
        try {
            Preconditions.checkArgument(!CollectionUtils.isEmpty(instances), "instances");
            final UnregisterRequest request = new UnregisterRequest(Lists.newArrayList(instances));
            final UnregisterResponse response = this.request(RestPaths.REGISTRY_UNREGISTER_FULL_PATH, request,
                UnregisterResponse.class);
            if (ResponseStatusUtil.isFail(response.getResponseStatus())) {
                _logger.error(
                    "unregister instances failed. Response:" + new String(JacksonJsonCodec.DEFAULT.encode(response)));
            } else if (ResponseStatusUtil.isPartialFail(response.getResponseStatus())) {
                _logger.warn("unregister instances patial failed. Response:"
                    + new String(JacksonJsonCodec.DEFAULT.encode(response)));
            }
            logEvent(response.getResponseStatus(), "registry", "unregister");
        } catch (final Throwable e) {
            _logger.warn("unregister instances failed", e);
            logEvent("registry", "unregister");
        }
    }
}
