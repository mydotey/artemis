package org.mydotey.artemis.registry.replication;

import org.mydotey.artemis.registry.RegistryService;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public interface RegistryReplicationService extends RegistryService {

    GetServicesResponse getServices(GetServicesRequest request);

}
