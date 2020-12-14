package org.mydotey.artemis.util;

/**
 * Created by Qiang Zhao on 10/07/2016.
 */
public interface RequestExecutor<C, Req, Res> {

    Res execute(C client, Req request);

}
