package org.mydotey.artemis.config;

import java.util.ArrayList;
import java.util.List;

import org.mydotey.java.StringExtension;
import org.mydotey.java.net.NetworkInterfaceManager;
import org.mydotey.scf.ConfigurationManager;
import org.mydotey.scf.ConfigurationSource;
import org.mydotey.scf.Property;
import org.mydotey.scf.facade.ConfigurationManagers;
import org.mydotey.scf.facade.StringProperties;
import org.mydotey.scf.facade.SimpleConfigurationSources;
import org.mydotey.scf.source.cascaded.CascadedConfigurationSource;
import org.mydotey.scf.source.pipeline.PipelineConfigurationSource;
import com.google.common.collect.ListMultimap;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public final class ArtemisConfig {

    private static final String PROPERTIES_FILE_NAME = "artemis";

    private static final String[] CASCADED_FACTORS = new String[] { NetworkInterfaceManager.INSTANCE.hostIP() };

    private static StringProperties _properties;

    static {
        List<ConfigurationSource> sources = new ArrayList<>();
        sources.add(SimpleConfigurationSources.newEnvironmentVariableSource());
        sources.add(SimpleConfigurationSources.newSystemPropertiesSource());
        if (!StringExtension.isBlank(DeploymentConfig.deploymentEnv())) {
            sources.add(SimpleConfigurationSources
                .newPropertiesFileSource(PROPERTIES_FILE_NAME + "-" + DeploymentConfig.deploymentEnv()));
        }
        sources.add(SimpleConfigurationSources.newPropertiesFileSource(PROPERTIES_FILE_NAME));

        PipelineConfigurationSource pipelineConfigurationSource = SimpleConfigurationSources.newPipelineSource(sources);
        CascadedConfigurationSource cascadedConfigurationSource = SimpleConfigurationSources
            .newCascadedSource(pipelineConfigurationSource, CASCADED_FACTORS);

        ConfigurationManager manager = ConfigurationManagers.newManager("artemis", cascadedConfigurationSource);
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
