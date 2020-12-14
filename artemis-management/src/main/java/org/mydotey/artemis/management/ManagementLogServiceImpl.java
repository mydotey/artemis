package org.mydotey.artemis.management;

import org.mydotey.artemis.ErrorCodes;
import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.management.dao.InstanceLogDao;
import org.mydotey.artemis.management.dao.InstanceLogModel;
import org.mydotey.artemis.management.dao.ServerLogDao;
import org.mydotey.artemis.management.dao.ServerLogModel;
import org.mydotey.artemis.management.group.dao.*;
import org.mydotey.artemis.management.group.model.*;
import org.mydotey.artemis.management.log.*;
import org.mydotey.artemis.management.zone.dao.ZoneOperationLogDao;
import org.mydotey.artemis.management.zone.model.ZoneOperationLogModel;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.ResponseStatusUtil;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ManagementLogServiceImpl implements ManagementLogService {
    private static ManagementLogServiceImpl _instance;

    public static ManagementLogServiceImpl getInstance() {
        if (_instance == null) {
            synchronized (ManagementLogServiceImpl.class) {
                if (_instance == null)
                    _instance = new ManagementLogServiceImpl();
            }
        }

        return _instance;
    }

    private final InstanceLogDao instanceLogDao = InstanceLogDao.INSTANCE;
    private final ServerLogDao serverLogDao = ServerLogDao.INSTANCE;
    private final GroupOperationLogDao groupOperationLogDao = GroupOperationLogDao.INSTANCE;
    private final GroupLogDao groupLogDao = GroupLogDao.INSTANCE;
    private final RouteRuleLogDao routeRuleLogDao = RouteRuleLogDao.INSTANCE;
    private final RouteRuleGroupLogDao routeRuleGroupLogDao = RouteRuleGroupLogDao.INSTANCE;
    private final GroupInstanceLogDao groupInstanceLogDao = GroupInstanceLogDao.INSTANCE;
    private final ZoneOperationLogDao zoneOperationLogDao = ZoneOperationLogDao.INSTANCE;
    private final ServiceInstanceLogDao serviceInstanceLogDao = ServiceInstanceLogDao.INSTANCE;

    private ManagementLogServiceImpl() {

    }

    @Override
    public GetInstanceOperationLogsResponse getInstanceOperationLogs(final GetInstanceOperationLogsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management-log.instance-operation-logs",
            () -> {
                GetInstanceOperationLogsResponse response = new GetInstanceOperationLogsResponse();
                if (!check(request, response)) {
                    return response;
                }
                try {
                    response.setLogs(instanceLogDao.select(
                        new InstanceLogModel(request.getRegionId(), request.getServiceId(), request.getInstanceId(),
                            request.getOperation(), request.getOperatorId()),
                        request.getComplete()));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Exception e) {
                    response.setResponseStatus(
                        ResponseStatusUtil.newFailStatus(e.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public GetServerOperationLogsResponse getServerOperationLogs(final GetServerOperationLogsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management-log.server-operation-logs",
            () -> {
                GetServerOperationLogsResponse response = new GetServerOperationLogsResponse();
                if (!check(request, response)) {
                    return response;
                }
                try {
                    response.setLogs(serverLogDao.select(
                        new ServerLogModel(request.getRegionId(), request.getServerId(), request.getOperation(),
                            request.getOperatorId()),
                        request.getComplete()));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Exception e) {
                    response.setResponseStatus(
                        ResponseStatusUtil.newFailStatus(e.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public GetGroupOperationLogsResponse getGroupOperationLogs(final GetGroupOperationLogsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management-log.group-operation-logs",
            () -> {
                GetGroupOperationLogsResponse response = new GetGroupOperationLogsResponse();
                if (!check(request, response)) {
                    return response;
                }
                try {
                    response.setLogs(groupOperationLogDao
                        .select(new GroupOperationLogModel(request.getGroupId(), request.getOperation(),
                            request.getOperatorId()), request.getComplete()));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Exception e) {
                    response.setResponseStatus(
                        ResponseStatusUtil.newFailStatus(e.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public GetGroupLogsResponse getGroupLogs(final GetGroupLogsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management-log.group-logs",
            () -> {
                GetGroupLogsResponse response = new GetGroupLogsResponse();
                if (!check(request, response)) {
                    return response;
                }
                try {
                    response.setLogs(groupLogDao.select(new GroupLogModel(request.getServiceId(),
                        request.getRegionId(), request.getZoneId(), request.getName(),
                        request.getAppId(), request.getOperation(), request.getOperatorId())));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Exception e) {
                    response.setResponseStatus(
                        ResponseStatusUtil.newFailStatus(e.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public GetRouteRuleLogsResponse getRouteRuleLogs(final GetRouteRuleLogsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management-log.route-rule-logs",
            () -> {
                GetRouteRuleLogsResponse response = new GetRouteRuleLogsResponse();
                if (!check(request, response)) {
                    return response;
                }
                try {
                    response.setLogs(routeRuleLogDao
                        .select(new RouteRuleLogModel(request.getServiceId(), request.getName(),
                            request.getOperation(), request.getOperatorId())));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Exception e) {
                    response.setResponseStatus(
                        ResponseStatusUtil.newFailStatus(e.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public GetRouteRuleGroupLogsResponse getRouteRuleGroupLogs(final GetRouteRuleGroupLogsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management-log.route-rule-group-logs",
            () -> {
                GetRouteRuleGroupLogsResponse response = new GetRouteRuleGroupLogsResponse();
                if (!check(request, response)) {
                    return response;
                }
                try {
                    response.setLogs(routeRuleGroupLogDao.select(
                        new RouteRuleGroupLogModel(request.getRouteRuleId(), request.getGroupId(),
                            request.getOperatorId(), request.getOperation())));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Exception e) {
                    response.setResponseStatus(
                        ResponseStatusUtil.newFailStatus(e.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public GetZoneOperationLogsResponse getZoneOperationLogs(final GetZoneOperationLogsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management-log.zone-operation-logs",
            () -> {
                GetZoneOperationLogsResponse response = new GetZoneOperationLogsResponse();
                if (!check(request, response)) {
                    return response;
                }
                try {
                    response.setLogs(zoneOperationLogDao.select(new ZoneOperationLogModel(request.getOperation(),
                        request.getZoneId(), request.getServiceId(),
                        request.getRegionId(), request.getOperatorId()), request.getComplete()));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Exception e) {
                    response.setResponseStatus(
                        ResponseStatusUtil.newFailStatus(e.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public GetGroupInstanceLogsResponse getGroupInstanceLogs(final GetGroupInstanceLogsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management-log.group-instance-logs",
            () -> {
                GetGroupInstanceLogsResponse response = new GetGroupInstanceLogsResponse();
                if (!check(request, response)) {
                    return response;
                }
                try {
                    response.setLogs(groupInstanceLogDao
                        .select(new GroupInstanceLogModel(request.getGroupId(), request.getInstanceId(),
                            request.getOperatorId(), request.getOperation())));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Exception e) {
                    response.setResponseStatus(
                        ResponseStatusUtil.newFailStatus(e.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public GetServiceInstanceLogsResponse getServiceInstanceLogs(final GetServiceInstanceLogsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management-log.service-instance-logs",
            () -> {
                GetServiceInstanceLogsResponse response = new GetServiceInstanceLogsResponse();
                if (!check(request, response)) {
                    return response;
                }
                try {
                    response.setLogs(serviceInstanceLogDao.select(
                        new ServiceInstanceLogModel(request.getServiceId(), request.getInstanceId(),
                            request.getOperatorId(), request.getOperation())));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Exception e) {
                    response.setResponseStatus(
                        ResponseStatusUtil.newFailStatus(e.getMessage(), ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    private boolean check(Object request, HasResponseStatus response) {
        if (request == null) {
            response.setResponseStatus(ResponseStatusUtil.newFailStatus("request is null", ErrorCodes.BAD_REQUEST));
            return false;
        }
        return true;
    }
}
