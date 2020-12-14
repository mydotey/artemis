package org.mydotey.artemis.util;

import org.mydotey.artemis.Region;
import org.mydotey.artemis.checker.ValueChecker;
import org.mydotey.artemis.config.DeploymentConfig;
import org.mydotey.java.ObjectExtension;
import org.mydotey.java.StringExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class SameRegionChecker implements ValueChecker<Region> {

    public static final SameRegionChecker DEFAULT = new SameRegionChecker();

    public void check(String regionId, String valueName) {
        if (!isSameRegion(regionId))
            throw new IllegalArgumentException(
                valueName + " is not the current region." + " expected: " + DeploymentConfig.regionId() + ", actual: "
                    + regionId);
    }

    @Override
    public void check(Region value, String valueName) {
        ObjectExtension.requireNonNull(value, valueName);
        check(value.getRegionId(), valueName);
    }

    public boolean isSameRegion(String regionId) {
        if (StringExtension.isBlank(regionId))
            return false;

        return regionId.equalsIgnoreCase(DeploymentConfig.regionId());
    }

}
