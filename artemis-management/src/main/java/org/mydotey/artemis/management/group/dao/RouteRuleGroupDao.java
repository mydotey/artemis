package org.mydotey.artemis.management.group.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.artemis.management.dao.DataConfig;
import org.mydotey.artemis.management.group.model.RouteRuleGroupModel;
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
 * Abstract RouteRuleGroupDao with built-in factory method.
 * <p>
 * Use RouteRuleGroupDao.getInstance() to get the appropriate implementation:
 * - MySQL: MysqlRouteRuleGroupDao (batch + ON DUPLICATE KEY UPDATE + INSERT IGNORE)
 * - SQLite/Other: GenericRouteRuleGroupDao (check-exists-then-insert/update)
 * <p>
 * Database type is determined from configuration, no DB connection needed.
 */
public abstract class RouteRuleGroupDao {

    /**
     * Holds the lazily-created singleton instance.
     */
    private static volatile RouteRuleGroupDao instance;

    /**
     * Get the appropriate RouteRuleGroupDao implementation based on configuration.
     * Lazy initialization on first call.
     */
    public static RouteRuleGroupDao getInstance() {
        if (instance == null) {
            synchronized (RouteRuleGroupDao.class) {
                if (instance == null) {
                    if (DataConfig.isMySQL()) {
                        instance = new MysqlRouteRuleGroupDao();
                    } else {
                        instance = new GenericRouteRuleGroupDao();
                    }
                }
            }
        }
        return instance;
    }

    

    protected RouteRuleGroupDao() {}

    // ========== Common Methods (all implementations share these) ==========

