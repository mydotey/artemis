package org.mydotey.artemis.management.group;

/**
 * Created by fang_j on 10/07/2016.
 */
public class GetAllGroupsRequest {
    private String regionId;

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    @Override
    public String toString() {
        return "GetAllGroupRequest{" +
                "regionId='" + regionId + '\'' +
                '}';
    }
}
