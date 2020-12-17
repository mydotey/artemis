package org.mydotey.artemis.management.util;

import org.mydotey.artemis.cluster.ClusterManager;
import org.mydotey.artemis.management.ManagementInitializer;
import org.mydotey.artemis.management.dao.DataConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * Created by fang_j on 10/07/2016.
 */
@Configuration
public class DataConfig4SpringInjection {
    static {
        try {
            ManagementInitializer.INSTANCE.init();
            ClusterManager.INSTANCE.init();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Bean
    public static JdbcTemplate jdbcTemplate() {
        return DataConfig.jdbcTemplate();
    }

    @Bean
    public static DataSourceTransactionManager dataSourceTransactionManager() {
        return DataConfig.dataSourceTransactionManager();
    }
}
