package org.mydotey.artemis.util;

import org.mydotey.artemis.ErrorCodes;
import org.mydotey.artemis.HasResponseStatus;

/**
 * Created by fang_j on 10/07/2016.
 */
public class Requests {

    public static boolean check(Object request, HasResponseStatus response) {
        if (request == null) {
            response.setResponseStatus(ResponseStatusUtil.newFailStatus("request is null", ErrorCodes.BAD_REQUEST));
            return false;
        }
        return true;
    }

    public static boolean updateCheck(Object request, HasResponseStatus response) {
        return ServiceNodeUtil.checkCurrentNode(response) && check(request, response);
    }

}
