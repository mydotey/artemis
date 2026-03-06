package org.mydotey.artemis.management.group.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.artemis.management.dao.DataConfig;
import org.mydotey.artemis.management.group.model.ServiceInstanceModel;
import org.mydotey.java.StringExtension;
import org.mydotey.java.collection.CollectionExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract ServiceInstanceDao with built-in factory method.
 * <p>
 * Use ServiceInstanceDao.getInstance() to get the appropriate implementation:
 * - MySQL: MysqlServiceInstanceDao (batch + ON DUPLICATE KEY UPDATE)
 * - SQLite/Other: GenericServiceInstanceDao (check-exists-then-insert/update)
 * <p>
 * Database type is determined from configuration, no DB connection needed.
 */
public abstract class ServiceInstanceDao {

    /**
     * Holds the lazily-created singleton instance.
     */
    private static volatile ServiceInstanceDao instance;

    /**
     * Get the appropriate ServiceInstanceDao implementation based on configuration.
     * Lazy initialization on first call.
     */
    public static ServiceInstanceDao getInstance() {
        if (instance == null) {
            synchronized (ServiceInstanceDao.class) {
                if (instance == null) {
                    if (DataConfig.isMySQL()) {
                        instance = new MysqlServiceInstanceDao();
                    } else {
                        instance = new GenericServiceInstanceDao();
                    }
                }
            }
        }
        return instance;
    }

    

    protected ServiceInstanceDao() {}

    // ========== Common Methods (all implementations share these) ==========

