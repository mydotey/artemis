package org.mydotey.artemis.management.zone;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetZoneOperationsRequest {
    private ZoneKey zoneKey;

    public ZoneKey getZoneKey() {
        return zoneKey;
    }

    public void setZoneKey(ZoneKey zoneKey) {
        this.zoneKey = zoneKey;
    }

    @Override
    public String toString() {
        return "GetZoneOperationsRequest{" +
                "zoneKey=" + zoneKey +
                '}';
    }
}
