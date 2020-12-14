package org.mydotey.artemis.client;

import org.mydotey.artemis.Instance;

/**
 * Created by fang_j on 10/07/2016.
 */
public interface RegistryClient {

    void register(Instance... instances);

    void unregister(Instance... instances);

}