package com.dt.cloudmsg.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import com.dt.cloudmsg.util.IntentConstants;

/**
 * Created by lvxiang on 13-9-29.
 */
public class JPushReceiver extends BroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(JPushInterface.ACTION_MESSAGE_RECEIVED)){
            Bundle bundle = intent.getExtras();
            String msg = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            Log.d("msg received", msg);
            Intent intent1 = new Intent(IntentConstants.INTENT_ACTION_MSG_RECV);
            Bundle bundle1 = new Bundle();
            bundle1.putString(IntentConstants.KEY_INTENT_BODY, msg);
            intent1.putExtras(bundle1);
            context.sendBroadcast(intent1);
        }
        else if(intent.getAction().equals(JPushInterface.ACTION_NOTIFICATION_RECEIVED)){

        }
    }
}
