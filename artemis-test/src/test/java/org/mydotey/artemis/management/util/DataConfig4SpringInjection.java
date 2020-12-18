package org.mydotey.artemis.management.util;

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

    @Bean
    public static JdbcTemplate jdbcTemplate() {
        return DataConfig.jdbcTemplate();
    }

    @Bean
    public static DataSourceTransactionManager dataSourceTransactionManager() {
        return DataConfig.dataSourceTransactionManager();
    }
}
