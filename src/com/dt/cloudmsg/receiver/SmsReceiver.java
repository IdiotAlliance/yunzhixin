package com.dt.cloudmsg.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import com.dt.cloudmsg.util.IntentConstants;
import com.dt.cloudmsg.util.LogUtils;

/**
 * Created by lvxiang on 13-9-24.
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(SMS_RECEIVED_ACTION)){
            for(SmsMessage msg: getMessagesFromIntent(intent)){
                LogUtils.d(BroadcastReceiver.class.getName(), msg.getDisplayMessageBody());
                // TODO 发送广播给短信监听器
                Intent intent1 = new Intent(IntentConstants.INTENT_ACTION_SMS_RECV);
                Bundle extras = new Bundle();
                extras.putString(IntentConstants.KEY_INTENT_SMS_FROM, msg.getDisplayOriginatingAddress());
                extras.putLong(IntentConstants.KEY_INTENT_SMS_TS, msg.getTimestampMillis());
                extras.putString(IntentConstants.KEY_INTENT_SMS_BODY, msg.getDisplayMessageBody());
                intent1.putExtras(extras);
                context.sendBroadcast(intent1);
            }
        }
    }

    private final SmsMessage[] getMessagesFromIntent(Intent intent)

    {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];
        for (int i = 0; i < messages.length; i++)
        {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++)
        {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);

        }
        return msgs;

    }
}
