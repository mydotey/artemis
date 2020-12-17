package org.mydotey.artemis.server;

import org.mydotey.artemis.cluster.ClusterManager;
import org.mydotey.artemis.management.ManagementInitializer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author koqizhao
 *
 * Sep 21, 2018
 */
public class ArtemisServer extends SpringBootServletInitializer {

    public static final ArtemisServer INSTANCE = new ArtemisServer();

    private boolean _managementEnabled;

    private ArtemisServer() {
        String mgmtOpt = System.getProperty("artemis.management.enabled", "true");
        _managementEnabled = mgmtOpt.toLowerCase().trim().equals("true");
    }

    public boolean isManagementEnabled() {
        return _managementEnabled;
    }

    public void init() throws Exception {
        if (_managementEnabled)
            ManagementInitializer.INSTANCE.init();

        ClusterManager.INSTANCE.init();
    }

}
