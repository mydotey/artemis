package org.mydotey.artemis.management;

import org.mydotey.artemis.ErrorCodes;
import org.mydotey.artemis.HasResponseStatus;
import org.mydotey.artemis.management.zone.*;
import org.mydotey.artemis.management.zone.model.ZoneOperationModel;
import org.mydotey.artemis.management.zone.util.ZoneOperationsUtil;
import org.mydotey.artemis.trace.ArtemisTraceExecutor;
import org.mydotey.artemis.util.ResponseStatusUtil;
import org.mydotey.artemis.util.ServiceNodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fang_j on 10/07/2016.
 */
public class ZoneServiceImpl implements ZoneService {
    private static volatile ZoneServiceImpl instance;

    public static ZoneServiceImpl getInstance() {
        if (instance == null) {
            synchronized (ZoneServiceImpl.class) {
                if (instance == null)
                    instance = new ZoneServiceImpl();
            }
        }

        return instance;
    }

    private static final Logger logger = LoggerFactory.getLogger(ZoneServiceImpl.class);
    private final ZoneRepository zoneRepository = ZoneRepository.getInstance();

    private ZoneServiceImpl() {
    }

    @Override
    public GetAllZoneOperationsResponse getAllZoneOperations(final GetAllZoneOperationsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management.get.all-zone-operations",
            () -> {
                GetAllZoneOperationsResponse response = new GetAllZoneOperationsResponse();
                if (!check(request, response)) {
                    return response;
                }

                try {
                    response.setAllZoneOperations(zoneRepository.getAllZoneOperations(request.getRegionId()));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Throwable ex) {
                    String errorMessage = "get all-zone-operations failed.";
                    logger.error(errorMessage, ex);
                    response.setResponseStatus(ResponseStatusUtil.newFailStatus(errorMessage + ex.getMessage(),
                        ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public GetZoneOperationsResponse getZoneOperations(final GetZoneOperationsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management.get.zone-operations",
            () -> {
                GetZoneOperationsResponse response = new GetZoneOperationsResponse();
                if (!check(request, response)) {
                    return response;
                }

                try {
                    response.setOperations(zoneRepository.getZoneOperations(request.getZoneKey()));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Throwable ex) {
                    String errorMessage = "get zone-operations failed.";
                    logger.error(errorMessage, ex);
                    response.setResponseStatus(ResponseStatusUtil.newFailStatus(errorMessage + ex.getMessage(),
                        ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public GetZoneOperationsListResponse getZoneOperationsList(final GetZoneOperationsListRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management.get.zone-operations-list",
            () -> {
                GetZoneOperationsListResponse response = new GetZoneOperationsListResponse();
                if (!check(request, response)) {
                    return response;
                }

                try {
                    response.setZoneOperationsList(zoneRepository.getZoneOperationsList(new ZoneOperationModel(
                        request.getRegionId(), request.getServiceId(), request.getZoneId())));
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Throwable ex) {
                    String errorMessage = "get zone-operations-list failed.";
                    logger.error(errorMessage, ex);
                    response.setResponseStatus(ResponseStatusUtil.newFailStatus(errorMessage + ex.getMessage(),
                        ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public IsZoneDownResponse isZoneDown(final IsZoneDownRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management.get.is-zone-down",
            () -> {
                IsZoneDownResponse response = new IsZoneDownResponse();
                if (!check(request, response)) {
                    return response;
                }

                try {
                    response.setDown(zoneRepository.getZoneOperations(request.getZoneKey()) != null);
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Throwable ex) {
                    String errorMessage = "is-zone-down failed.";
                    logger.error(errorMessage, ex);
                    response.setResponseStatus(ResponseStatusUtil.newFailStatus(errorMessage + ex.getMessage(),
                        ErrorCodes.INTERNAL_SERVICE_ERROR));
                    return response;
                }
            });
    }

    @Override
    public OperateZoneOperationsResponse operateZoneOperations(final OperateZoneOperationsRequest request) {
        return ArtemisTraceExecutor.INSTANCE.execute("artemis.service.management.operate.zone-operations",
            () -> {
                OperateZoneOperationsResponse response = new OperateZoneOperationsResponse();
                if (!updateCheck(request, response)) {
                    return response;
                }

                try {
                    zoneRepository.operateGroupOperations(request,
                        ZoneOperationsUtil.newZoneOperationModels(request.getZoneOperationsList()),
                        request.isOperationComplete());
                    response.setResponseStatus(ResponseStatusUtil.SUCCESS_STATUS);
                    return response;
                } catch (Throwable ex) {
                    String errorMessage = "operate zone-operations failed.";
                    logger.error(errorMessage, ex);
                    response.setResponseStatus(ResponseStatusUtil.newFailStatus(errorMessage + ex.getMessage(),
                        ErrorCodes.INTERNAL_SERVICE_ERROR));
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
