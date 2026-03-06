package org.mydotey.artemis.management.zone.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.artemis.management.dao.DataConfig;
import org.mydotey.artemis.management.zone.model.ZoneOperationModel;
import org.mydotey.java.StringExtension;
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
 * Abstract ZoneOperationDao with built-in factory method.
 * <p>
 * Use ZoneOperationDao.getInstance() to get the appropriate implementation:
 * - MySQL: MysqlZoneOperationDao (batch + INSERT IGNORE)
 * - SQLite/Other: GenericZoneOperationDao (check-exists-then-insert)
 * <p>
 * Database type is determined from configuration, no DB connection needed.
 */
public abstract class ZoneOperationDao {

    /**
     * Holds the lazily-created singleton instance.
     */
    private static volatile ZoneOperationDao instance;

    /**
     * Get the appropriate ZoneOperationDao implementation based on configuration.
     * Lazy initialization on first call.
     */
    public static ZoneOperationDao getInstance() {
        if (instance == null) {
            synchronized (ZoneOperationDao.class) {
                if (instance == null) {
                    if (DataConfig.isMySQL()) {
                        instance = new MysqlZoneOperationDao();
                    } else {
                        instance = new GenericZoneOperationDao();
                    }
                }
            }
        }
        return instance;
    }

    

    protected ZoneOperationDao() {}

    // ========== Common Methods (all implementations share these) ==========

    public List<ZoneOperationModel> query() {
        return DataConfig.jdbcTemplate().query("select service_id, region_id, zone_id, operation from service_zone",
            new RowMapper<ZoneOperationModel>() {
                @Override
                public ZoneOperationModel mapRow(ResultSet rs, int i) throws SQLException {
                    ZoneOperationModel zoneOperation = new ZoneOperationModel();
                    zoneOperation.setServiceId(rs.getString(1));
                    zoneOperation.setRegionId(rs.getString(2));
                    zoneOperation.setZoneId(rs.getString(3));
                    zoneOperation.setOperation(rs.getString(4));
                    return zoneOperation;
                }
            });
    }

    public void delete(final ZoneOperationModel... zoneOperations) {
        delete(Lists.newArrayList(zoneOperations));
    }

