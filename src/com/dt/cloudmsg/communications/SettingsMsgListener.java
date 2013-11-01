package com.dt.cloudmsg.communications;

import android.content.Context;

/**
 * Created by lvxiang on 13-10-18.
 */
public class SettingsMsgListener extends AbstractConnectionListener{
    public SettingsMsgListener(Context context, String intent) {
        super(context, intent);
    }

    @Override
    public void onMessage(int statusCode, String msg){
        super.onMessage(statusCode, msg);
    }
}
