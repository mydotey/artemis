package org.mydotey.artemis.status;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.artemis.config.RangeValueFilter;
import org.mydotey.artemis.config.RestPaths;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.StringUtil;
import org.mydotey.codec.json.JacksonJsonCodec;
import org.mydotey.java.ObjectExtension;
import org.mydotey.rpc.client.http.apache.HttpRequestFactory;
import org.mydotey.rpc.client.http.apache.sync.DynamicPoolingHttpClientProvider;
import org.mydotey.rpc.client.http.apache.sync.HttpRequestExecutors;
import org.mydotey.scf.Property;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class StatusServiceClient implements StatusService {

    private static DynamicPoolingHttpClientProvider _clientProvider = new DynamicPoolingHttpClientProvider(
        "artemis.service.registry.status", ArtemisConfig.properties().getManager());

    private static Property<String, Integer> _getLeasesSocketTimeoutProperty = ArtemisConfig.properties()
        .getIntProperty("artemis.service.registry.status.get-leases.client.socket-timeout", 10 * 1000,
            new RangeValueFilter<>(100, 300 * 1000));

    private static Property<String, Integer> _getClusterNodeStatusSocketTimeoutProperty = ArtemisConfig.properties()
        .getIntProperty("artemis.service.registry.status.get-cluster-node.client.socket-timeout", 200,
            new RangeValueFilter<>(100, 10 * 1000));

    private String _serviceUrl;

    public StatusServiceClient(String serviceUrl) {
        ObjectExtension.requireNonBlank(serviceUrl, "serviceUrl");
        _serviceUrl = serviceUrl;
    }

    @Override
    public GetClusterNodeStatusResponse getClusterNodeStatus(final GetClusterNodeStatusRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.status.get-cluster-node",
            () -> {
                String requestUrl = StringUtil.concatPathParts(_serviceUrl, RestPaths.STATUS_NODE_FULL_PATH);
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest, JacksonJsonCodec.DEFAULT,
                    GetClusterNodeStatusResponse.class);
            });
    }

    @Override
    public GetClusterStatusResponse getClusterStatus(final GetClusterStatusRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.status.get-cluster",
            () -> {
                String requestUrl = StringUtil.concatPathParts(_serviceUrl,
                    RestPaths.STATUS_CLUSTER_FULL_PATH);
                RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(_getClusterNodeStatusSocketTimeoutProperty.getValue().intValue()).build();
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                httpRequest.setConfig(config);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest, JacksonJsonCodec.DEFAULT,
                    GetClusterStatusResponse.class);
            });
    }

    @Override
    public GetLeasesStatusResponse getLeasesStatus(final GetLeasesStatusRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.status.get-leases",
            () -> {
                String requestUrl = StringUtil.concatPathParts(_serviceUrl, RestPaths.STATUS_LEASES_FULL_PATH);
                RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(_getLeasesSocketTimeoutProperty.getValue().intValue()).build();
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                httpRequest.setConfig(config);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest, JacksonJsonCodec.DEFAULT,
                    GetLeasesStatusResponse.class);
            });
    }

    @Override
    public GetLeasesStatusResponse getLegacyLeasesStatus(final GetLeasesStatusRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.status.get-legacy-leases",
            () -> {
                String requestUrl = StringUtil.concatPathParts(_serviceUrl,
                    RestPaths.STATUS_LEGACY_LEASES_FULL_PATH);
                RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(_getLeasesSocketTimeoutProperty.getValue().intValue()).build();
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                httpRequest.setConfig(config);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest, JacksonJsonCodec.DEFAULT,
                    GetLeasesStatusResponse.class);
            });
    }

    @Override
    public GetConfigStatusResponse getConfigStatus(final GetConfigStatusRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.status.get-config",
            () -> {
                String requestUrl = StringUtil.concatPathParts(_serviceUrl, RestPaths.STATUS_CONFIG_FULL_PATH);
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest, JacksonJsonCodec.DEFAULT,
                    GetConfigStatusResponse.class);
            });
    }

    @Override
    public GetDeploymentStatusResponse getDeploymentStatus(final GetDeploymentStatusRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.client.status.get-deployment",
            () -> {
                String requestUrl = StringUtil.concatPathParts(_serviceUrl,
                    RestPaths.STATUS_DEPLOYMENT_FULL_PATH);
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                return HttpRequestExecutors.execute(_clientProvider.get(), httpRequest, JacksonJsonCodec.DEFAULT,
                    GetDeploymentStatusResponse.class);
            });
    }

}
