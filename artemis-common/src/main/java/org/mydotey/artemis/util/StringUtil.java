package org.mydotey.artemis.util;

import org.mydotey.codec.json.JacksonJsonCodec;
import org.mydotey.java.StringExtension;
import org.mydotey.java.net.NetworkInterfaceManager;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public final class StringUtil {

    private StringUtil() {

    }

    public static boolean hasLocalhost(String s) {
        if (s == null)
            return false;

        return s.indexOf(NetworkInterfaceManager.INSTANCE.localhostIP()) != -1;
    }

    public static String concatPathParts(String... pathParts) {
        if (pathParts == null)
            return null;

        String url = null;
        for (String item : pathParts) {
            if (StringExtension.isBlank(item))
                continue;
            item = item.trim();

            if (url == null)
                url = item;
            else
                url += (url.endsWith("/") ? StringExtension.EMPTY : "/")
                    + StringExtension.nullToEmpty(StringExtension.trimStart(item, '/'));
        }

        return url;
    }

    public static String toJson(Object obj) {
        return new String(JacksonJsonCodec.DEFAULT.encode(obj));
    }

}
