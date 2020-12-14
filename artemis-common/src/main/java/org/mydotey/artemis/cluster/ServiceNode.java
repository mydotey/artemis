package org.mydotey.artemis.cluster;

import java.util.Objects;

import org.mydotey.artemis.Zone;
import org.mydotey.java.StringExtension;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ServiceNode {

    private Zone _zone;
    private String _url;

    public ServiceNode() {

    }

    public ServiceNode(Zone zone, String url) {
        _zone = zone;
        _url = url;
    }

    public Zone getZone() {
        return _zone;
    }

    public void setZone(Zone zone) {
        _zone = zone;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    @Override
    public String toString() {
        return StringExtension.toLowerCase(_zone.getRegionId() + "/" + _zone.getZoneId() + "/" + _url);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
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
