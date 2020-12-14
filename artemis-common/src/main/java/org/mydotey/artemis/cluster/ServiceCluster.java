package org.mydotey.artemis.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.mydotey.artemis.Zone;
import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;
import org.mydotey.scf.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ServiceCluster {

    private static final Logger _logger = LoggerFactory.getLogger(ServiceCluster.class);

    private Property<String, ListMultimap<String, String>> _clusterNodesProperty = ArtemisConfig
        .getListMultimapProperty("artemis.service.cluster.nodes", ArrayListMultimap.<String, String>create());

    private String _regionId;
    private ListMultimap<Zone, ServiceNode> _clusterNodes = ArrayListMultimap.create();
    private List<ClusterChangeListener> _clusterChangeListeners = new ArrayList<>();

    public ServiceCluster(String regionId) {
        ObjectExtension.requireNonBlank(regionId, "regionId");

        _regionId = regionId;
        _logger.info("ServiceCluster is initialized with regionId: " + _regionId);

        updateClusterNodes();
        _clusterNodesProperty.addChangeListener(event -> {
            updateClusterNodes();
            ClusterChangeEvent clusterChangeEvent = new ClusterChangeEvent();
            for (ClusterChangeListener listener : _clusterChangeListeners) {
                try {
                    listener.onChange(clusterChangeEvent);
                } catch (Throwable ex) {
                    _logger.error("ClusterChangeListener onChange failed.", ex);
                }
            }
        });
    }

    public String regionId() {
        return _regionId;
    }

    public ListMultimap<Zone, ServiceNode> clusterNodes() {
        return _clusterNodes;
    }

    synchronized public void addClusterChangeListener(ClusterChangeListener listener) {
        Objects.requireNonNull(listener, "listener");
        _clusterChangeListeners.add(listener);
    }

    protected void updateClusterNodes() {
        ListMultimap<String, String> zoneIdNodeUrlsMap = _clusterNodesProperty.getValue();
        _logger.info("ClusterNodes setting raw value: " + _clusterNodesProperty.getValue() + ", getValue: "
            + zoneIdNodeUrlsMap);
        ListMultimap<Zone, ServiceNode> clusterNodes = ArrayListMultimap.create();
        for (String zoneId : zoneIdNodeUrlsMap.keySet()) {
            Zone zone = new Zone(_regionId, zoneId);
            for (String serviceUrl : zoneIdNodeUrlsMap.get(zoneId)) {
                if (StringExtension.isBlank(serviceUrl))
                    continue;
                ServiceNode peerNode = new ServiceNode(zone, serviceUrl);
                clusterNodes.put(zone, peerNode);
            }
        }

        if (clusterNodes.size() == 0) {
            _logger.warn("New ClusterNodes is empty. Skip to update");
            return;
        }

        _logger.info("ClusterNodes is updated. from: " + _clusterNodes + ", to: " + clusterNodes);
        _clusterNodes = clusterNodes;
    }

}
