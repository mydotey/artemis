package org.mydotey.artemis.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import org.mydotey.artemis.checker.ValueCheckers;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by fang_j on 10/07/2016.
 */
public class SearchTree<K, V> {
    private final Map<K, SearchTree<K, V>> children = Maps.newHashMap();
    private V value;
    private V defaultChildrenValue;

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public V getDefaultChildrenValue() {
        return defaultChildrenValue;
    }

    public void setDefaultChildrenValue(V defaultChildrenValue) {
        this.defaultChildrenValue = defaultChildrenValue;
    }

    public V get(List<K> cascadingFactors) {
        if (CollectionExtension.isEmpty(cascadingFactors)) {
            return value;
        }
        List<K> factors = Lists.newArrayList(cascadingFactors);
        SearchTree<K, V> g = children.get(factors.remove(0));
        if (g == null) {
            return defaultChildrenValue;
        }
        return g.get(factors);
    }

    public V first(List<K> cascadingFactors) {
        if (value != null || CollectionExtension.isEmpty(cascadingFactors)) {
            return value;
        }
        List<K> factors = Lists.newArrayList(cascadingFactors);
        SearchTree<K, V> g = children.get(factors.remove(0));
        if (g == null) {
            return defaultChildrenValue;
        }
        return g.first(factors);
    }

    public void add(List<K> cascadingFactors, V value) {
        if (CollectionExtension.isEmpty(cascadingFactors)) {
            this.value = value;
            return;
        }
        List<K> factors = Lists.newArrayList(cascadingFactors);
        K factor = factors.remove(0);
        ValueCheckers.notNull(factor, "factor");

        SearchTree<K, V> child = children.get(factor);
        if (child == null) {
            child = new SearchTree<>();
            children.put(factor, child);
        }

        child.add(factors, value);
    }

    public void put(K childKey, SearchTree<K, V> searchTree) {
        ValueCheckers.notNull(childKey, "childKey");
        ValueCheckers.notNull(searchTree, "searchTree");
        children.put(childKey, searchTree);
    }
}
