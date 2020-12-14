package org.mydotey.artemis.management;

import org.mydotey.artemis.management.instance.*;
import org.mydotey.artemis.management.server.*;

/**
 * Created by fang_j on 10/07/2016.
 */
public interface ManagementService {

    OperateInstanceResponse operateInstance(OperateInstanceRequest request);

    OperateServerResponse operateServer(OperateServerRequest request);

    GetInstanceOperationsResponse getInstanceOperations(GetInstanceOperationsRequest request);

    GetServerOperationsResponse getServerOperations(GetServerOperationsRequest request);

    GetAllInstanceOperationsResponse getAllInstanceOperations(GetAllInstanceOperationsRequest request);

    GetAllServerOperationsResponse getAllServerOperations(GetAllServerOperationsRequest request);

    IsInstanceDownResponse isInstanceDown(IsInstanceDownRequest request);

    IsServerDownResponse isServerDown(IsServerDownRequest request);

    GetServicesResponse getServices(GetServicesRequest request);
    
    GetServiceResponse getService(GetServiceRequest request);

}
