package org.mydotey.artemis;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.mydotey.java.StringExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class Region {

    private String _regionId;
    private Map<String, String> _metadata;
    private List<Zone> _zones;

    public Region() {

    }

    public Region(String regionId, List<Zone> zones) {
        this(regionId, zones, null);
    }

    public Region(String regionId, List<Zone> zones, Map<String, String> metadata) {
        _regionId = regionId;
        _zones = zones;
        _metadata = metadata;
    }

    public String getRegionId() {
        return _regionId;
    }

    public void setRegionId(String regionId) {
        _regionId = regionId;
    }

    public List<Zone> getZones() {
        return _zones;
    }

    public void setZones(List<Zone> zones) {
        _zones = zones;
    }

    public Map<String, String> getMetadata() {
        return _metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        _metadata = metadata;
    }

    @Override
    public String toString() {
        return StringExtension.toLowerCase(_regionId);
    }

    @Override
    public int hashCode() {
        String string = toString();
        return string == null ? 0 : string.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;

        if (other.getClass() != this.getClass())
            return false;

        return Objects.equals(toString(), other.toString());
    }

}
