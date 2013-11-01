package com.dt.cloudmsg.communications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dt.cloudmsg.util.IntentConstants;

import org.apache.http.HttpStatus;

/**
 * Created by lvxiang on 13-9-30.
 */
public class MyServiceConnectionMsgListener extends AbstractConnectionListener{

    private int msgId = -1;

    public MyServiceConnectionMsgListener(Context context, String action, int msgId){
        super(context, action);
        this.msgId = msgId;
    }

    public MyServiceConnectionMsgListener(Context context, String intent) {
        super(context, intent);
    }

    @Override
    public void onMessage(int statusCode, String msg) {
        if(msgId == -1)
            super.onMessage(statusCode, msg);
        else {
            switch(statusCode){
                case HttpStatus.SC_OK:{
                    Intent intent = new Intent(this.action);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(IntentConstants.KEY_INTENT_CODE, IntentConstants.IntentCode.INTENT_OK);
                    bundle.putString(IntentConstants.KEY_INTENT_BODY, msg);
                    bundle.putInt(IntentConstants.KEY_INTENT_MSGID, msgId);
                    intent.putExtras(bundle);
                    broadcast(intent);
                    break;
                }
                case HttpStatus.SC_FORBIDDEN:{
                    this.broadcastForbidden();
                    break;
                }
                case HttpStatus.SC_NOT_FOUND:{
                    this.broadcaseNotFound();
                    break;
                }
                case HttpStatus.SC_REQUEST_TIMEOUT:{
                    this.broadcastTimeout();
                    break;
                }
                case -1:{
                    this.broadcastConnectionException();
                    break;
                }
                default:{
                    this.broadcastUnknown();
                    break;
                }
            }
        }
    }
}
