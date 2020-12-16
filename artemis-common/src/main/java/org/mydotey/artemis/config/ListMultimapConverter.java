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
            KeyValuePair<String, String> pair = KeyValuePair.parse(pairValue);
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

}
