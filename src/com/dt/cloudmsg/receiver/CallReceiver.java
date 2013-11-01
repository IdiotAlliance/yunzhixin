package com.dt.cloudmsg.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.dt.cloudmsg.beans.MessageBean;
import com.dt.cloudmsg.util.IntentConstants;

/**
 * Created by lvxiang on 13-9-28.
 */
public class CallReceiver extends BroadcastReceiver{

    private static boolean isCall = false;
    private long startTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            //如果是去电（拨出）
            //System.out.println("拨出");
        }else{
            //System.out.println("来电");
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(new MyPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE);
            //设置一个监听器
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener{

        private Context context;

        public MyPhoneStateListener(Context context){
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // TODO Auto-generated method stub
            //state 当前状态 incomingNumber,貌似没有去电的API
            super.onCallStateChanged(state, incomingNumber);
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:{
                    System.out.println("挂断");
                    if(isCall){
                        long ringTime = (System.currentTimeMillis() - startTime) / 1000;
                        if(context != null){
                            Intent intent = new Intent(IntentConstants.INTENT_ACTION_CALL_RECV);
                            Bundle bundle = new Bundle();
                            bundle.putString(IntentConstants.KEY_INTENT_CALL_FROM, incomingNumber);
                            bundle.putString(IntentConstants.KEY_INTENT_CALL_TIME, ringTime + "");
                            //bundle.putString(IntentConstants.KEY_INTENT_CALL_TYPE, );
                            bundle.putLong(IntentConstants.KEY_INTENT_CALL_TS, startTime);
                            bundle.putInt(IntentConstants.KEY_INTENT_CALL_TYPE, MessageBean.TYPE_MSG_CAL_MISS);
                            intent.putExtras(bundle);
                            context.sendBroadcast(intent);
                        }
                        isCall = false;
                    }
                    break;
                }
                case TelephonyManager.CALL_STATE_OFFHOOK:{
                    System.out.println("接听");
                    if(isCall){
                        isCall = false;
                        if(context != null){

                        }
                    }
                    break;
                }
                case TelephonyManager.CALL_STATE_RINGING:{
                    System.out.println("响铃:来电号码"+incomingNumber);
                    startTime = System.currentTimeMillis();
                    isCall = true;
                    break;
                }
            }
        }

    };
}