    public List<RouteRuleGroupModel> query() {
        final String sql = "SELECT id, route_rule_id, group_id, weight, unreleased_weight from service_route_rule_group";
        return DataConfig.jdbcTemplate().query(sql, new RowMapper<RouteRuleGroupModel>() {
            @Override
            public RouteRuleGroupModel mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                RouteRuleGroupModel routeRuleGroup = new RouteRuleGroupModel();
                routeRuleGroup.setId(rs.getLong(1));
                routeRuleGroup.setRouteRuleId(rs.getLong(2));
                routeRuleGroup.setGroupId(rs.getLong(3));
                if (rs.getString(4) != null) {
                    routeRuleGroup.setWeight(rs.getInt(4));
                }
                if (rs.getString(5) != null) {
                    routeRuleGroup.setUnreleasedWeight(rs.getInt(5));
                }
                return routeRuleGroup;
            }
        });
    }

    public RouteRuleGroupModel generateGroup(RouteRuleGroupModel routeRuleGroup) {
        RouteRuleGroupModel filter = new RouteRuleGroupModel();
        filter.setGroupId(routeRuleGroup.getGroupId());
        List<RouteRuleGroupModel> newRouteRuleGroups = select(filter);
        if (newRouteRuleGroups.size() == 0) {
            insert(routeRuleGroup);
            newRouteRuleGroups = select(filter);
        }
        return newRouteRuleGroups.get(0);
    }

    protected void delete(final Long... ids) {
        delete(Lists.newArrayList(ids));
    }

    protected void delete(final List<Long> ids) {
        ValueCheckers.notNullOrEmpty(ids, "ids");
        DataConfig.jdbcTemplate().batchUpdate("delete from service_route_rule_group where id = ?",
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

    protected void release(final List<RouteRuleGroupModel> models) {
        checkReleaseArgument(models);
        DataConfig.jdbcTemplate().batchUpdate(
            "update service_route_rule_group set weight = unreleased_weight where route_rule_id=? and group_id=?",
            new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    RouteRuleGroupModel model = models.get(i);
                    ps.setLong(1, model.getRouteRuleId());
                    ps.setLong(2, model.getGroupId());
                }

                @Override
                public int getBatchSize() {
                    return models.size();
                }
            });
    }

    public List<RouteRuleGroupModel> select(RouteRuleGroupModel filter) {
        ValueCheckers.notNull(filter, "filter");
        Map<String, Long> conditions = Maps.newHashMap();
        conditions.put("id=?", filter.getId());
        conditions.put("route_rule_id=?", filter.getRouteRuleId());
        conditions.put("group_id=?", filter.getGroupId());

        Set<String> removed = Sets.newHashSet();
        for (String key : conditions.keySet()) {
            if (conditions.get(key) == null) {
                removed.add(key);
            }
        }
        conditions.keySet().removeAll(removed);
        return query(Joiner.on(" and ").join(conditions.keySet()), Lists.newArrayList(conditions.values()));
    }

    public List<RouteRuleGroupModel> select(List<Long> groupIds) {
        if (CollectionExtension.isEmpty(groupIds)) {
            return Lists.newArrayList();
        }
        return query(String.format("id in (%s)", Joiner.on(',').join(groupIds)));
    }

    protected List<RouteRuleGroupModel> query(final String condition, final Long... args) {
        return query(condition, Lists.newArrayList(args));
    }

    protected List<RouteRuleGroupModel> query(final String condition, final List<Long> args) {
        final String sql = "SELECT id, route_rule_id, group_id, weight,unreleased_weight, create_time,DataChange_LastTime from service_route_rule_group";
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps;
                if (StringExtension.isBlank(condition)) {
                    ps = conn.prepareStatement(sql);
                } else {
                    ps = conn.prepareStatement(Joiner.on(" ").join(sql, "where", condition));
                    for (int i = 0; i < args.size(); i++) {
                        ps.setLong(i + 1, args.get(i));
                    }
                }
                return ps;
            }

        }, new RowMapper<RouteRuleGroupModel>() {
            @Override
            public RouteRuleGroupModel mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                RouteRuleGroupModel routeRuleGroup = new RouteRuleGroupModel();
                routeRuleGroup.setId(rs.getLong(1));
                routeRuleGroup.setRouteRuleId(rs.getLong(2));
                routeRuleGroup.setGroupId(rs.getLong(3));
                if (rs.getString(4) != null) {
                    routeRuleGroup.setWeight(rs.getInt(4));
                }
                if (rs.getString(5) != null) {
                    routeRuleGroup.setUnreleasedWeight(rs.getInt(5));
                }
                routeRuleGroup.setCreateTime(rs.getTimestamp(6));
                routeRuleGroup.setUpdateTime(rs.getTimestamp(7));
                return routeRuleGroup;
            }
        });
    }

    private static void checkInsertOrUpdateArgument(List<RouteRuleGroupModel> models) {
        ValueCheckers.notNullOrEmpty(models, "models");
        for (RouteRuleGroupModel model : models) {
            ValueCheckers.notNull(model, "routeRuleGroup");
            ValueCheckers.notNull(model.getRouteRuleId(), "routeRuleGroup.routeRuleId");
            ValueCheckers.notNull(model.getGroupId(), "routeRuleGroup.groupId");
        }
    }

    private static void checkReleaseArgument(List<RouteRuleGroupModel> models) {
        ValueCheckers.notNullOrEmpty(models, "models");
        for (RouteRuleGroupModel model : models) {
            ValueCheckers.notNull(model, "routeRuleGroup");
            ValueCheckers.notNull(model.getRouteRuleId(), "routeRuleGroup.routeRuleId");
            ValueCheckers.notNull(model.getGroupId(), "routeRuleGroup.groupId");
        }
    }

    // ========== Abstract Methods (database-specific) ==========

    protected abstract void insertOrUpdate(RouteRuleGroupModel... models);

    protected abstract void insertOrUpdate(List<RouteRuleGroupModel> models);

    protected abstract void insert(RouteRuleGroupModel... models);

    protected abstract void insert(List<RouteRuleGroupModel> models);

    protected abstract void publish(RouteRuleGroupModel... models);

    protected abstract void publish(List<RouteRuleGroupModel> models);

    // ========== MySQL Implementation ==========

    private static class MysqlRouteRuleGroupDao extends RouteRuleGroupDao {
        @Override
        protected void insertOrUpdate(RouteRuleGroupModel... models) {
            insertOrUpdate(Lists.newArrayList(models));
        }

        @Override
        protected void insertOrUpdate(final List<RouteRuleGroupModel> models) {
            checkInsertOrUpdateArgument(models);
            DataConfig.jdbcTemplate().batchUpdate(
                "insert into service_route_rule_group (route_rule_id, group_id, unreleased_weight) values (?,?,?)"
                    + " on duplicate key update unreleased_weight=?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        RouteRuleGroupModel model = models.get(i);
                        ps.setLong(1, model.getRouteRuleId());
                        ps.setLong(2, model.getGroupId());
                        Integer unreleasedWeight = model.getUnreleasedWeight();
                        if (unreleasedWeight == null) {
                            ps.setString(3, null);
                            ps.setString(4, null);
                        } else {
                            ps.setInt(3, unreleasedWeight);
                            ps.setInt(4, unreleasedWeight);
                        }
                    }

                    @Override
                    public int getBatchSize() {
                        return models.size();
                    }
                });
        }

        @Override
        protected void insert(RouteRuleGroupModel... models) {
            insert(Lists.newArrayList(models));
        }

        @Override
        protected void insert(final List<RouteRuleGroupModel> models) {
            checkInsertOrUpdateArgument(models);
            DataConfig.jdbcTemplate().batchUpdate(
                "insert ignore into service_route_rule_group (route_rule_id, group_id, unreleased_weight) values (?,?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        RouteRuleGroupModel model = models.get(i);
                        ps.setLong(1, model.getRouteRuleId());
                        ps.setLong(2, model.getGroupId());
                        Integer unreleasedWeight = model.getUnreleasedWeight();
                        if (unreleasedWeight == null) {
                            ps.setString(3, null);
                        } else {
                            ps.setInt(3, unreleasedWeight);
                        }
                    }

                    @Override
                    public int getBatchSize() {
                        return models.size();
                    }
                });
        }

        @Override
        protected void publish(RouteRuleGroupModel... models) {
            publish(Lists.newArrayList(models));
        }

        @Override
        protected void publish(final List<RouteRuleGroupModel> models) {
            checkInsertOrUpdateArgument(models);
            DataConfig.jdbcTemplate()
                .batchUpdate("insert into service_route_rule_group (route_rule_id, group_id, weight) values (?,?,?)"
                    + " on duplicate key update weight=?, unreleased_weight=NULL", new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            RouteRuleGroupModel model = models.get(i);
                            ps.setLong(1, model.getRouteRuleId());
                            ps.setLong(2, model.getGroupId());
                            Integer weight = model.getWeight();
                            if (weight == null) {
                                ps.setString(3, null);
                                ps.setString(4, null);
                            } else {
                                ps.setInt(3, weight);
                                ps.setInt(4, weight);
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

    private static class GenericRouteRuleGroupDao extends RouteRuleGroupDao {
        @Override
        protected void insertOrUpdate(RouteRuleGroupModel... models) {
            insertOrUpdate(Lists.newArrayList(models));
        }

        @Override
        protected void insertOrUpdate(final List<RouteRuleGroupModel> models) {
            checkInsertOrUpdateArgument(models);
            for (RouteRuleGroupModel model : models) {
                upsert(model, "unreleased_weight");
            }
        }

        @Override
        protected void insert(RouteRuleGroupModel... models) {
            insert(Lists.newArrayList(models));
        }

        @Override
        protected void insert(final List<RouteRuleGroupModel> models) {
            checkInsertOrUpdateArgument(models);
            for (RouteRuleGroupModel model : models) {
                insertIfNotExists(model);
            }
        }

        @Override
        protected void publish(RouteRuleGroupModel... models) {
            publish(Lists.newArrayList(models));
        }

        @Override
        protected void publish(final List<RouteRuleGroupModel> models) {
            checkInsertOrUpdateArgument(models);
            for (RouteRuleGroupModel model : models) {
                publishOne(model);
            }
        }

        /**
         * Publish a single route rule group: set weight and clear unreleased_weight
         */
        private void publishOne(final RouteRuleGroupModel model) {
            // Check if record exists
            final String checkSql = "select id from service_route_rule_group where route_rule_id=? and group_id=?";
            List<Long> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setLong(1, model.getRouteRuleId());
                    ps.setLong(2, model.getGroupId());
                    return ps;
                }
            }, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong(1);
                }
            });

            if (existing.isEmpty()) {
                // Insert new record with weight only (unreleased_weight defaults to NULL)
                final String insertSql = "insert into service_route_rule_group (route_rule_id, group_id, weight) values (?,?,?)";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setLong(1, model.getRouteRuleId());
                        ps.setLong(2, model.getGroupId());
                        Integer weight = model.getWeight();
                        if (weight == null) {
                            ps.setString(3, null);
                        } else {
                            ps.setInt(3, weight);
                        }
                        return ps;
                    }
                });
            } else {
                // Update existing record: set weight and clear unreleased_weight
                final String updateSql = "update service_route_rule_group set weight=?, unreleased_weight=NULL where id=?";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(updateSql);
                        Integer weight = model.getWeight();
                        if (weight == null) {
                            ps.setString(1, null);
                        } else {
                            ps.setInt(1, weight);
                        }
                        ps.setLong(2, existing.get(0));
                        return ps;
                    }
                });
            }
        }

        /**
         * Database-agnostic upsert: check if record exists, then insert or update
         */
        private void upsert(final RouteRuleGroupModel model, final String updateField) {
            // Check if record exists
            final String checkSql = "select id from service_route_rule_group where route_rule_id=? and group_id=?";
            List<Long> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setLong(1, model.getRouteRuleId());
                    ps.setLong(2, model.getGroupId());
                    return ps;
                }
            }, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong(1);
                }
            });

            if (existing.isEmpty()) {
                // Insert new record - only insert the field being updated (to match MySQL behavior)
                final String insertSql;
                if ("unreleased_weight".equals(updateField)) {
                    insertSql = "insert into service_route_rule_group (route_rule_id, group_id, unreleased_weight) values (?,?,?)";
                } else {
                    insertSql = "insert into service_route_rule_group (route_rule_id, group_id, weight) values (?,?,?)";
                }
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setLong(1, model.getRouteRuleId());
                        ps.setLong(2, model.getGroupId());
                        Integer value;
                        if ("unreleased_weight".equals(updateField)) {
                            value = model.getUnreleasedWeight();
                        } else {
                            value = model.getWeight();
                        }
                        if (value == null) {
                            ps.setString(3, null);
                        } else {
                            ps.setInt(3, value);
                        }
                        return ps;
                    }
                });
            } else {
                // Update existing record - clear the other field when updating one
                final String updateSql;
                if ("unreleased_weight".equals(updateField)) {
                    updateSql = "update service_route_rule_group set unreleased_weight=?, weight=NULL where id=?";
                } else {
                    updateSql = "update service_route_rule_group set weight=? where id=?";
                }
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(updateSql);
                        Integer value;
                        if ("weight".equals(updateField)) {
                            value = model.getWeight();
                        } else {
                            value = model.getUnreleasedWeight();
                        }
                        if (value == null) {
                            ps.setString(1, null);
                        } else {
                            ps.setInt(1, value);
                        }
                        ps.setLong(2, existing.get(0));
                        return ps;
                    }
                });
            }
        }

        /**
         * Database-agnostic insert: only insert if record doesn't exist
         */
        private void insertIfNotExists(final RouteRuleGroupModel model) {
            // Check if record exists
            final String checkSql = "select id from service_route_rule_group where route_rule_id=? and group_id=?";
            List<Long> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setLong(1, model.getRouteRuleId());
                    ps.setLong(2, model.getGroupId());
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
                final String insertSql = "insert into service_route_rule_group (route_rule_id, group_id, unreleased_weight) values (?,?,?)";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setLong(1, model.getRouteRuleId());
                        ps.setLong(2, model.getGroupId());
                        Integer unreleasedWeight = model.getUnreleasedWeight();
                        if (unreleasedWeight == null) {
                            ps.setString(3, null);
                        } else {
                            ps.setInt(3, unreleasedWeight);
                        }
                        return ps;
                    }
                });
            }
        }
    }
}
