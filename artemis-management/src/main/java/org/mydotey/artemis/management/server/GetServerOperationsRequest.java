package org.mydotey.artemis.management.server;

import org.mydotey.artemis.ServerKey;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetServerOperationsRequest {

    private ServerKey serverKey;

    public ServerKey getServerKey() {
        return serverKey;
    }

    public void setServerKey(ServerKey serverKey) {
        this.serverKey = serverKey;
    }

}
