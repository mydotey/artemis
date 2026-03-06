package org.mydotey.artemis.management.group.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.artemis.management.dao.DataConfig;
import org.mydotey.artemis.management.group.model.GroupOperationModel;
import org.mydotey.java.StringExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import java.sql.*;
import java.util.List;

/**
 * Abstract GroupOperationDao with built-in factory method.
 * <p>
 * Use GroupOperationDao.getInstance() to get the appropriate implementation:
 * - MySQL: MysqlGroupOperationDao (batch + INSERT IGNORE)
 * - SQLite/Other: GenericGroupOperationDao (check-exists-then-insert)
 * <p>
 * Database type is determined from configuration, no DB connection needed.
 */
public abstract class GroupOperationDao {

    /**
     * Holds the lazily-created singleton instance.
     */
    private static volatile GroupOperationDao instance;

    /**
     * Get the appropriate GroupOperationDao implementation based on configuration.
     * Lazy initialization on first call.
     */
    public static GroupOperationDao getInstance() {
        if (instance == null) {
            synchronized (GroupOperationDao.class) {
                if (instance == null) {
                    if (DataConfig.isMySQL()) {
                        instance = new MysqlGroupOperationDao();
                    } else {
                        instance = new GenericGroupOperationDao();
                    }
                }
            }
        }
        return instance;
    }

    

    protected GroupOperationDao() {}

    // ========== Common Methods (all implementations share these) ==========

    public List<GroupOperationModel> query() {
        return DataConfig.jdbcTemplate().query("select group_id, operation from service_group_operation",
            new RowMapper<GroupOperationModel>() {
                @Override
                public GroupOperationModel mapRow(ResultSet rs, int i) throws SQLException {
                    GroupOperationModel groupOperation = new GroupOperationModel();
                    groupOperation.setGroupId(rs.getLong(1));
                    groupOperation.setOperation(rs.getString(2));
                    return groupOperation;
                }
            });
    }

    protected void delete(GroupOperationModel groupOperation) {
        delete(Lists.newArrayList(groupOperation));
    }

    protected void delete(final List<GroupOperationModel> groupOperations) {
        ValueCheckers.notNullOrEmpty(groupOperations, "group operations");
        DataConfig.jdbcTemplate().batchUpdate("delete from service_group_operation where group_id =? and operation = ?",
            new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    GroupOperationModel groupOperation = groupOperations.get(i);
                    ps.setLong(1, groupOperation.getGroupId());
                    ps.setString(2, groupOperation.getOperation());
                }

                @Override
                public int getBatchSize() {
                    return groupOperations.size();
                }
            });
    }

    protected List<GroupOperationModel> query(final String condition, final List<String> args) {
        final String sql = "SELECT id, group_id,operation,create_time,DataChange_LastTime from service_group_operation";
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

        }, new RowMapper<GroupOperationModel>() {
            @Override
            public GroupOperationModel mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                GroupOperationModel groupOperation = new GroupOperationModel();
                groupOperation.setId(rs.getLong(1));
                groupOperation.setGroupId(rs.getLong(2));
                groupOperation.setOperation(rs.getString(3));
                groupOperation.setCreateTime(rs.getTimestamp(4));
                groupOperation.setUpdateTime(rs.getTimestamp(5));
                return groupOperation;
            }
        });
    }

    protected void checkInsertOrUpdateArgument(List<GroupOperationModel> models) {
        ValueCheckers.notNullOrEmpty(models, "models");
        for (GroupOperationModel model : models) {
            ValueCheckers.notNull(model, "groupOperation");
            ValueCheckers.notNull(model.getGroupId(), "groupOperation.groupId");
            ValueCheckers.notNullOrWhiteSpace(model.getOperation(), "groupOperation.operation");
        }
    }

    // ========== Abstract Methods (database-specific) ==========

    protected abstract void insertOrUpdate(GroupOperationModel... operationModels);

    protected abstract void insertOrUpdate(List<GroupOperationModel> models);

    // ========== MySQL Implementation ==========

    private static class MysqlGroupOperationDao extends GroupOperationDao {
        @Override
        protected void insertOrUpdate(GroupOperationModel... operationModels) {
            insertOrUpdate(Lists.newArrayList(operationModels));
        }

        @Override
        protected void insertOrUpdate(final List<GroupOperationModel> models) {
            checkInsertOrUpdateArgument(models);
            DataConfig.jdbcTemplate().batchUpdate(
                "insert ignore into service_group_operation (group_id, operation) values (?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        GroupOperationModel groupOperation = models.get(i);
                        ps.setLong(1, groupOperation.getGroupId());
                        ps.setString(2, groupOperation.getOperation());
                    }

                    @Override
                    public int getBatchSize() {
                        return models.size();
                    }
                });
        }
    }

    // ========== Generic Implementation (for SQLite and others) ==========

    private static class GenericGroupOperationDao extends GroupOperationDao {
        @Override
        protected void insertOrUpdate(GroupOperationModel... operationModels) {
            insertOrUpdate(Lists.newArrayList(operationModels));
        }

        @Override
        protected void insertOrUpdate(final List<GroupOperationModel> models) {
            checkInsertOrUpdateArgument(models);
            for (GroupOperationModel model : models) {
                insertIfNotExists(model);
            }
        }

        /**
         * Database-agnostic insert: only insert if record doesn't exist
         */
        private void insertIfNotExists(final GroupOperationModel model) {
            // Check if record exists
            final String checkSql = "select id from service_group_operation where group_id=? and operation=?";
            List<Long> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setLong(1, model.getGroupId());
                    ps.setString(2, model.getOperation());
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
                final String insertSql = "insert into service_group_operation (group_id, operation) values (?,?)";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setLong(1, model.getGroupId());
                        ps.setString(2, model.getOperation());
                        return ps;
                    }
                });
            }
        }
    }
}
