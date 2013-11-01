package com.dt.cloudmsg.communications;

import android.content.Context;

/**
 * Created by lvxiang on 13-9-28.
 */
public class MessageCenterMsgListener extends AbstractConnectionListener{

    public MessageCenterMsgListener(Context context, String intent) {
        super(context, intent);
    }

    @Override
    public void onMessage(int statusCode, String msg) {
        super.onMessage(statusCode, msg);
    }
}
