package org.mydotey.artemis.management.dao;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * Created by fang_j on 10/07/2016.
 * Modified to support SQLite and environment-based configuration.
 *
 * Default database is MySQL. SQLite can be configured via:
 * 1. Environment variable ARTEMIS_DB_CONFIG pointing to a SQLite properties file
 * 2. Environment variables ARTEMIS_DB_URL and ARTEMIS_DB_DRIVER
 * 3. System properties artemis.db.url and artemis.db.driver
 */
public class DataConfig {

    private static final String DB_CONFIG_FILE = "data-source.properties";
    private static final String DB_CONFIG_FILE_ENV = "ARTEMIS_DB_CONFIG";
    private static final String DB_URL_ENV = "ARTEMIS_DB_URL";
    private static final String DB_DRIVER_ENV = "ARTEMIS_DB_DRIVER";

    private static final String DEFAULT_MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    private static DataSource _dataSource;
    private static DataSourceTransactionManager _dataSourceTransactionManager;
    private static JdbcTemplate _jdbcTemplate;

    private static AtomicBoolean _inited = new AtomicBoolean();
    private static volatile Boolean _isMySQL;

    public static void init() throws Exception {
        if (!_inited.compareAndSet(false, true))
            return;

        initDataSource();
        // Ensure _isMySQL is initialized
        isMySQL();

        _dataSourceTransactionManager = new DataSourceTransactionManager(_dataSource);
        _jdbcTemplate = new JdbcTemplate(_dataSource);
    }

    /**
     * Check if we're using MySQL, based on configuration.
     * Can be called before init(), will detect from environment.
     */
    public static boolean isMySQL() {
        System.out.println("[DataConfig] isMySQL() called, returning: " + (_isMySQL == null ? "null (will detect)" : _isMySQL));
        if (_isMySQL == null) {
            synchronized (DataConfig.class) {
                if (_isMySQL == null) {
                    _isMySQL = detectIsMySQL();
                }
            }
        }
        return _isMySQL;
    }

    /**
     * Detect database type from configuration.
     * Checks system properties, environment variables, or config files.
     */
    private static boolean detectIsMySQL() {
        // 1. Check system properties
        String driver = System.getProperty("artemis.db.driver");
        if (driver != null) {
            return isMySQLDriver(driver);
        }

        String url = System.getProperty("artemis.db.url");
        if (url != null) {
            return isMySQLUrl(url);
        }

        // 2. Check environment variables
        driver = System.getenv("ARTEMIS_DB_DRIVER");
        if (driver != null) {
            return isMySQLDriver(driver);
        }

        url = System.getenv("ARTEMIS_DB_URL");
        if (url != null) {
            return isMySQLUrl(url);
        }

        // 3. Check if ARTEMIS_DB_CONFIG is pointing to sqlite
        String configFile = System.getenv("ARTEMIS_DB_CONFIG");
        if (configFile != null && configFile.contains("sqlite")) {
            return false;
        }

        // 4. Default to MySQL for backward compatibility
        return true;
    }

    private static boolean isMySQLDriver(String driver) {
        String lower = driver.toLowerCase();
        return lower.contains("mysql") && !lower.contains("sqlite");
    }

    private static boolean isMySQLUrl(String url) {
        String lower = url.toLowerCase();
        return lower.contains("jdbc:mysql") && !lower.contains("sqlite");
    }

