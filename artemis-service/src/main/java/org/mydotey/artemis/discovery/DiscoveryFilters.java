package org.mydotey.artemis.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mydotey.java.collection.CollectionExtension;

public class DiscoveryFilters {

    public static final DiscoveryFilters INSTANCE = new DiscoveryFilters();

    private volatile List<DiscoveryFilter> _filters = new ArrayList<>();

    private DiscoveryFilters() {

    }

    public synchronized void registerFilter(DiscoveryFilter... filters) {
        if (CollectionExtension.isEmpty(filters))
            return;

        List<DiscoveryFilter> newFilters = new ArrayList<>(_filters);
        for (DiscoveryFilter filter : filters) {
            if (filter == null)
                continue;

            newFilters.add(filter);
        }

        _filters = Collections.unmodifiableList(newFilters);
    }

    public List<DiscoveryFilter> getFilters() {
        return _filters;
    }

}
