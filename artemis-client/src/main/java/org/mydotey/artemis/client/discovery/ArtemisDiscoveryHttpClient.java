package org.mydotey.artemis.client.discovery;

import java.util.List;

import org.mydotey.artemis.ResponseStatus;
import org.mydotey.artemis.Service;
import org.mydotey.artemis.client.common.ArtemisClientConfig;
import org.mydotey.artemis.client.common.ArtemisHttpClient;
import org.mydotey.artemis.config.DeploymentConfig;
import org.mydotey.artemis.config.RestPaths;
import org.mydotey.artemis.discovery.DiscoveryConfig;
import org.mydotey.artemis.discovery.LookupRequest;
import org.mydotey.artemis.discovery.LookupResponse;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ArtemisDiscoveryHttpClient extends ArtemisHttpClient {
    public ArtemisDiscoveryHttpClient(final ArtemisClientConfig config) {
        super(config, config.key("discovery"));
    }

    public Service getService(final DiscoveryConfig discoveryConfig) {
        Preconditions.checkArgument(discoveryConfig != null, "discoveryConfig");
        final List<Service> services = getServices(Lists.newArrayList(discoveryConfig));
        if (services.size() > 0) {
            return services.get(0);
        }
        throw new RuntimeException("not found any service by discoveryConfig:" + discoveryConfig);
    }

    public List<Service> getServices(final List<DiscoveryConfig> discoveryConfigs) {
        Preconditions.checkArgument(!CollectionUtils.isEmpty(discoveryConfigs), "discoveryConfigs should not be null or empty");

        final LookupRequest request = new LookupRequest(discoveryConfigs, DeploymentConfig.regionId(), DeploymentConfig.zoneId());
        final LookupResponse response = this.request(RestPaths.DISCOVERY_LOOKUP_FULL_PATH, request, LookupResponse.class);
        ResponseStatus status = response.getResponseStatus();
        logEvent(status, "discovery", "lookup");
        if (ResponseStatusUtil.isSuccess(status))
            return response.getServices();

        throw new RuntimeException("lookup services failed. " + status);
    }
}