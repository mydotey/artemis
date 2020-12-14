package org.mydotey.artemis.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mydotey.java.StringExtension;
import org.mydotey.java.net.NetworkInterfaceManager;
import org.mydotey.scf.ConfigurationManager;
import org.mydotey.scf.ConfigurationManagerConfig;
import org.mydotey.scf.ConfigurationSource;
import org.mydotey.scf.Property;
import org.mydotey.scf.facade.ConfigurationManagers;
import org.mydotey.scf.facade.StringProperties;
import org.mydotey.scf.facade.StringPropertySources;
import org.mydotey.scf.source.stringproperty.StringPropertyConfigurationSource;
import org.mydotey.scf.source.stringproperty.cascaded.CascadedConfigurationSourceConfig;
import org.mydotey.scf.source.stringproperty.propertiesfile.PropertiesFileConfigurationSource;
import org.mydotey.scf.source.stringproperty.propertiesfile.PropertiesFileConfigurationSourceConfig;

import com.google.common.collect.ListMultimap;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class ArtemisConfig {

    private static final String PROPERTIES_FILE_NAME = "artemis";

    private static final String[] CASCADED_FACTORS = new String[] { NetworkInterfaceManager.INSTANCE.localhostIP() };

    private static StringProperties _properties;

    static {
        String sourceFileName = PROPERTIES_FILE_NAME;
        PropertiesFileConfigurationSourceConfig sourceConfig = StringPropertySources
            .newPropertiesFileSourceConfigBuilder()
            .setName(sourceFileName).setFileName(sourceFileName).build();
        PropertiesFileConfigurationSource source = StringPropertySources.newPropertiesFileSource(sourceConfig);
        List<ConfigurationSource> sources = new ArrayList<>();
        sources.add(source);
        if (!StringExtension.isBlank(DeploymentConfig.deploymentEnv())) {
            sourceFileName = PROPERTIES_FILE_NAME + "-" + DeploymentConfig.deploymentEnv();
            sourceConfig = StringPropertySources.newPropertiesFileSourceConfigBuilder()
                .setName(sourceFileName).setFileName(sourceFileName).build();
            source = StringPropertySources.newPropertiesFileSource(sourceConfig);
            sources.add(source);
        }

        for (int i = 0; i < sources.size(); i++) {
            StringPropertyConfigurationSource source2 = (StringPropertyConfigurationSource) sources.get(0);
            CascadedConfigurationSourceConfig<PropertiesFileConfigurationSourceConfig> cascadedConfigurationSourceConfig = StringPropertySources
                .<PropertiesFileConfigurationSourceConfig>newCascadedSourceConfigBuilder()
                .setKeySeparator(".").addCascadedFactors(Arrays.asList(CASCADED_FACTORS))
                .setName("cascaded-" + source2.getConfig().getName()).setSource(source2).build();
            sources.set(i, StringPropertySources.newCascadedSource(cascadedConfigurationSourceConfig));
        }

        sources.add(StringPropertySources.newEnvironmentVariableSource("environment-variables"));

        ConfigurationManagerConfig.Builder managerConfigBuilder = ConfigurationManagers.newConfigBuilder()
            .setName("artemis");
        for (int i = 0; i < sources.size(); i++) {
            managerConfigBuilder.addSource(i, sources.get(i));
        }
        ConfigurationManager manager = ConfigurationManagers.newManager(managerConfigBuilder.build());
        _properties = new StringProperties(manager);
    }

    public static StringProperties properties() {
        return _properties;
    }

    public static Property<String, ListMultimap<String, String>> getListMultimapProperty(String key,
        ListMultimap<String, String> defaultValue) {
        return _properties.getProperty(key, defaultValue, ListMultimapConverter.DEFAULT);
    }

}