    public void delete(final List<ZoneOperationModel> zoneOperations) {
        ValueCheckers.notNullOrEmpty(zoneOperations, "group operations");
        DataConfig.jdbcTemplate().batchUpdate(
            "delete from service_zone where service_id =? and region_id=? and zone_id=? and operation = ?",
            new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ZoneOperationModel zoneOperation = zoneOperations.get(i);
                    ps.setString(1, zoneOperation.getServiceId());
                    ps.setString(2, zoneOperation.getRegionId());
                    ps.setString(3, zoneOperation.getZoneId());
                    ps.setString(4, zoneOperation.getOperation());
                }

                @Override
                public int getBatchSize() {
                    return zoneOperations.size();
                }
            });
    }

    public void insertOrUpdate(ZoneOperationModel... operationModels) {
        insertOrUpdate(Lists.newArrayList(operationModels));
    }

    public abstract void insertOrUpdate(List<ZoneOperationModel> models);

    public List<ZoneOperationModel> select(ZoneOperationModel filter) {
        ValueCheckers.notNull(filter, "filter");
        Map<String, String> conditions = Maps.newHashMap();
        conditions.put("id=?", filter.getId() == null ? null : Long.toString(filter.getId()));
        conditions.put("service_id=?", filter.getServiceId());
        conditions.put("region_id=?", filter.getRegionId());
        conditions.put("zone_id=?", filter.getZoneId());
        conditions.put("operation=?", filter.getOperation());

        Set<String> removed = Sets.newHashSet();
        for (String key : conditions.keySet()) {
            if (StringExtension.isBlank(conditions.get(key))) {
                removed.add(key);
            }
        }
        conditions.keySet().removeAll(removed);
        return query(Joiner.on(" and ").join(conditions.keySet()), Lists.newArrayList(conditions.values()));
    }

    protected List<ZoneOperationModel> query(final String condition, final String... args) {
        return query(condition, Lists.newArrayList(args));
    }

    protected List<ZoneOperationModel> query(final String condition, final List<String> args) {
        final String sql = "SELECT id, service_id, region_id, zone_Id,operation,create_time,DataChange_LastTime from service_zone";
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

        }, new RowMapper<ZoneOperationModel>() {
            @Override
            public ZoneOperationModel mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                ZoneOperationModel zoneOperation = new ZoneOperationModel();
                zoneOperation.setId(rs.getLong(1));
                zoneOperation.setServiceId(rs.getString(2));
                zoneOperation.setRegionId(rs.getString(3));
                zoneOperation.setZoneId(rs.getString(4));
                zoneOperation.setOperation(rs.getString(5));
                zoneOperation.setCreateTime(rs.getTimestamp(6));
                zoneOperation.setUpdateTime(rs.getTimestamp(7));
                return zoneOperation;
            }
        });
    }

    private static void checkInsertOrUpdateArgument(List<ZoneOperationModel> models) {
        ValueCheckers.notNullOrEmpty(models, "models");
        for (ZoneOperationModel model : models) {
            ValueCheckers.notNull(model, "zoneOperation");
            ValueCheckers.notNullOrWhiteSpace(model.getServiceId(), "zoneOperation.serviceId");
            ValueCheckers.notNullOrWhiteSpace(model.getRegionId(), "zoneOperation.regionId");
            ValueCheckers.notNullOrWhiteSpace(model.getZoneId(), "zoneOperation.zoneId");
            ValueCheckers.notNullOrWhiteSpace(model.getOperation(), "zoneOperation.operation");
        }
    }

    // ========== MySQL Implementation ==========

    private static class MysqlZoneOperationDao extends ZoneOperationDao {
        @Override
        public void insertOrUpdate(final List<ZoneOperationModel> models) {
            checkInsertOrUpdateArgument(models);
            DataConfig.jdbcTemplate().batchUpdate(
                "insert ignore into service_zone (service_id, region_id, zone_id, operation) values (?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ZoneOperationModel zoneOperation = models.get(i);
                        ps.setString(1, zoneOperation.getServiceId());
                        ps.setString(2, zoneOperation.getRegionId());
                        ps.setString(3, zoneOperation.getZoneId());
                        ps.setString(4, zoneOperation.getOperation());
                    }

                    @Override
                    public int getBatchSize() {
                        return models.size();
                    }
                });
        }
    }

    // ========== Generic Implementation (for SQLite and others) ==========

    private static class GenericZoneOperationDao extends ZoneOperationDao {
        @Override
        public void insertOrUpdate(final List<ZoneOperationModel> models) {
            checkInsertOrUpdateArgument(models);
            for (ZoneOperationModel model : models) {
                insertIfNotExists(model);
            }
        }

        /**
         * Database-agnostic insert: only insert if record doesn't exist
         */
        private void insertIfNotExists(final ZoneOperationModel model) {
            // Check if record exists
            final String checkSql = "select id from service_zone where service_id=? and region_id=? and zone_id=? and operation=?";
            List<Long> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setString(1, model.getServiceId());
                    ps.setString(2, model.getRegionId());
                    ps.setString(3, model.getZoneId());
                    ps.setString(4, model.getOperation());
                    return ps;
                }
            }, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong(1);
                }
            });

            if (existing.isEmpty()) {
                // Insert new record only if not exists
                final String insertSql = "insert into service_zone (service_id, region_id, zone_id, operation) values (?,?,?,?)";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setString(1, model.getServiceId());
                        ps.setString(2, model.getRegionId());
                        ps.setString(3, model.getZoneId());
                        ps.setString(4, model.getOperation());
                        return ps;
                    }
                });
            }
        }
    }
}
