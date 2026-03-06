package org.mydotey.artemis.test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Test database initializer - creates tables before tests run.
 */
public class TestDatabaseInitializer {

    private static boolean initialized = false;

    public static synchronized void initDatabase() throws Exception {
        if (initialized) {
            return;
        }

        // Get JdbcTemplate from DataConfig
        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        // Execute schema.sql
        executeSchemaSql(jdbcTemplate);

        initialized = true;
        System.out.println("[TestDatabaseInitializer] Database schema initialized successfully");
    }

    private static JdbcTemplate getJdbcTemplate() throws Exception {
        // Force DataConfig initialization
        Class<?> dataConfigClass = Class.forName("org.mydotey.artemis.management.dao.DataConfig");
        java.lang.reflect.Method initMethod = dataConfigClass.getMethod("init");
        initMethod.invoke(null);

        java.lang.reflect.Method jdbcTemplateMethod = dataConfigClass.getMethod("jdbcTemplate");
        return (JdbcTemplate) jdbcTemplateMethod.invoke(null);
    }

    private static void executeSchemaSql(JdbcTemplate jdbcTemplate) throws Exception {
        // Read and execute schema.sql
        java.io.File schemaFile = ResourceUtils.getFile("classpath:schema.sql");

        try (BufferedReader reader = new BufferedReader(new FileReader(schemaFile))) {
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sql.append(line).append("\n");

                // Execute each statement
                if (line.endsWith(";")) {
                    String statement = sql.toString();
                    try {
                        jdbcTemplate.execute(statement);
                    } catch (Exception e) {
                        // Ignore errors for IF NOT EXISTS statements
                        if (!statement.contains("IF NOT EXISTS")) {
                            System.err.println("[TestDatabaseInitializer] Error executing SQL: " + statement);
                            throw e;
                        }
                    }
                    sql = new StringBuilder();
                }
            }
        }
    }
}
