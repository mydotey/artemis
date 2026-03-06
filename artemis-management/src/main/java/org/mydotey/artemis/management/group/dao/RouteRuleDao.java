package org.mydotey.artemis.management.group.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.artemis.management.dao.DataConfig;
import org.mydotey.artemis.management.group.model.RouteRuleModel;
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
 * Abstract RouteRuleDao with built-in factory method.
 * <p>
 * Use RouteRuleDao.getInstance() to get the appropriate implementation:
 * - MySQL: MysqlRouteRuleDao (batch + ON DUPLICATE KEY UPDATE)
 * - SQLite/Other: GenericRouteRuleDao (check-exists-then-insert/update)
 * <p>
 * Database type is determined from configuration, no DB connection needed.
 */
public abstract class RouteRuleDao {

    /**
     * Holds the lazily-created singleton instance.
     */
    private static volatile RouteRuleDao instance;

    /**
     * Get the appropriate RouteRuleDao implementation based on configuration.
     * Lazy initialization on first call.
     */
    public static RouteRuleDao getInstance() {
        if (instance == null) {
            synchronized (RouteRuleDao.class) {
                if (instance == null) {
                    if (DataConfig.isMySQL()) {
                        instance = new MysqlRouteRuleDao();
                    } else {
                        instance = new GenericRouteRuleDao();
                    }
                }
            }
        }
        return instance;
    }

    

    protected RouteRuleDao() {}

    // ========== Common Methods (all implementations share these) ==========

