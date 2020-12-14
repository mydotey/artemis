package org.mydotey.artemis;

import java.util.Objects;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public class ServiceGroupKey {

    private String groupKey;

    public ServiceGroupKey() {

    }

    public ServiceGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    @Override
    public String toString() {
        return String.valueOf(groupKey).toLowerCase();
    };

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (other == null)
            return false;

        if (other.getClass() != this.getClass())
            return false;

        return Objects.equals(toString(), other.toString());
    }

}
