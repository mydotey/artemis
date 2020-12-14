package org.mydotey.artemis.config;

import org.mydotey.java.StringExtension;
import org.mydotey.java.collection.KeyValuePair;
import org.mydotey.scf.type.AbstractTypeConverter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Created by Qiang Zhao on 10/05/2016.
 */
public class ListMultimapConverter extends AbstractTypeConverter<String, ListMultimap<String, String>> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ListMultimapConverter() {
        super(String.class, (Class) ListMultimap.class);
    }

    public static final ListMultimapConverter DEFAULT = new ListMultimapConverter();

    @Override
    public ListMultimap<String, String> convert(String source) {
        if (StringExtension.isBlank(source))
            return null;

        source = source.trim();
        ListMultimap<String, String> listMultimap = ArrayListMultimap.create();
        String[] pairValues = source.trim().split(";");
        for (String pairValue : pairValues) {
            KeyValuePair<String, String> pair = toKeyValuePair(pairValue);
            if (pair == null)
                continue;

            String[] sources = pair.getValue().split(",");
            for (String item : sources) {
                if (StringExtension.isBlank(item))
                    continue;

                listMultimap.put(pair.getKey(), item.trim());
            }
        }

        return listMultimap.size() == 0 ? null : listMultimap;
    }

    public static KeyValuePair<String, String> toKeyValuePair(String s) {
        return toKeyValuePair(s, ":");
    }

    public static KeyValuePair<String, String> toKeyValuePair(String s, String separator) {
        if (StringExtension.isBlank(s) || StringExtension.isBlank(separator))
            return null;

        s = s.trim();
        separator = separator.trim();

        String[] parts = s.split(separator, 2);
        if (parts.length != 2)
            return null;

        if (StringExtension.isBlank(parts[0]) || StringExtension.isBlank(parts[1]))
            return null;

        return new KeyValuePair<String, String>(parts[0].trim(), parts[1].trim());
    }

}
