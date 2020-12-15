package org.mydotey.artemis.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.mydotey.artemis.config.ArtemisConfig;
import org.mydotey.scf.filter.RangeValueFilter;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.ServiceNodeUtil;
import org.mydotey.java.BooleanExtension;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.ThreadExtension;
import org.mydotey.java.net.NetworkInterfaceManager;
import org.mydotey.scf.Property;
import org.mydotey.scf.PropertyChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public final class NodeManager {

    public static final NodeManager INSTANCE = new NodeManager();

    private static final Logger _logger = LoggerFactory.getLogger(NodeManager.class);

    private Property<String, Boolean> _statusForceUpProperty = ArtemisConfig.properties().getBooleanProperty(
        "artemis.service.cluster.node.status.force-up",
        false);

    private Property<String, Boolean> _serviceRegistryForceUpProperty = ArtemisConfig.properties()
        .getBooleanProperty("artemis.service.cluster.node.status.registry.force-up", false);

    private Property<String, Boolean> _serviceDiscoveryForceUpProperty = ArtemisConfig.properties()
        .getBooleanProperty("artemis.service.cluster.node.status.discovery.force-up", false);

    private Property<String, Boolean> _statusForceDownProperty = ArtemisConfig.properties()
        .getBooleanProperty(
            "artemis.service.cluster.node.status.force-down." + NetworkInterfaceManager.INSTANCE.hostIP(), false);

    private Property<String, Boolean> _serviceRegistryForceDownProperty = ArtemisConfig.properties()
        .getBooleanProperty(
            "artemis.service.cluster.node.status.registry.force-down." + NetworkInterfaceManager.INSTANCE.hostIP(),
            false);

    private Property<String, Boolean> _serviceDiscoveryForceDownProperty = ArtemisConfig.properties()
        .getBooleanProperty("artemis.service.cluster.node.status.discovery.force-down."
            + NetworkInterfaceManager.INSTANCE.hostIP(), false);

    private Property<String, Integer> _initSyncIntervalProperty = ArtemisConfig.properties()
        .getIntProperty("artemis.service.cluster.node.init.sync-interval", 1000,
            new RangeValueFilter<>(50, 600 * 1000));

    private Property<String, Boolean> _allowRegistryFromOtherZoneProperty = ArtemisConfig.properties()
        .getBooleanProperty("artemis.service.registry.allow-from-other-zone", false);

    private Property<String, Boolean> _allowDiscoveryFromOtherZoneProperty = ArtemisConfig.properties()
        .getBooleanProperty("artemis.service.discovery.allow-from-other-zone", false);

    private ServiceNodeStatus _nodeStatus;

    private List<NodeInitializer> _initializers = new ArrayList<>();

    private NodeManager() {

    }

    public ServiceNodeStatus nodeStatus() {
        return _nodeStatus;
    }

    public void init(List<NodeInitializer> initializers) {
        _nodeStatus = ServiceNodeUtil.newUnknownNodeStatus(ClusterManager.INSTANCE.localNode());
        _nodeStatus.setStatus(ServiceNodeStatus.Status.STARTING);
        updateNodeStatus();

        Consumer<PropertyChangeEvent<String, Boolean>> listener = event -> updateNodeStatus();
        _statusForceUpProperty.addChangeListener(listener);
        _serviceRegistryForceUpProperty.addChangeListener(listener);
        _serviceDiscoveryForceUpProperty.addChangeListener(listener);
        _statusForceDownProperty.addChangeListener(listener);
        _serviceRegistryForceDownProperty.addChangeListener(listener);
        _serviceDiscoveryForceDownProperty.addChangeListener(listener);
        _allowRegistryFromOtherZoneProperty.addChangeListener(listener);
        _allowDiscoveryFromOtherZoneProperty.addChangeListener(listener);

        _initializers.add(RegistryReplicationInitializer.INSTANCE);
        if (initializers != null)
            _initializers.addAll(initializers);

        initAsync();
    }

    private void initAsync() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    if (ServiceNodeUtil.isUp(_nodeStatus))
                        return;

                    try {
                        ArtemisTraceExecutor.INSTANCE.execute("artemis.service.cluster.node.init.sync-data",
                            () -> {
                                if (ServiceNodeUtil.isDown(_nodeStatus))
                                    return;

                                executeInitializers();
                            });
                    } catch (Throwable ex) {
                        _logger.error("Exception happen in NodeManage init.", ex);
                    }

                    ThreadExtension.sleep(_initSyncIntervalProperty.getValue().intValue());
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void updateNodeStatus() {
        if (_statusForceUpProperty.getValue().booleanValue()) {
            _nodeStatus.setStatus(ServiceNodeStatus.Status.UP);
            _nodeStatus.setCanServiceDiscovery(true);
            _nodeStatus.setCanServiceRegistry(true);
            _logger.warn("Node status is force up by config.");
        }

        if (_statusForceDownProperty.getValue().booleanValue()) {
            _nodeStatus.setStatus(ServiceNodeStatus.Status.DOWN);
            _logger.warn("Node status is force down by config.");
        }

        if (_serviceRegistryForceUpProperty.getValue().booleanValue()) {
            _nodeStatus.setCanServiceRegistry(true);
            _logger.warn("Node registry status is force up by config.");
        }

        if (_serviceRegistryForceDownProperty.getValue().booleanValue()) {
            _nodeStatus.setCanServiceRegistry(false);
            _logger.warn("Node registry status is force down by config.");
        }

        if (_serviceDiscoveryForceUpProperty.getValue().booleanValue()) {
            _nodeStatus.setCanServiceDiscovery(true);
            _logger.warn("Node status discovery is force up by config.");
        }

        if (_serviceDiscoveryForceDownProperty.getValue().booleanValue()) {
            _nodeStatus.setCanServiceDiscovery(false);
            _logger.warn("Node status discovery is force down by config.");
        }

        _nodeStatus.setAllowRegistryFromOtherZone(_allowRegistryFromOtherZoneProperty.getValue().booleanValue());
        _nodeStatus.setAllowDiscoveryFromOtherZone(_allowDiscoveryFromOtherZoneProperty.getValue().booleanValue());
    }

    private void executeInitializers() {
        boolean discoveryInitSuccess = true;
        if (!BooleanExtension.isFalse(_nodeStatus.isCanServiceDiscovery())) {
            discoveryInitSuccess = executeInitializers(NodeInitializer.TargetType.DISCOVERY);
            _logger.info("Discovery initializers execute result: " + discoveryInitSuccess);
        }

        if (!BooleanExtension.isTrue(_nodeStatus.isCanServiceRegistry())) {
            boolean success = executeInitializers(NodeInitializer.TargetType.REGISTRY);
            _logger.info("Registry initializers execute result: " + discoveryInitSuccess);
            if (success)
                _nodeStatus.setCanServiceRegistry(true);
        }

        if (!BooleanExtension.isTrue(_nodeStatus.isCanServiceDiscovery()) && discoveryInitSuccess
            && BooleanExtension.isTrue(_nodeStatus.isCanServiceRegistry()))
            _nodeStatus.setCanServiceDiscovery(true);

        if (!ServiceNodeUtil.isUp(_nodeStatus) && !ServiceNodeUtil.isDown(_nodeStatus)
            && BooleanExtension.isTrue(_nodeStatus.isCanServiceRegistry())
            && BooleanExtension.isTrue(_nodeStatus.isCanServiceDiscovery()))
            _nodeStatus.setStatus(ServiceNodeStatus.Status.UP);

        _logger.info("NodeManager inited. NodeStatus: " + _nodeStatus);
    }

    private boolean executeInitializers(NodeInitializer.TargetType target) {
        ObjectExtension.requireNonNull(target, "target");

        boolean success = true;
        for (NodeInitializer initializer : _initializers) {
            if (!target.equals(initializer.target()))
                continue;

            try {
                boolean itemSucess = initializer.initialize();
                success = success && itemSucess;
            } catch (Throwable ex) {
                success = false;
                _logger.error("Initializer failed. Target: " + initializer, ex);
            }
        }

        return success;
    }

}
