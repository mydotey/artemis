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
 * Abstract ServerDao with built-in factory method.
 * <p>
 * Use ServerDao.getInstance() to get the appropriate implementation:
 * - MySQL: MysqlServerDao (batch + ON DUPLICATE KEY UPDATE)
 * - SQLite/Other: GenericServerDao (check-exists-then-insert/update)
 * <p>
 * Database type is determined from configuration, no DB connection needed.
 */
public abstract class ServerDao {

    private static volatile ServerDao instance;

    /**
     * Get the appropriate ServerDao implementation based on configuration.
     * Lazy initialization on first call.
     */
    public static ServerDao getInstance() {
        if (instance == null) {
            synchronized (ServerDao.class) {
                if (instance == null) {
                    if (DataConfig.isMySQL()) {
                        instance = new MysqlServerDao();
                    } else {
                        instance = new GenericServerDao();
                    }
                }
            }
        }
        return instance;
    }

    

    private final String queryServerSql = "select distinct region_Id, server_id, operation from server where region_id=? and server_id=?";

    private final String queryServersSql = "select distinct region_Id, server_id, operation from server where region_id=?";

    private final String queryServersSql2 = "select distinct region_Id, server_id, operation from server";

    private final RowMapper<ServerModel> queryServersRowMapper = new RowMapper<ServerModel>() {
        @Override
        public ServerModel mapRow(final ResultSet rs, final int arg1) throws SQLException {
            final ServerModel instance = new ServerModel();
            instance.setRegionId(rs.getString(1));
            instance.setServerId(rs.getString(2));
            instance.setOperation(rs.getString(3));
            return instance;
        }
    };

    protected ServerDao() {

    }

    public List<ServerModel> queryServer(final String regionId, final String serverId) {
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(queryServerSql);
                ps.setString(1, regionId);
                ps.setString(2, serverId);
                return ps;
            }
        }, queryServersRowMapper);
    }

    public List<ServerModel> queryServers(final String regionId) {
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(queryServersSql);
                ps.setString(1, regionId);
                return ps;
            }
        }, queryServersRowMapper);
    }

    public List<ServerModel> queryServers() {
        return DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                return conn.prepareStatement(queryServersSql2);
            }
        }, queryServersRowMapper);
    }

    public List<ServerModel> query(final String condition, final String... args) {
        final String sql = "select id, region_id, server_id, operation, operator_id, token, create_time, datachange_lasttime from server";
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

        }, new RowMapper<ServerModel>() {
            @Override
            public ServerModel mapRow(final ResultSet rs, final int arg1) throws SQLException {
                final ServerModel instance = new ServerModel();
                instance.setId(rs.getLong(1));
                instance.setRegionId(rs.getString(2));
                instance.setServerId(rs.getString(3));
                instance.setOperation(rs.getString(4));
                instance.setOperatorId(rs.getString(5));
                instance.setToken(rs.getString(6));
                instance.setCreateTime(rs.getDate(7));
                instance.setUpdateTime(rs.getDate(8));
                return instance;
            }
        });
    }

    public void delete(final Long... ids) {
        DataConfig.jdbcTemplate().batchUpdate("delete from server where id = ?", new BatchPreparedStatementSetter() {
            @Override
            public int getBatchSize() {
                return ids.length;
            }

            @Override
            public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                ps.setLong(1, ids[i]);
            }
        });
    }

    public void delete(final ServerModel... servers) {
        if ((servers == null) || (servers.length == 0)) {
            return;
        }
        this.delete(Lists.newArrayList(servers));
    }

    public void delete(final List<ServerModel> serverList) {
        if (CollectionUtils.isEmpty(serverList)) {
            return;
        }
        DataConfig.jdbcTemplate().batchUpdate("delete from server where region_id=? and server_id=? and operation=?",
            new BatchPreparedStatementSetter() {
                @Override
                public int getBatchSize() {
                    return serverList.size();
                }

                @Override
                public void setValues(final PreparedStatement ps, final int index) throws SQLException {
                    final ServerModel server = serverList.get(index);
                    ps.setString(1, server.getRegionId());
                    ps.setString(2, server.getServerId());
                    ps.setString(3, server.getOperation());
                }
            });
    }

    public void destroyServers(final List<ServerKey> serverKeys) {
        if (CollectionUtils.isEmpty(serverKeys)) {
            return;
        }
        DataConfig.jdbcTemplate().batchUpdate("delete from server where region_id=? and server_id=?",
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

    public void insert(final ServerModel... servers) {
        if ((servers == null) || (servers.length == 0)) {
            return;
        }
        this.insert(Lists.newArrayList(servers));
    }

    public abstract void insert(List<ServerModel> serverList);

    // ========== MySQL Implementation ==========

    private static class MysqlServerDao extends ServerDao {
        @Override
        public void insert(final List<ServerModel> serverList) {
            if (CollectionUtils.isEmpty(serverList)) {
                return;
            }
            DataConfig.jdbcTemplate()
                .batchUpdate("insert into server (region_id, server_id, operation, operator_id, token) values (?,?,?,?,?)"
                    + " on duplicate key update operator_id=?, token=?", new BatchPreparedStatementSetter() {
                        @Override
                        public int getBatchSize() {
                            return serverList.size();
                        }

                        @Override
                        public void setValues(final PreparedStatement ps, final int index) throws SQLException {
                            final ServerModel server = serverList.get(index);
                            ps.setString(1, server.getRegionId());
                            ps.setString(2, server.getServerId());
                            ps.setString(3, server.getOperation());
                            ps.setString(4, server.getOperatorId());
                            ps.setString(5, server.getToken());

                            ps.setString(6, server.getOperatorId());
                            ps.setString(7, server.getToken());
                        }
                    });
        }
    }

    // ========== Generic Implementation (for SQLite and others) ==========

    private static class GenericServerDao extends ServerDao {
        @Override
        public void insert(final List<ServerModel> serverList) {
            if (CollectionUtils.isEmpty(serverList)) {
                return;
            }
            for (ServerModel server : serverList) {
                upsert(server);
            }
        }

        /**
         * Database-agnostic upsert: check if record exists, then insert or update
         */
        private void upsert(final ServerModel server) {
            // Check if record exists
            final String checkSql = "select id, operator_id, token from server where region_id=? and server_id=? and operation=?";
            List<Object[]> existing = DataConfig.jdbcTemplate().query(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                    PreparedStatement ps = conn.prepareStatement(checkSql);
                    ps.setString(1, server.getRegionId());
                    ps.setString(2, server.getServerId());
                    ps.setString(3, server.getOperation());
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
                final String insertSql = "insert into server (region_id, server_id, operation, operator_id, token) values (?,?,?,?,?)";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(insertSql);
                        ps.setString(1, server.getRegionId());
                        ps.setString(2, server.getServerId());
                        ps.setString(3, server.getOperation());
                        ps.setString(4, server.getOperatorId());
                        ps.setString(5, server.getToken());
                        return ps;
                    }
                });
            } else {
                // Update existing record
                final String updateSql = "update server set operator_id=?, token=? where id=?";
                DataConfig.jdbcTemplate().update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                        PreparedStatement ps = conn.prepareStatement(updateSql);
                        ps.setString(1, server.getOperatorId());
                        ps.setString(2, server.getToken());
                        ps.setLong(3, (Long) existing.get(0)[0]);
                        return ps;
                    }
                });
            }
        }
    }
}
