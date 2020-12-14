package org.mydotey.artemis.config;

import java.util.Comparator;

import org.mydotey.java.StringExtension;
import org.mydotey.scf.Property;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
@SuppressWarnings("rawtypes")
public class PropertyComparator implements Comparator<Property> {

    public static final PropertyComparator DEFAULT = new PropertyComparator();

    @Override
    public int compare(Property o1, Property o2) {
        if (o1 == o2)
            return 0;

        if (StringExtension.isBlank(o1.getConfig().getKey().toString()))
            return -1;

        if (StringExtension.isBlank(o2.getConfig().getKey().toString()))
            return 1;

        return o1.getConfig().getKey().toString().compareTo(o2.getConfig().getKey().toString());
    }

}
