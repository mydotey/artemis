package org.mydotey.artemis.registry.replication;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.artemis.config.RestPaths;
import org.mydotey.artemis.registry.HeartbeatRequest;
import org.mydotey.artemis.registry.HeartbeatResponse;
import org.mydotey.artemis.registry.RegisterRequest;
import org.mydotey.artemis.registry.RegisterResponse;
import org.mydotey.artemis.registry.UnregisterRequest;
import org.mydotey.artemis.registry.UnregisterResponse;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.codec.json.JacksonJsonCodec;
import org.mydotey.java.io.file.FileExtension;
import org.mydotey.rpc.client.http.apache.HttpRequestFactory;
import org.mydotey.rpc.client.http.apache.sync.DynamicPoolingHttpClientProvider;
import org.mydotey.rpc.client.http.apache.sync.HttpRequestExecutors;
import org.mydotey.scf.Property;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class RegistryReplicationServiceClient implements RegistryReplicationService {

    private static DynamicPoolingHttpClientProvider _clientProvider = new DynamicPoolingHttpClientProvider(
        "artemis.service.registry.replication", ArtemisConfig.properties().getManager());

    private static Property<String, Integer> _heartbeatSocketTimeoutProperty = ArtemisConfig.properties()
        .getIntProperty("artemis.service.registry.replication.heartbeat.client.socket-timeout", 200,
            new RangeValueFilter<>(50, 5 * 1000));

    private static Property<String, Integer> _getApplicationsSocketTimeoutProperty = ArtemisConfig.properties()
        .getIntProperty("artemis.service.registry.replication.get-applications.client.socket-timeout", 2000,
            new RangeValueFilter<>(100, 60 * 1000));

    private String _serviceUrl;

    public RegistryReplicationServiceClient(String serviceUrl) {
        _serviceUrl = serviceUrl;
    }

    @Override
    public HeartbeatResponse heartbeat(final HeartbeatRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.registry.replication.heartbeat",
            () -> {
                String requestUrl = FileExtension.concatPathParts(_serviceUrl,
                    RestPaths.REPLICATION_REGISTRY_HEARTBEAT_FULL_PATH);
                RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(_heartbeatSocketTimeoutProperty.getValue().intValue()).build();
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                httpRequest.setConfig(config);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest, JacksonJsonCodec.DEFAULT,
                    HeartbeatResponse.class);
            });
    }

    @Override
    public RegisterResponse register(final RegisterRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.registry.replication.register",
            () -> {
                String requestUrl = FileExtension.concatPathParts(_serviceUrl,
                    RestPaths.REPLICATION_REGISTRY_REGISTER_FULL_PATH);
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest, JacksonJsonCodec.DEFAULT,
                    RegisterResponse.class);
            });
    }

    @Override
    public UnregisterResponse unregister(final UnregisterRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.registry.replication.unregister",
            () -> {
                String requestUrl = FileExtension.concatPathParts(_serviceUrl,
                    RestPaths.REPLICATION_REGISTRY_UNREGISTER_FULL_PATH);
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest, JacksonJsonCodec.DEFAULT,
                    UnregisterResponse.class);
            });
    }

    @Override
    public GetServicesResponse getServices(final GetServicesRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.registry.replication.get-applications",
            () -> {
                String requestUrl = FileExtension.concatPathParts(_serviceUrl,
                    RestPaths.REPLICATION_REGISTRY_GET_SERVICES_FULL_PATH);
                RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(_getApplicationsSocketTimeoutProperty.getValue().intValue())
                    .build();
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                httpRequest.setConfig(config);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest, JacksonJsonCodec.DEFAULT,
                    GetServicesResponse.class);
            });
    }

}
