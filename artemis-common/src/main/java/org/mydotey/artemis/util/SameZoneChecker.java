package org.mydotey.artemis.util;

import org.mydotey.artemis.Zone;
import org.mydotey.artemis.checker.ValueChecker;
import org.mydotey.artemis.config.DeploymentConfig;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class SameZoneChecker implements ValueChecker<Zone> {

    public static final SameZoneChecker DEFAULT = new SameZoneChecker();

    public void check(String zoneId, String valueName) {
        if (!isSameZone(zoneId))
            throw new IllegalArgumentException(valueName + " is not the current zone." + " expected: "
                + DeploymentConfig.zoneId() + ", actual: " + zoneId);
    }

    @Override
    public void check(Zone value, String valueName) {
        ObjectExtension.requireNonNull(value, valueName);
        SameRegionChecker.DEFAULT.check(value.getRegionId(), valueName);
        check(value.getZoneId(), valueName);
    }

    public boolean isSameZone(String zoneId) {
        if (StringExtension.isBlank(zoneId))
            return false;

        return zoneId.equalsIgnoreCase(DeploymentConfig.zoneId());
    }

}
