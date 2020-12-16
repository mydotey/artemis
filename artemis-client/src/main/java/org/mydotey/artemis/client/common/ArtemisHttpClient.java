package org.mydotey.artemis.client.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.ResponseStatus;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.caravan.util.metric.EventMetric;
import org.mydotey.caravan.util.metric.EventMetricManager;
import org.mydotey.caravan.util.metric.MetricConfig;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.codec.json.JacksonJsonCodec;
import org.mydotey.java.StringExtension;
import org.mydotey.java.ThreadExtension;
import org.mydotey.rpc.client.http.apache.HttpRequestFactory;
import org.mydotey.rpc.client.http.apache.sync.DynamicPoolingHttpClientProvider;
import org.mydotey.rpc.client.http.apache.sync.HttpRequestExecutors;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ArtemisHttpClient {
    protected final Logger _logger = LoggerFactory.getLogger(this.getClass());
    private final DynamicPoolingHttpClientProvider _clientProvider;
    private final AddressManager _addressManager;
    private final Property<String, Integer> _httpClientRetryTimes;
    private final Property<String, Integer> _retryInterval;
    private final String _distributionMetricName;
    protected final EventMetricManager _eventMetricManager;

    public ArtemisHttpClient(final ArtemisClientConfig config, final String httpClientId) {
        Preconditions.checkArgument(config != null, "config");
        Preconditions.checkArgument(!StringExtension.isBlank(httpClientId), "httpClientId");
        _clientProvider = new DynamicPoolingHttpClientProvider(httpClientId + ".http-client",
            config.properties().getManager());
        _addressManager = config.addressManager();
        _httpClientRetryTimes = config.properties().getIntProperty(httpClientId + ".http-client.retry-times", 5,
            new RangeValueFilter<>(1, 10));
        _retryInterval = config.properties().getIntProperty(httpClientId + ".http-client.retry-interval", 100,
            new RangeValueFilter<>(0, 1000));
        _distributionMetricName = config.key("http-response.status-code");
        _eventMetricManager = config.eventMetricManager();
    }

    public <T extends HasResponseStatus> T request(final String path, final Object request, final Class<T> clazz) {
        final int retryTimes = _httpClientRetryTimes.getValue().intValue();
        ResponseStatus responseStatus = null;
        for (int i = 0; i < retryTimes; i++) {
            AddressContext context = null;
            try {
                context = _addressManager.getContext();
                String requestUrl = context.customHttpUrl(path);
                HttpEntityEnclosingRequestBase httpRequest = HttpRequestFactory.createRequest(
                    requestUrl, HttpPost.METHOD_NAME, request, JacksonJsonCodec.DEFAULT);
                HttpRequestFactory.gzipRequest(httpRequest);
                T response = HttpRequestExecutors.execute(_clientProvider.get(), httpRequest,
                    JacksonJsonCodec.DEFAULT, clazz);
                if (response == null || response.getResponseStatus() == null)
                    throw new RuntimeException("Got null response or null response status.");

                responseStatus = response.getResponseStatus();
                boolean isServiceDown = ResponseStatusUtil.isServiceDown(responseStatus);
                boolean isRerunnable = ResponseStatusUtil.isRerunnable(responseStatus);
                if (!(isServiceDown || isRerunnable))
                    return response;

                if (isServiceDown)
                    context.markUnavailable();

                _logger.info("get response failed, but can be retried. at turn: " + (i + 1) + ". responseStatus: "
                    + responseStatus);
            } catch (final Throwable e) {
                if (context != null)
                    context.markUnavailable();

                if (i < retryTimes - 1) {
                    _logger.info("get response failed in this turn: " + (i + 1), e);
                } else {
                    _logger.error("与 SOA 注册中心通信时发生错误", e);
                    throw e;
                }
            }

            ThreadExtension.sleep(_retryInterval.getValue());
        }

        throw new RuntimeException("Got failed response: " + responseStatus);
    }

    protected void logEvent(final String service, final String operation) {
        logEvent(null, service, operation);
    }

    protected void logEvent(final ResponseStatus status, final String service, final String operation) {
        final String metricId = _distributionMetricName + "|" + service + "|" + operation;
        final Map<String, String> metadata = Maps.newHashMap();
        metadata.put("metric_name_distribution", _distributionMetricName);
        metadata.put("service", service);
        metadata.put("operation", operation);
        final EventMetric metric = _eventMetricManager.getMetric(metricId, new MetricConfig(metadata));
        if (status == null) {
            metric.addEvent("null");
        } else {
            metric.addEvent(status.getErrorCode());
        }
    }
}
