package org.mydotey.artemis.client.registry;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.mydotey.artemis.config.RestPaths;
import org.mydotey.artemis.registry.HeartbeatRequest;
import org.mydotey.artemis.registry.HeartbeatResponse;
import org.mydotey.artemis.registry.RegisterRequest;
import org.mydotey.artemis.registry.RegisterResponse;
import org.mydotey.artemis.registry.RegistryService;
import org.mydotey.artemis.registry.UnregisterRequest;
import org.mydotey.artemis.registry.UnregisterResponse;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.StringUtil;
import org.mydotey.codec.json.JacksonJsonCodec;
import org.mydotey.java.StringExtension;
import org.mydotey.rpc.client.http.apache.HttpRequestFactory;
import org.mydotey.rpc.client.http.apache.sync.DynamicPoolingHttpClientProvider;
import org.mydotey.rpc.client.http.apache.sync.HttpRequestExecutors;
import org.mydotey.scf.facade.StringProperties;

import com.google.common.base.Preconditions;

/**
 * Created by fang_j on 10/07/2016.
 */
public class RegistryServiceClient implements RegistryService {
    private final DynamicPoolingHttpClientProvider _clientProvider;
    private final String _url;

    public RegistryServiceClient(final StringProperties properties,
        final String url) {
        Preconditions.checkArgument(properties != null, "properties");
        Preconditions.checkArgument(!StringExtension.isBlank(url), "url");
        _clientProvider = new DynamicPoolingHttpClientProvider(
            "artemis.client.registry-service", properties.getManager());
        _url = url;
    }

    @Override
    public RegisterResponse register(final RegisterRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.registry-service.register",
            () -> {
                final String requestUrl = StringUtil.concatPathParts(_url,
                    RestPaths.REGISTRY_REGISTER_FULL_PATH);
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest,
                    JacksonJsonCodec.DEFAULT, RegisterResponse.class);
            });
    }

    @Override
    public HeartbeatResponse heartbeat(final HeartbeatRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.registry-service.heartbeat",
            () -> {
                final String requestUrl = StringUtil.concatPathParts(_url,
                    RestPaths.REGISTRY_HEARTBEAT_FULL_PATH);
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest,
                    JacksonJsonCodec.DEFAULT, HeartbeatResponse.class);
            });

    }

    @Override
    public UnregisterResponse unregister(final UnregisterRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.registry-service.unregister",
            () -> {
                final String requestUrl = StringUtil.concatPathParts(_url,
                    RestPaths.REGISTRY_UNREGISTER_FULL_PATH);
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest,
                    JacksonJsonCodec.DEFAULT, UnregisterResponse.class);
            });
    }
}
