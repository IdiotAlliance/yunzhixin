package com.dt.cloudmsg.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lvxiang on 13-9-30.
 */
public class MsgIdGenerator {

    private static final AtomicInteger ai = new AtomicInteger(0);

    public static final int genIntegerId(){
        int result = ai.addAndGet(1);
        if(result > 0)
            return result;
        ai.compareAndSet(ai.get(), 0);
        return ai.incrementAndGet();
    }

}