    public List<ServiceInstanceModel> query() {
        final String sql = "SELECT service_id, instance_id, ip, machine_name, metadata, port, protocol, region_id, zone_id, healthy_check_url, url, group_id from service_instance";
        return DataConfig.jdbcTemplate().query(sql, new RowMapper<ServiceInstanceModel>() {
            @Override
            public ServiceInstanceModel mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                ServiceInstanceModel serviceInstance = new ServiceInstanceModel();
                serviceInstance.setServiceId(rs.getString(1));
                serviceInstance.setInstanceId(rs.getString(2));
                serviceInstance.setIp(rs.getString(3));
                serviceInstance.setMachineName(rs.getString(4));
                serviceInstance.setMetadata(rs.getString(5));
                serviceInstance.setPort(rs.getInt(6));
                serviceInstance.setProtocol(rs.getString(7));
                serviceInstance.setRegionId(rs.getString(8));
                serviceInstance.setZoneId(rs.getString(9));
                serviceInstance.setHealthCheckUrl(rs.getString(10));
                serviceInstance.setUrl(rs.getString(11));
                serviceInstance.setGroupId(rs.getString(12));
                return serviceInstance;
            }
        });
    }

    protected void delete(final Long... ids) {
        delete(Lists.newArrayList(ids));
    }

    protected void delete(final List<Long> ids) {
        ValueCheckers.notNullOrEmpty(ids, "ids");
        DataConfig.jdbcTemplate().batchUpdate("delete from service_instance where id = ?",
            new BatchPreparedStatementSetter() {
                @Override
                public int getBatchSize() {
                    return ids.size();
                }

                @Override
                public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                    final Long id = ids.get(i);
                    if (id == null) {
                        ps.setLong(1, 0L);
                    } else {
                        ps.setLong(1, id);
                    }
                }
            });
    }

    protected void deleteByFilters(final List<ServiceInstanceModel> filters) {
        checkInsertOrUpdateArgument(filters);
        DataConfig.jdbcTemplate().batchUpdate("delete from service_instance where service_id = ? and instance_id=?",
            new BatchPreparedStatementSetter() {
                @Override
                public int getBatchSize() {
                    return filters.size();
                }

                @Override
                public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                    final ServiceInstanceModel filter = filters.get(i);
                    ps.setString(1, filter.getServiceId());
                    ps.setString(2, filter.getInstanceId());
                }
            });
    }

    public List<ServiceInstanceModel> select(ServiceInstanceModel filter) {
        ValueCheckers.notNull(filter, "filter");
        Map<String, String> conditions = Maps.newHashMap();
        conditions.put("service_id=?", filter.getServiceId());
        conditions.put("instance_id=?", filter.getInstanceId());

        Set<String> removed = Sets.newHashSet();
        for (String key : conditions.keySet()) {
            if (StringExtension.isBlank(conditions.get(key))) {
                removed.add(key);
            }
        }
        conditions.keySet().removeAll(removed);
        return query(Joiner.on(" and ").join(conditions.keySet()), Lists.newArrayList(conditions.values()));
    }

    public List<ServiceInstanceModel> select(List<Long> groupIds) {
        if (CollectionExtension.isEmpty(groupIds)) {
            return Lists.newArrayList();
        }
        return query(String.format("id in (%s)", Joiner.on(',').join(groupIds)));
    }

    protected List<ServiceInstanceModel> query(final String condition, final String... args) {
        return query(condition, Lists.newArrayList(args));
    }

    protected List<ServiceInstanceModel> query(final String condition, final List<String> args) {
        final String sql = "SELECT id, service_id, instance_id, ip, machine_name, metadata, port, protocol, region_id, zone_id, healthy_check_url,url, description, group_id, create_time,DataChange_LastTime from service_instance";
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps;
                if (StringExtension.isBlank(condition)) {
                    ps = conn.prepareStatement(sql);
                } else {
                    ps = conn.prepareStatement(Joiner.on(" ").join(sql, "where", condition));
                    for (int i = 0; i < args.size(); i++) {
                        ps.setString(i + 1, args.get(i));
                    }
                }
                return ps;
            }

        }, new RowMapper<ServiceInstanceModel>() {
            @Override
            public ServiceInstanceModel mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                ServiceInstanceModel serviceInstance = new ServiceInstanceModel();
                serviceInstance.setId(rs.getLong(1));
                serviceInstance.setServiceId(rs.getString(2));
                serviceInstance.setInstanceId(rs.getString(3));
                serviceInstance.setIp(rs.getString(4));
                serviceInstance.setMachineName(rs.getString(5));
                serviceInstance.setMetadata(rs.getString(6));
                serviceInstance.setPort(rs.getInt(7));
                serviceInstance.setProtocol(rs.getString(8));
                serviceInstance.setRegionId(rs.getString(9));
                serviceInstance.setZoneId(rs.getString(10));
                serviceInstance.setHealthCheckUrl(rs.getString(11));
                serviceInstance.setUrl(rs.getString(12));
                serviceInstance.setDescription(rs.getString(13));
                serviceInstance.setGroupId(rs.getString(14));
                serviceInstance.setCreateTime(rs.getTimestamp(15));
                serviceInstance.setUpdateTime(rs.getTimestamp(16));
                return serviceInstance;
            }
        });
    }

    protected void checkInsertOrUpdateArgument(List<ServiceInstanceModel> models) {
        ValueCheckers.notNullOrEmpty(models, "models");
        for (ServiceInstanceModel model : models) {
            ValueCheckers.notNull(model, "serviceInstance");
            ValueCheckers.notNull(model.getServiceId(), "serviceInstance.serviceId");
            ValueCheckers.notNullOrWhiteSpace(model.getInstanceId(), "serviceInstance.instanceId");
            ValueCheckers.notNullOrWhiteSpace(model.getUrl(), "serviceInstance.url");
            ValueCheckers.notNullOrWhiteSpace(model.getIp(), "serviceInstance.ip");
            ValueCheckers.notNullOrWhiteSpace(model.getMachineName(), "serviceInstance.machineName");
            ValueCheckers.notNullOrWhiteSpace(model.getMetadata(), "serviceInstance.metadata");
            ValueCheckers.notNullOrWhiteSpace(model.getProtocol(), "serviceInstance.protocol");
            ValueCheckers.notNullOrWhiteSpace(model.getRegionId(), "serviceInstance.regionId");
            ValueCheckers.notNullOrWhiteSpace(model.getZoneId(), "serviceInstance.zoneId");
            ValueCheckers.notNullOrWhiteSpace(model.getHealthCheckUrl(), "serviceInstance.healthCheckUrl");
            ValueCheckers.notNullOrWhiteSpace(model.getGroupId(), "serviceInstance.groupId");
        }
    }

    // ========== Abstract Methods (database-specific) ==========

    protected abstract void insertOrUpdate(ServiceInstanceModel... groupInstances);

    protected abstract void insertOrUpdate(List<ServiceInstanceModel> models);

    // ========== MySQL Implementation ==========

    private static class MysqlServiceInstanceDao extends ServiceInstanceDao {
        @Override
        protected void insertOrUpdate(ServiceInstanceModel... groupInstances) {
            insertOrUpdate(Lists.newArrayList(groupInstances));
        }

        @Override
        protected void insertOrUpdate(final List<ServiceInstanceModel> models) {
            checkInsertOrUpdateArgument(models);
            DataConfig.jdbcTemplate().batchUpdate(
                "insert into service_instance (service_id, instance_id, ip, machine_name, metadata, port, protocol, region_id, zone_id, healthy_check_url, url, description, group_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?) on duplicate key "
                    +
                    "update  ip=?, machine_name=?, metadata=?, port=?, protocol=?, region_id=?, zone_id=?, healthy_check_url=?, url=?, description=?, group_id=?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ServiceInstanceModel model = models.get(i);
                        Object[] params = new Object[] { model.getServiceId(), model.getInstanceId(),
                            model.getIp(), model.getMachineName(), model.getMetadata(), model.getPort(),
                            model.getProtocol(), model.getRegionId(), model.getZoneId(), model.getHealthCheckUrl(),
                            model.getUrl(), model.getDescription(), model.getGroupId(),
                            model.getIp(), model.getMachineName(), model.getMetadata(), model.getPort(),
                            model.getProtocol(), model.getRegionId(), model.getZoneId(), model.getHealthCheckUrl(),
                            model.getUrl(), model.getDescription(), model.getGroupId() };
                        for (int index = 0; index < params.length; index++) {
                            ps.setObject(index + 1, params[index]);
                        }
                    }

                    @Override
                    public int getBatchSize() {
                        return models.size();
                    }
                });
        }
    }

    // ========== Generic Implementation (for SQLite and others) ==========

    private static class GenericServiceInstanceDao extends ServiceInstanceDao {
        @Override
        protected void insertOrUpdate(ServiceInstanceModel... groupInstances) {
            insertOrUpdate(Lists.newArrayList(groupInstances));
        }

        @Override
        protected void insertOrUpdate(final List<ServiceInstanceModel> models) {
            checkInsertOrUpdateArgument(models);
            for (ServiceInstanceModel model : models) {
                upsert(model);
            }
        }

        /**
         * Database-agnostic upsert: check if record exists, then insert or update
         */
        private void upsert(final ServiceInstanceModel model) {
            // Check if record exists
            final String checkSql = "select id from service_instance where service_id=? and instance_id=?";
            List<Long> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setString(1, model.getServiceId());
                    ps.setString(2, model.getInstanceId());
                    return ps;
                }
            }, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong(1);
                }
            });

            if (existing.isEmpty()) {
                // Insert new record
                final String insertSql = "insert into service_instance (service_id, instance_id, ip, machine_name, metadata, port, protocol, region_id, zone_id, healthy_check_url, url, description, group_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setString(1, model.getServiceId());
                        ps.setString(2, model.getInstanceId());
                        ps.setString(3, model.getIp());
                        ps.setString(4, model.getMachineName());
                        ps.setString(5, model.getMetadata());
                        ps.setInt(6, model.getPort());
                        ps.setString(7, model.getProtocol());
                        ps.setString(8, model.getRegionId());
                        ps.setString(9, model.getZoneId());
                        ps.setString(10, model.getHealthCheckUrl());
                        ps.setString(11, model.getUrl());
                        ps.setString(12, model.getDescription());
                        ps.setString(13, model.getGroupId());
                        return ps;
                    }
                });
            } else {
                // Update existing record
                final String updateSql = "update service_instance set ip=?, machine_name=?, metadata=?, port=?, protocol=?, region_id=?, zone_id=?, healthy_check_url=?, url=?, description=?, group_id=? where id=?";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(updateSql);
                        ps.setString(1, model.getIp());
                        ps.setString(2, model.getMachineName());
                        ps.setString(3, model.getMetadata());
                        ps.setInt(4, model.getPort());
                        ps.setString(5, model.getProtocol());
                        ps.setString(6, model.getRegionId());
                        ps.setString(7, model.getZoneId());
                        ps.setString(8, model.getHealthCheckUrl());
                        ps.setString(9, model.getUrl());
                        ps.setString(10, model.getDescription());
                        ps.setString(11, model.getGroupId());
                        ps.setLong(12, existing.get(0));
                        return ps;
                    }
                });
            }
        }
    }
}
