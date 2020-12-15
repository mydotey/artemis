package org.mydotey.artemis.util;

import org.mydotey.codec.json.JacksonJsonCodec;
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

        return s.indexOf(NetworkInterfaceManager.INSTANCE.hostIP()) != -1;
    }

    public static String toJson(Object obj) {
        return new String(JacksonJsonCodec.DEFAULT.encode(obj));
    }

}
