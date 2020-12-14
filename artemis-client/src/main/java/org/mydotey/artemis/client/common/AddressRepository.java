package org.mydotey.artemis.client.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.mydotey.artemis.client.ArtemisClientManagerConfig;
import org.mydotey.artemis.cluster.GetServiceNodesRequest;
import org.mydotey.artemis.cluster.GetServiceNodesResponse;
import org.mydotey.artemis.cluster.ServiceNode;
import org.mydotey.artemis.config.DeploymentConfig;
import org.mydotey.artemis.config.RangePropertyConfig;
import org.mydotey.artemis.util.DynamicScheduledThread;
import org.mydotey.artemis.util.DynamicScheduledThreadConfig;
import org.mydotey.artemis.util.StringUtil;
import org.mydotey.codec.json.JacksonJsonCodec;
import org.mydotey.java.StringExtension;
import org.mydotey.rpc.client.http.apache.HttpRequestFactory;
import org.mydotey.rpc.client.http.apache.sync.DynamicPoolingHttpClientProvider;
import org.mydotey.rpc.client.http.apache.sync.HttpRequestExecutors;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;

/**
 * Created by fang_j on 10/07/2016.
 */
public class AddressRepository {
    private static final Logger _logger = LoggerFactory.getLogger(AddressRepository.class);
    private final Property<String, String> _domainUrl;
    private final AtomicReference<List<String>> _avlSvcUrls = new AtomicReference<List<String>>();
    private final DynamicPoolingHttpClientProvider _clientProvider;
    private final String _path;
    private final GetServiceNodesRequest _request;
    private final DynamicScheduledThread _addressesPoller;

    public AddressRepository(final String clientId, final ArtemisClientManagerConfig managerConfig, final String path) {
        Preconditions.checkArgument(!StringExtension.isBlank(clientId), "clientId");
        Preconditions.checkArgument(managerConfig != null, "manager config");
        Preconditions.checkArgument(!StringExtension.isBlank(path), "path");
        _clientProvider = new DynamicPoolingHttpClientProvider(
            clientId + ".address.http-client", managerConfig.properties().getManager());
        _path = path;
        _request = new GetServiceNodesRequest(DeploymentConfig.regionId(), DeploymentConfig.zoneId());
        _domainUrl = managerConfig.properties().getStringProperty(clientId + ".service.domain.url", "");
        final DynamicScheduledThreadConfig dynamicScheduledThreadConfig = new DynamicScheduledThreadConfig(
            managerConfig.properties(),
            new RangePropertyConfig<Integer>(20, 0, 200),
            new RangePropertyConfig<Integer>(5 * 60 * 1000, 1 * 60 * 1000, 30 * 60 * 1000));
        _addressesPoller = new DynamicScheduledThread(clientId + ".address-repository", this::refresh,
            dynamicScheduledThreadConfig);
        refresh();
        _addressesPoller.setDaemon(true);
        _addressesPoller.start();
    }

    public String get() {
        final List<String> addressList = _avlSvcUrls.get();
        if (CollectionUtils.isEmpty(addressList)) {
            return _domainUrl.getValue();
        }
        return addressList.get(ThreadLocalRandom.current().nextInt(addressList.size()));
    }

    protected void refresh() {
        try {
            _logger.info("start refresh service urls");
            String domainUrl = _domainUrl.getValue();
            if (StringExtension.isBlank(domainUrl)) {
                _logger.error("domain url should not be null or empty for artemis client");
                return;
            }
            final List<String> urls = getUrlsFromService(domainUrl);
            if (!CollectionUtils.isEmpty(urls)) {
                _avlSvcUrls.set(urls);
            }
        } catch (final Throwable e) {
            _logger.warn("refesh service urls failed", e);
        } finally {
            _logger.info("end refresh service urls");
        }
    }

    private List<String> getUrlsFromService(final String url) {
        final List<String> addressList = new ArrayList<String>();
        try {
            if (StringExtension.isBlank(url)) {
                return addressList;
            }

            final String requestUrl = StringUtil.concatPathParts(url, _path);
            HttpEntityEnclosingRequestBase request = HttpRequestFactory.createRequest(
                requestUrl, HttpPost.METHOD_NAME, _request, JacksonJsonCodec.DEFAULT);
            HttpRequestFactory.gzipRequest(request);
            final GetServiceNodesResponse response = HttpRequestExecutors.execute(
                _clientProvider.get(), request, JacksonJsonCodec.DEFAULT, GetServiceNodesResponse.class);
            if (CollectionUtils.isEmpty(response.getNodes())) {
                return addressList;
            }

            final Set<String> newAddressList = new HashSet<String>();
            for (final ServiceNode node : response.getNodes()) {
                if ((node != null) && !StringExtension.isBlank(node.getUrl())) {
                    String address = node.getUrl();
                    address = StringExtension.trimEnd(address, '/');
                    if (StringExtension.isBlank(address)) {
                        continue;
                    }
                    newAddressList.add(address);
                }
            }
            addressList.addAll(newAddressList);
        } catch (final Throwable e) {
            _logger.error("reset address from service failed", e);
        }
        return addressList;
    }
}
