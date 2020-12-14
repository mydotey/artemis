package org.mydotey.artemis.config;

import java.util.Arrays;

import org.mydotey.java.StringExtension;
import org.mydotey.java.collection.CollectionExtension;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public final class PropertyKeyGenerator {

    private PropertyKeyGenerator() {

    }

    public static String generateKey(String... parts) {
        if (CollectionExtension.isEmpty(parts))
            return null;

        if (parts.length == 1)
            return StringExtension.trim(parts[0]);

        if (parts.length == 2)
            return generateKey(parts[0], parts[1]);

        return generateKey(parts[0], generateKey(Arrays.copyOfRange(parts, 1, parts.length)));
    }

    private static String generateKey(String part1, String part2) {
        if (part2 == null)
            return null;

        part2 = StringExtension.trim(part2);
        if (part2.isEmpty())
            return StringExtension.EMPTY;

        if (part1 == null)
            return part2;

        part1 = StringExtension.trim(part1);
        if (part1.isEmpty())
            return part2;

        return part1 + "." + part2;
    }

}