    public List<RouteRuleModel> query() {
        final String sql = "SELECT id, service_id, name, description, status, strategy from service_route_rule where deleted = false";
        return DataConfig.jdbcTemplate().query(sql, new RowMapper<RouteRuleModel>() {
            @Override
            public RouteRuleModel mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                RouteRuleModel routeRule = new RouteRuleModel();
                routeRule.setId(rs.getLong(1));
                routeRule.setServiceId(rs.getString(2));
                routeRule.setName(rs.getString(3));
                routeRule.setDescription(rs.getString(4));
                routeRule.setStatus(rs.getString(5));
                routeRule.setStrategy(rs.getString(6));
                return routeRule;
            }
        });
    }

    protected RouteRuleModel generateRouteRule(RouteRuleModel routeRule) {
        insert(routeRule);
        RouteRuleModel filter = new RouteRuleModel();
        filter.setServiceId(routeRule.getServiceId());
        filter.setName(routeRule.getName());
        List<RouteRuleModel> newRouteRules = select(filter);
        if (newRouteRules.size() == 0) {
            insert(routeRule);
            newRouteRules = select(filter);
        }
        return newRouteRules.get(0);
    }

    protected void delete(final Long... ids) {
        delete(Lists.newArrayList(ids));
    }

    protected void delete(final List<Long> ids) {
        ValueCheckers.notNullOrEmpty(ids, "ids");
        DataConfig.jdbcTemplate().batchUpdate("update service_route_rule set deleted = true where id = ?",
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

    protected void insertOrUpdate(RouteRuleModel... models) {
        insertOrUpdate(Lists.newArrayList(models));
    }

    protected abstract void insertOrUpdate(List<RouteRuleModel> models);

    protected void insert(RouteRuleModel... models) {
        insert(Lists.newArrayList(models));
    }

    protected abstract void insert(List<RouteRuleModel> models);

    public List<RouteRuleModel> select(RouteRuleModel filter) {
        ValueCheckers.notNull(filter, "filter");
        Map<String, String> conditions = Maps.newHashMap();
        conditions.put("id=?", filter.getId() == null ? null : Long.toString(filter.getId()));
        conditions.put("service_id=?", filter.getServiceId());
        conditions.put("name=?", filter.getName());
        conditions.put("status=?", filter.getStatus());

        Set<String> removed = Sets.newHashSet();
        for (String key : conditions.keySet()) {
            if (StringExtension.isBlank(conditions.get(key))) {
                removed.add(key);
            }
        }
        conditions.keySet().removeAll(removed);
        return query(Joiner.on(" and ").join(conditions.keySet()), Lists.newArrayList(conditions.values()));
    }

    public List<RouteRuleModel> select(List<Long> groupIds) {
        if (CollectionExtension.isEmpty(groupIds)) {
            return Lists.newArrayList();
        }
        return query(String.format("id in (%s)", Joiner.on(',').join(groupIds)));
    }

    protected List<RouteRuleModel> query(final String condition, final String... args) {
        return query(condition, Lists.newArrayList(args));
    }

    protected List<RouteRuleModel> query(final String condition, final List<String> args) {
        final String sql = "SELECT id, service_id, name, description,status, strategy, create_time,DataChange_LastTime from service_route_rule where deleted = false";
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps;
                if (StringExtension.isBlank(condition)) {
                    ps = conn.prepareStatement(sql);
                } else {
                    ps = conn.prepareStatement(Joiner.on(" ").join(sql, "and", condition));
                    for (int i = 0; i < args.size(); i++) {
                        ps.setString(i + 1, args.get(i));
                    }
                }
                return ps;
            }

        }, new RowMapper<RouteRuleModel>() {
            @Override
            public RouteRuleModel mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                RouteRuleModel routeRule = new RouteRuleModel();
                routeRule.setId(rs.getLong(1));
                routeRule.setServiceId(rs.getString(2));
                routeRule.setName(rs.getString(3));
                routeRule.setDescription(rs.getString(4));
                routeRule.setStatus(rs.getString(5));
                routeRule.setStrategy(rs.getString(6));
                routeRule.setCreateTime(rs.getTimestamp(7));
                routeRule.setUpdateTime(rs.getTimestamp(8));
                return routeRule;
            }
        });
    }

    private static void checkInsertOrUpdateArgument(List<RouteRuleModel> models) {
        ValueCheckers.notNullOrEmpty(models, "models");
        for (RouteRuleModel model : models) {
            checkInsertOrUpdateArgument(model);
        }
    }

    private static void checkInsertOrUpdateArgument(RouteRuleModel model) {
        ValueCheckers.notNull(model, "routeRule");
        ValueCheckers.notNullOrWhiteSpace(model.getServiceId(), "routeRule.serviceId");
        ValueCheckers.notNullOrWhiteSpace(model.getName(), "routeRule.name");
        ValueCheckers.notNullOrWhiteSpace(model.getStatus(), "routeRule.status");
        ValueCheckers.notNullOrWhiteSpace(model.getStrategy(), "routeRule.strategy");
    }

    // ========== MySQL Implementation ==========

    private static class MysqlRouteRuleDao extends RouteRuleDao {
        @Override
        protected void insertOrUpdate(final List<RouteRuleModel> models) {
            checkInsertOrUpdateArgument(models);
            DataConfig.jdbcTemplate().batchUpdate(
                "insert into service_route_rule (service_id, name, description, status, strategy) values (?,?,?,?,?)"
                    + " on duplicate key update description=?, status=?, strategy=?, deleted=false",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        RouteRuleModel model = models.get(i);
                        Object[] params = new Object[] { model.getServiceId(), model.getName(), model.getDescription(),
                            model.getStatus(), model.getStrategy(),
                            model.getDescription(), model.getStatus(), model.getStrategy() };

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

        @Override
        protected void insert(final List<RouteRuleModel> models) {
            checkInsertOrUpdateArgument(models);
            DataConfig.jdbcTemplate().batchUpdate(
                "insert into service_route_rule (service_id, name, description, status, strategy) values (?,?,?,?,?)"
                    + " on duplicate key update deleted=false",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        RouteRuleModel model = models.get(i);
                        ps.setString(1, model.getServiceId());
                        ps.setString(2, model.getName());
                        ps.setString(3, model.getDescription());
                        ps.setString(4, model.getStatus());
                        ps.setString(5, model.getStrategy());
                    }

                    @Override
                    public int getBatchSize() {
                        return models.size();
                    }
                });
        }
    }

    // ========== Generic Implementation (for SQLite and others) ==========

    private static class GenericRouteRuleDao extends RouteRuleDao {
        @Override
        protected void insertOrUpdate(final List<RouteRuleModel> models) {
            checkInsertOrUpdateArgument(models);
            for (RouteRuleModel model : models) {
                upsert(model);
            }
        }

        @Override
        protected void insert(final List<RouteRuleModel> models) {
            checkInsertOrUpdateArgument(models);
            for (RouteRuleModel model : models) {
                insertIfNotExists(model);
            }
        }

        /**
         * Database-agnostic upsert: check if record exists, then insert or update
         */
        private void upsert(final RouteRuleModel model) {
            // Check if record exists
            final String checkSql = "select id, description, status, strategy from service_route_rule where service_id=? and name=?";
            List<Object[]> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setString(1, model.getServiceId());
                    ps.setString(2, model.getName());
                    return ps;
                }
            }, new RowMapper<Object[]>() {
                @Override
                public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new Object[]{rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4)};
                }
            });

            if (existing.isEmpty()) {
                // Insert new record
                final String insertSql = "insert into service_route_rule (service_id, name, description, status, strategy) values (?,?,?,?,?)";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setString(1, model.getServiceId());
                        ps.setString(2, model.getName());
                        ps.setString(3, model.getDescription());
                        ps.setString(4, model.getStatus());
                        ps.setString(5, model.getStrategy());
                        return ps;
                    }
                });
            } else {
                // Update existing record
                final String updateSql = "update service_route_rule set description=?, status=?, strategy=?, deleted=false where id=?";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(updateSql);
                        ps.setString(1, model.getDescription());
                        ps.setString(2, model.getStatus());
                        ps.setString(3, model.getStrategy());
                        ps.setLong(4, (Long) existing.get(0)[0]);
                        return ps;
                    }
                });
            }
        }

        /**
         * Database-agnostic insert: only insert if record doesn't exist
         */
        private void insertIfNotExists(final RouteRuleModel model) {
            // Check if record exists
            final String checkSql = "select id from service_route_rule where service_id=? and name=?";
            List<Long> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setString(1, model.getServiceId());
                    ps.setString(2, model.getName());
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
                final String insertSql = "insert into service_route_rule (service_id, name, description, status, strategy) values (?,?,?,?,?)";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setString(1, model.getServiceId());
                        ps.setString(2, model.getName());
                        ps.setString(3, model.getDescription());
                        ps.setString(4, model.getStatus());
                        ps.setString(5, model.getStrategy());
                        return ps;
                    }
                });
            }
        }
    }
}
