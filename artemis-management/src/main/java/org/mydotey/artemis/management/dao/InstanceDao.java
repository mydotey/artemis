package org.mydotey.artemis.management.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mydotey.artemis.ServerKey;
import org.mydotey.java.StringExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Abstract InstanceDao with built-in factory method.
 * <p>
 * Use InstanceDao.getInstance() to get the appropriate implementation:
 * - MySQL: MysqlInstanceDao (batch + ON DUPLICATE KEY UPDATE)
 * - SQLite/Other: GenericInstanceDao (check-exists-then-insert/update)
 * <p>
 * Database type is determined from configuration, no DB connection needed.
 */
public abstract class InstanceDao {

    private static volatile InstanceDao instance;

    /**
     * Get the appropriate InstanceDao implementation based on configuration.
     * Lazy initialization on first call.
     */
    public static InstanceDao getInstance() {
        if (instance == null) {
            synchronized (InstanceDao.class) {
                if (instance == null) {
                    if (DataConfig.isMySQL()) {
                        instance = new MysqlInstanceDao();
                    } else {
                        instance = new GenericInstanceDao();
                    }
                }
            }
        }
        return instance;
    }

    

    private final String queryInstanceSql = "select distinct region_id, service_id, instance_id, operation from instance where region_id=? and service_id=? and instance_id=?";

    private final String queryInstancesSql = "select distinct region_id, service_id, instance_id, operation from instance where region_id=?";

    private final String queryInstancesSql2 = "select distinct region_id, service_id, instance_id, operation from instance";

    private final RowMapper<InstanceModel> queryInstancesRowMapper = new RowMapper<InstanceModel>() {
        @Override
        public InstanceModel mapRow(final ResultSet rs, final int arg1) throws SQLException {
            final InstanceModel instance = new InstanceModel();
            instance.setRegionId(rs.getString(1));
            instance.setServiceId(rs.getString(2));
            instance.setInstanceId(rs.getString(3));
            instance.setOperation(rs.getString(4));
            return instance;
        }
    };

    protected InstanceDao() {

    }

