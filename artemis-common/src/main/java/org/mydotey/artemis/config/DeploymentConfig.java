package org.mydotey.artemis.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;
import org.mydotey.java.net.NetworkInterfaceManager;
import org.mydotey.scf.ConfigurationManager;
import org.mydotey.scf.ConfigurationSource;
import org.mydotey.scf.Property;
import org.mydotey.scf.facade.ConfigurationManagers;
import org.mydotey.scf.facade.StringProperties;
import org.mydotey.scf.facade.SimpleConfigurationSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public final class DeploymentConfig {

    private static final String DEPLOYMENT_ENV_PROPERTY_NAME = "deployment-env";
    private static final String PROPERTIES_FILE_NAME = "application";

    private static final String REGION_ID_PROPERTY_KEY = "region.id";
    private static final String ZONE_ID_PROPERTY_KEY = "zone.id";
    private static final String APPID_PROPERTY_KEY = "app.id";
    private static final String PORT_PROPERTY_KEY = "app.port";
    private static final String PROTOCOL_PROPERTY_KEY = "app.protocol";
    private static final String PATH_PROPERTY_KEY = "app.path";

    private static final Logger _logger = LoggerFactory.getLogger(DeploymentConfig.class);

    private static String _deploymentEnv;

    private static StringProperties _properties;

    static {
        _deploymentEnv = System.getProperty(DEPLOYMENT_ENV_PROPERTY_NAME);
        _deploymentEnv = StringExtension.isBlank(_deploymentEnv) ? null : _deploymentEnv.trim().toLowerCase();

        List<ConfigurationSource> sources = new ArrayList<>();
        sources.add(SimpleConfigurationSources.newEnvironmentVariableSource());
        sources.add(SimpleConfigurationSources.newSystemPropertiesSource());
        if (!StringExtension.isBlank(_deploymentEnv)) {
            sources.add(SimpleConfigurationSources
                .newPropertiesFileSource(PROPERTIES_FILE_NAME + "-" + DeploymentConfig.deploymentEnv()));
        }
        sources.add(SimpleConfigurationSources.newPropertiesFileSource(PROPERTIES_FILE_NAME));
        ConfigurationManager manager = ConfigurationManagers.newManager("deployment", sources);
        _properties = new StringProperties(manager);
    }

    public static StringProperties properties() {
        return _properties;
    }

    private static Property<String, String> _regionIdProperty = _properties.getStringProperty(REGION_ID_PROPERTY_KEY);
    private static Property<String, String> _zoneIdProperty = _properties.getStringProperty(ZONE_ID_PROPERTY_KEY);
    private static Property<String, String> _appIdProperty = _properties.getStringProperty(APPID_PROPERTY_KEY);
    private static Property<String, Integer> _portProperty = _properties.getIntProperty(
        PORT_PROPERTY_KEY, 8080, v -> v >= 1 && v <= 65535 ? v : null);
    private static Property<String, String> _protocolProperty = _properties.getStringProperty(PROTOCOL_PROPERTY_KEY,
        "http");
    private static Property<String, String> _pathProperty = _properties.getStringProperty(PATH_PROPERTY_KEY);

    private static String _regionId;
    private static String _zoneId;
    private static String _appId;
    private static String _machineName;
    private static String _ip;
    private static int _port;
    private static String _protocol;
    private static String _path;

    static {
        _regionId = _regionIdProperty.getValue();
        _zoneId = _zoneIdProperty.getValue();
        _appId = _appIdProperty.getValue();
        _port = _portProperty.getValue().intValue();
        _protocol = _protocolProperty.getValue();
        _path = _pathProperty.getValue();

        _machineName = NetworkInterfaceManager.INSTANCE.hostName();
        _ip = NetworkInterfaceManager.INSTANCE.hostIP();

        logDeploymentInfo();
    }

    private static AtomicBoolean _inited = new AtomicBoolean();

    public static void init(String regionId, String zoneId, String appId, String protocol, int port, String path) {
        ObjectExtension.requireNonBlank(regionId, "region");
        ObjectExtension.requireNonBlank(zoneId, "zone");
        ObjectExtension.requireNonBlank(appId, "appId");
        ObjectExtension.requireNonBlank(protocol, "protocol");

        if (!_inited.compareAndSet(false, true)) {
            _logger.warn("DeploymentConfig init method can be only invoked once!");
            return;
        }

        _regionId = regionId;
        _zoneId = zoneId;
        _appId = appId;

        if (port > 0)
            _port = port;

        _protocol = protocol;
        _path = path;

        logDeploymentInfo();
    }

    public static String deploymentEnv() {
        return _deploymentEnv;
    }

    public static String regionId() {
        return _regionId;
    }

    public static String zoneId() {
        return _zoneId;
    }

    public static String appId() {
        return _appId;
    }

    public static String machineName() {
        return _machineName;
    }

    public static String ip() {
        return _ip;
    }

    public static int port() {
        return _port;
    }

    public static String protocol() {
        return _protocol;
    }

    public static String path() {
        return _path;
    }

    private static void logDeploymentInfo() {
        _logger.info(
            "DeploymentConfig is initialized. deploymentEnv: {}, regionId: {}, zoneId: {}, appId: {}, machineName: {}"
                + ", ip: {}, port: {}, protocol: {}, path: {}",
            _deploymentEnv, _regionId, _zoneId, _appId, _machineName, _ip, _port, _protocol, _path);
    }

    private DeploymentConfig() {

    }

}
