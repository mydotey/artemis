package org.mydotey.artemis.management.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mydotey.artemis.management.log.ServerOperationLog;
import org.mydotey.artemis.management.util.ServerLogModels;
import org.mydotey.artemis.test.ArtemisTest;
import com.google.common.collect.Lists;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ServerLogDaoTest extends ArtemisTest {
    ServerLogDao _serverLogDao = ServerLogDao.INSTANCE;

    @Test
    public void testInsert() {
        final ServerLogModel log1 = ServerLogModels.newServerLogModel();
        final ServerLogModel log2 = ServerLogModels.newServerLogModel();
        final List<ServerLogModel> logs = Lists.newArrayList(log1, log2);

        log1.setComplete(true);
        log2.setComplete(true);
        _serverLogDao.insert(log1, log2);
        for (final ServerLogModel log : logs) {
            final List<ServerOperationLog> logModels = query(log);
            Assert.assertEquals(1, logModels.size());
            ServerLogModels.assertServerLog(log, logModels.get(0));
        }

        log1.setComplete(false);
        log2.setComplete(false);
        _serverLogDao.insert(log1, log2);
        for (final ServerLogModel log : logs) {
            final List<ServerOperationLog> logModels = query(log);
            Assert.assertEquals(1, logModels.size());
            ServerLogModels.assertServerLog(log, logModels.get(0));
        }

    }

    private List<ServerOperationLog> query(final ServerLogModel serverLog) {
        return _serverLogDao.select(serverLog, serverLog.isComplete());
    }
}