    public List<InstanceModel> queryInstance(final String regionId, final String serviceId, final String instanceId) {
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(queryInstanceSql);
                ps.setString(1, regionId);
                ps.setString(2, serviceId);
                ps.setString(3, instanceId);
                return ps;
            }
        }, queryInstancesRowMapper);
    }

    public List<InstanceModel> queryInstances(final String regionId) {
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(queryInstancesSql);
                ps.setString(1, regionId);
                return ps;
            }
        }, queryInstancesRowMapper);
    }

    public List<InstanceModel> queryInstances() {
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                return conn.prepareStatement(queryInstancesSql2);
            }
        }, queryInstancesRowMapper);
    }

    public List<InstanceModel> queryInstances(final String regionId, final List<String> serviceIds) {
        if (CollectionUtils.isEmpty(serviceIds)) {
            return Lists.newArrayList();
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < serviceIds.size(); i++) {
            builder.append("?,");
        }
        builder.deleteCharAt(builder.length() - 1);
        final String sql = queryInstancesSql + " and service_id in (" + builder + ")";
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, regionId);
                for (int i = 0, len = serviceIds.size(); i < len; i++) {
                    ps.setString(i + 2, serviceIds.get(i));
                }
                return ps;
            }

        }, queryInstancesRowMapper);
    }

    public List<InstanceModel> query(final String condition, final String... args) {
        final String sql = "select id, region_id, service_id, instance_id, operation, operator_id, token, create_time, datachange_lasttime from instance";
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps;
                if (StringExtension.isBlank(condition)) {
                    ps = conn.prepareStatement(sql);
                } else {
                    ps = conn.prepareStatement(Joiner.on(" ").join(sql, "where", condition));
                    for (int i = 0; i < args.length; i++) {
                        ps.setString(i + 1, args[i]);
                    }
                }
                return ps;
            }

        }, new RowMapper<InstanceModel>() {
            @Override
            public InstanceModel mapRow(final ResultSet rs, final int arg1) throws SQLException {
                final InstanceModel instance = new InstanceModel();
                instance.setId(rs.getLong(1));
                instance.setRegionId(rs.getString(2));
                instance.setServiceId(rs.getString(3));
                instance.setInstanceId(rs.getString(4));
                instance.setOperation(rs.getString(5));
                instance.setOperatorId(rs.getString(6));
                instance.setToken(rs.getString(7));
                instance.setCreateTime(rs.getDate(8));
                instance.setUpdateTime(rs.getDate(9));
                return instance;
            }
        });
    }

    public void delete(final Long... ids) {
        if ((ids == null) || (ids.length == 0)) {
            return;
        }
        DataConfig.jdbcTemplate().batchUpdate("delete from instance where id = ?", new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return ids.length;
            }

            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                final Long id = ids[i];
                if (id == null) {
                    ps.setLong(1, 0L);
                } else {
                    ps.setLong(1, id);
                }
            }
        });
    }

    public void delete(final InstanceModel... instances) {
        if ((instances == null) || (instances.length == 0)) {
            return;
        }
        this.delete(Lists.newArrayList(instances));
    }

    public void delete(final List<InstanceModel> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return;
        }
        DataConfig.jdbcTemplate().batchUpdate(
            "delete from instance where region_id=? and service_id=? and instance_id=? and operation=?",
            new BatchPreparedStatementSetter() {
                @Override
                public int getBatchSize() {
                    return instances.size();
                }

                @Override
                public void setValues(final PreparedStatement ps, final int index) throws SQLException {
                    final InstanceModel instance = instances.get(index);
                    ps.setString(1, instance.getRegionId());
                    ps.setString(2, instance.getServiceId());
                    ps.setString(3, instance.getInstanceId());
                    ps.setString(4, instance.getOperation());
                }
            });
    }

    public void destroyServers(final List<ServerKey> serverKeys) {
        if (CollectionUtils.isEmpty(serverKeys)) {
            return;
        }
        DataConfig.jdbcTemplate().batchUpdate("delete from instance where region_id=? and instance_id=?",
            new BatchPreparedStatementSetter() {
                @Override
                public int getBatchSize() {
                    return serverKeys.size();
                }

                @Override
                public void setValues(final PreparedStatement ps, final int index) throws SQLException {
                    ServerKey serverKey = serverKeys.get(index);
                    ps.setString(1, serverKey.getRegionId());
                    ps.setString(2, serverKey.getServerId());
                }
            });
    }

    public void insert(final InstanceModel... instances) {
        if ((instances == null) || (instances.length == 0)) {
            return;
        }
        this.insert(Lists.newArrayList(instances));
    }

    public abstract void insert(List<InstanceModel> instances);

    // ========== MySQL Implementation ==========

    private static class MysqlInstanceDao extends InstanceDao {
        @Override
        public void insert(final List<InstanceModel> instances) {
            if (CollectionUtils.isEmpty(instances)) {
                return;
            }
            DataConfig.jdbcTemplate().batchUpdate(
                "insert into instance (region_id, service_id, instance_id, operation, operator_id, token) values (?,?,?,?,?,?)"
                    + " on duplicate key update operator_id=?, token=?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public int getBatchSize() {
                        return instances.size();
                    }

                    @Override
                    public void setValues(final PreparedStatement ps, final int index) throws SQLException {
                        final InstanceModel instance = instances.get(index);
                        ps.setString(1, instance.getRegionId());
                        ps.setString(2, instance.getServiceId());
                        ps.setString(3, instance.getInstanceId());
                        ps.setString(4, instance.getOperation());
                        ps.setString(5, instance.getOperatorId());
                        ps.setString(6, instance.getToken());
                        ps.setString(7, instance.getOperatorId());
                        ps.setString(8, instance.getToken());
                    }
                });
        }
    }

    // ========== Generic Implementation (for SQLite and others) ==========

    private static class GenericInstanceDao extends InstanceDao {
        @Override
        public void insert(final List<InstanceModel> instances) {
            if (CollectionUtils.isEmpty(instances)) {
                return;
            }
            for (InstanceModel instance : instances) {
                upsert(instance);
            }
        }

        /**
         * Database-agnostic upsert: check if record exists, then insert or update
         */
        private void upsert(final InstanceModel instance) {
            // Check if record exists
            final String checkSql = "select id, operator_id, token from instance where region_id=? and service_id=? and instance_id=? and operation=?";
            List<Object[]> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setString(1, instance.getRegionId());
                    ps.setString(2, instance.getServiceId());
                    ps.setString(3, instance.getInstanceId());
                    ps.setString(4, instance.getOperation());
                    return ps;
                }
            }, new RowMapper<Object[]>() {
                @Override
                public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new Object[]{rs.getLong(1), rs.getString(2), rs.getString(3)};
                }
            });

            if (existing.isEmpty()) {
                // Insert new record
                final String insertSql = "insert into instance (region_id, service_id, instance_id, operation, operator_id, token) values (?,?,?,?,?,?)";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setString(1, instance.getRegionId());
                        ps.setString(2, instance.getServiceId());
                        ps.setString(3, instance.getInstanceId());
                        ps.setString(4, instance.getOperation());
                        ps.setString(5, instance.getOperatorId());
                        ps.setString(6, instance.getToken());
                        return ps;
                    }
                });
            } else {
                // Update existing record
                final String updateSql = "update instance set operator_id=?, token=? where id=?";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(updateSql);
                        ps.setString(1, instance.getOperatorId());
                        ps.setString(2, instance.getToken());
                        ps.setLong(3, (Long) existing.get(0)[0]);
                        return ps;
                    }
                });
            }
        }
    }
}