    private static void initDataSource() throws Exception {
        Properties prop = new Properties();
        String configFile = DB_CONFIG_FILE;
        boolean useDefaultMySQL = true;

        // 1. 检查环境变量指定的配置文件
        String envConfigFile = System.getenv(DB_CONFIG_FILE_ENV);
        if (envConfigFile != null && !envConfigFile.isEmpty()) {
            configFile = envConfigFile;
            useDefaultMySQL = false;
            System.out.println("[DataConfig] Using database config from environment: " + configFile);
        }

        // 2. 检查环境变量是否指定了数据库URL（用于SQLite或其他数据库）
        String envDbUrl = System.getenv(DB_URL_ENV);
        if (envDbUrl != null && !envDbUrl.isEmpty()) {
            useDefaultMySQL = false;
        }

        // 3. 检查系统属性是否指定了数据库URL
        String sysDbUrl = System.getProperty("artemis.db.url");
        if (sysDbUrl != null && !sysDbUrl.isEmpty()) {
            useDefaultMySQL = false;
        }

        // 4. 尝试加载配置文件
        boolean configLoaded = false;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile)) {
            if (is != null) {
                prop.load(is);
                configLoaded = true;
                System.out.println("[DataConfig] Loaded database config from: " + configFile);
            }
        }

        // 5. 如果没有加载到配置，且没有指定其他数据库，使用默认MySQL配置
        if (!configLoaded && useDefaultMySQL) {
            System.out.println("[DataConfig] No config file found and no database URL specified. Using default MySQL configuration.");
            System.out.println("[DataConfig] To use SQLite, set one of:");
            System.out.println("  - Environment variable ARTEMIS_DB_CONFIG=data-source-sqlite.properties");
            System.out.println("  - Environment variable ARTEMIS_DB_URL=jdbc:sqlite:./artemis.db");
            System.out.println("  - System property -Dartemis.db.url=jdbc:sqlite:./artemis.db");

            // 使用默认MySQL配置，但URL需要用户配置
            prop.setProperty("driverClassName", DEFAULT_MYSQL_DRIVER);
            prop.setProperty("url", "jdbc:mysql://localhost:3306/artemis");
            prop.setProperty("username", "root");
            prop.setProperty("password", "");
            prop.setProperty("initialSize", "5");
            prop.setProperty("maxActive", "20");
            prop.setProperty("maxIdle", "10");
            prop.setProperty("minIdle", "2");
        }

        // 6. 允许通过环境变量覆盖特定配置
        if (envDbUrl != null && !envDbUrl.isEmpty()) {
            prop.setProperty("url", envDbUrl);
            System.out.println("[DataConfig] Database URL from environment (" + DB_URL_ENV + "): " + envDbUrl);
        }

        String dbDriver = System.getenv(DB_DRIVER_ENV);
        if (dbDriver != null && !dbDriver.isEmpty()) {
            prop.setProperty("driverClassName", dbDriver);
            System.out.println("[DataConfig] Database driver from environment (" + DB_DRIVER_ENV + "): " + dbDriver);
        }

        // 7. 允许通过系统属性覆盖配置
        if (sysDbUrl != null && !sysDbUrl.isEmpty()) {
            prop.setProperty("url", sysDbUrl);
            System.out.println("[DataConfig] Database URL from system property (artemis.db.url): " + sysDbUrl);
        }

        String sysDbDriver = System.getProperty("artemis.db.driver");
        if (sysDbDriver != null && !sysDbDriver.isEmpty()) {
            prop.setProperty("driverClassName", sysDbDriver);
            System.out.println("[DataConfig] Database driver from system property (artemis.db.driver): " + sysDbDriver);
        }

        // 8. 根据最终配置设置数据库类型
        String finalDriver = prop.getProperty("driverClassName");
        String finalUrl = prop.getProperty("url");
        if (finalDriver != null) {
            _isMySQL = isMySQLDriver(finalDriver);
        } else if (finalUrl != null) {
            _isMySQL = isMySQLUrl(finalUrl);
        } else {
            _isMySQL = true; // 默认 MySQL
        }

        // 9. 创建数据源
        _dataSource = BasicDataSourceFactory.createDataSource(prop);

        System.out.println("[DataConfig] DataSource initialized successfully");
        System.out.println("  Driver: " + finalDriver);
        System.out.println("  URL: " + maskPassword(finalUrl));
        System.out.println("  Database Type: " + (_isMySQL ? "MySQL" : "Generic/SQLite"));
    }

    /**
     * Masks password in URL for logging
     */
    private static String maskPassword(String url) {
        if (url == null) return null;
        // Simple masking for URLs containing passwords
        return url.replaceAll("(password|pwd)=([^&]*)", "$1=***");
    }

    public static JdbcTemplate jdbcTemplate() {
        return _jdbcTemplate;
    }

    public static DataSourceTransactionManager dataSourceTransactionManager() {
        return _dataSourceTransactionManager;
    }

    public DataSource dataSource() {
        return _dataSource;
    }
}
