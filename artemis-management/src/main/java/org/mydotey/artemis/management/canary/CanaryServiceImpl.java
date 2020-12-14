package org.mydotey.artemis.management.canary;

import com.google.common.collect.Sets;

import org.mydotey.artemis.ErrorCodes;
import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.management.GroupRepository;
import org.mydotey.artemis.management.group.model.GroupModel;
import org.mydotey.artemis.management.group.model.RouteRuleGroupModel;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.artemis.util.ServiceGroups;
import org.mydotey.artemis.util.ServiceNodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fang_j on 10/07/2016.
 */
public class CanaryServiceImpl implements CanaryService {
    private static final Logger logger = LoggerFactory.getLogger(CanaryServiceImpl.class);
    private static CanaryServiceImpl instance;

    public static CanaryServiceImpl getInstance() {
        if (instance == null) {
            synchronized (CanaryServiceImpl.class) {
                if (instance == null) {
                    instance = new CanaryServiceImpl();
                }
            }
        }
        return instance;
    }

    private final GroupRepository groupRepository = GroupRepository.getInstance();

    @Override
    public UpdateCanaryIPsResponse updateCanaryIPs(final UpdateCanaryIPsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.canary.update.canary-ips",
            () -> {
                UpdateCanaryIPsResponse response = new UpdateCanaryIPsResponse();
                if (!updateCheck(request, response)) {
                    return response;
                }
                try {
                    GroupModel group = CanaryServices.generateCanaryGroup(request.getServiceId(),
                        request.getAppId());
                    Long routeRuleId = groupRepository
                        .generateRouteRule(CanaryServices.generateCanaryRouteRule(request.getServiceId()))
                        .getRouteRuleId();
                    Long groupId = groupRepository.generateGroup(group).getGroupId();
                    groupRepository.generateRouteRuleGroup(
                        new RouteRuleGroupModel(null, routeRuleId, groupId, ServiceGroups.DEFAULT_WEIGHT_VALUE));
                    groupRepository.updateGroupInstances(request, groupId, Sets.newHashSet(request.getCanaryIps()));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    groupRepository.waitForPeerSync();
                    return response;
                } catch (Throwable ex) {
                    String errorMessage = "update canary ips";
                    logger.error(errorMessage, ex);
                    response.setResponseStatus(
                        ResponseStatusUtil.newFailStatus(errorMessage, ErrorCodes.INTERNAL_SERVICE_ERROR));
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

    private boolean updateCheck(Object request, HasResponseStatus response) {
        return ServiceNodeUtil.checkCurrentNode(response) && check(request, response);
    }
}
