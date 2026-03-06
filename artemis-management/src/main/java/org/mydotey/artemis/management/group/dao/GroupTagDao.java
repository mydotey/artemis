package org.mydotey.artemis.management.group.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.artemis.management.dao.DataConfig;
import org.mydotey.artemis.management.group.model.GroupTagModel;
import org.mydotey.java.StringExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract GroupTagDao with built-in factory method.
 * <p>
 * Use GroupTagDao.getInstance() to get the appropriate implementation:
 * - MySQL: MysqlGroupTagDao (batch + ON DUPLICATE KEY UPDATE)
 * - SQLite/Other: GenericGroupTagDao (check-exists-then-insert/update)
 * <p>
 * Database type is determined from configuration, no DB connection needed.
 */
public abstract class GroupTagDao {

    /**
     * Holds the lazily-created singleton instance.
     */
    private static volatile GroupTagDao instance;

    /**
     * Get the appropriate GroupTagDao implementation based on configuration.
     * Lazy initialization on first call.
     */
    public static GroupTagDao getInstance() {
        if (instance == null) {
            synchronized (GroupTagDao.class) {
                if (instance == null) {
                    if (DataConfig.isMySQL()) {
                        instance = new MysqlGroupTagDao();
                    } else {
                        instance = new GenericGroupTagDao();
                    }
                }
            }
        }
        return instance;
    }

    

    protected GroupTagDao() {}

    // ========== Common Methods (all implementations share these) ==========

    public List<GroupTagModel> query() {
        return DataConfig.jdbcTemplate().query("select group_id, tag, value from service_group_tag",
            new RowMapper<GroupTagModel>() {
                @Override
                public GroupTagModel mapRow(ResultSet rs, int i) throws SQLException {
                    GroupTagModel tag = new GroupTagModel();
                    tag.setGroupId(rs.getLong(1));
                    tag.setTag(rs.getString(2));
                    tag.setValue(rs.getString(3));
                    return tag;
                }
            });
    }

    public void delete(GroupTagModel filter) {
        ValueCheckers.notNull(filter, "filter");
        final Map<String, String> conditions = Maps.newHashMap();
        conditions.put("group_id=?", filter.getGroupId() == null ? null : Long.toString(filter.getGroupId()));
        conditions.put("tag=?", filter.getTag());
        conditions.put("value=?", filter.getValue());
        Set<String> removed = Sets.newHashSet();
        for (String key : conditions.keySet()) {
            if (StringExtension.isBlank(conditions.get(key))) {
                removed.add(key);
            }
        }
        conditions.keySet().removeAll(removed);
        if (conditions.size() == 0) {
            throw new IllegalStateException("forbidden operation.");
        }
        String sql = "delete from service_group_tag where " + Joiner.on(" and ").join(conditions.keySet());
        DataConfig.jdbcTemplate().update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                List<String> args = Lists.newArrayList(conditions.values());
                for (int i = 0; i < args.size(); i++) {
                    ps.setString(i + 1, args.get(i));
                }
            }
        });
    }

    protected List<GroupTagModel> query(final String condition, final List<String> args) {
        final String sql = "SELECT id, group_id, tag, value, create_time, DataChange_LastTime from service_group_tag";
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

        }, new RowMapper<GroupTagModel>() {
            @Override
            public GroupTagModel mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                GroupTagModel groupTag = new GroupTagModel();
                groupTag.setId(rs.getLong(1));
                groupTag.setGroupId(rs.getLong(2));
                groupTag.setTag(rs.getString(3));
                groupTag.setValue(rs.getString(4));
                groupTag.setCreateTime(rs.getTimestamp(5));
                groupTag.setUpdateTime(rs.getTimestamp(6));
                return groupTag;
            }
        });
    }

    protected void checkInsertOrUpdateArgument(List<GroupTagModel> models) {
        ValueCheckers.notNullOrEmpty(models, "models");
        for (GroupTagModel model : models) {
            ValueCheckers.notNull(model, "groupTag");
            ValueCheckers.notNull(model.getGroupId(), "groupTag.groupId");
            ValueCheckers.notNullOrWhiteSpace(model.getTag(), "groupTag.tag");
            ValueCheckers.notNullOrWhiteSpace(model.getValue(), "groupTag.value");
        }
    }

    // ========== Abstract Methods (database-specific) ==========

    public abstract void insertOrUpdate(GroupTagModel... models);

    public abstract void insertOrUpdate(List<GroupTagModel> models);

    // ========== MySQL Implementation ==========

    private static class MysqlGroupTagDao extends GroupTagDao {
        @Override
        public void insertOrUpdate(GroupTagModel... models) {
            insertOrUpdate(Lists.newArrayList(models));
        }

        @Override
        public void insertOrUpdate(final List<GroupTagModel> models) {
            checkInsertOrUpdateArgument(models);
            DataConfig.jdbcTemplate().batchUpdate(
                "insert into service_group_tag (group_id, tag, value) values (?,?,?) on duplicate key update value = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        GroupTagModel model = models.get(i);
                        ps.setLong(1, model.getGroupId());
                        ps.setString(2, model.getTag());
                        ps.setString(3, model.getValue());
                        ps.setString(4, model.getValue());
                    }

                    @Override
                    public int getBatchSize() {
                        return models.size();
                    }
                });
        }
    }

    // ========== Generic Implementation (for SQLite and others) ==========

    private static class GenericGroupTagDao extends GroupTagDao {
        @Override
        public void insertOrUpdate(GroupTagModel... models) {
            insertOrUpdate(Lists.newArrayList(models));
        }

        @Override
        public void insertOrUpdate(final List<GroupTagModel> models) {
            checkInsertOrUpdateArgument(models);
            for (GroupTagModel model : models) {
                upsert(model);
            }
        }

        /**
         * Database-agnostic upsert: check if record exists, then insert or update
         */
        private void upsert(final GroupTagModel model) {
            // Check if record exists
            final String checkSql = "select id from service_group_tag where group_id=? and tag=?";
            List<Long> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setLong(1, model.getGroupId());
                    ps.setString(2, model.getTag());
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
                final String insertSql = "insert into service_group_tag (group_id, tag, value) values (?,?,?)";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setLong(1, model.getGroupId());
                        ps.setString(2, model.getTag());
                        ps.setString(3, model.getValue());
                        return ps;
                    }
                });
            } else {
                // Update existing record
                final String updateSql = "update service_group_tag set value=? where id=?";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(updateSql);
                        ps.setString(1, model.getValue());
                        ps.setLong(2, existing.get(0));
                        return ps;
                    }
                });
            }
        }
    }
}
