package com.dt.cloudmsg.receiver;

import com.baidu.android.pushservice.PushConstants;
import com.dt.cloudmsg.util.IntentConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class BDMessageReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(PushConstants.ACTION_MESSAGE)){
			String message = intent.getExtras().getString(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
			if(message != null){
				Intent intent1 = new Intent(IntentConstants.INTENT_ACTION_MSG_RECV);
	            Bundle bundle1 = new Bundle();
	            bundle1.putString(IntentConstants.KEY_INTENT_BODY, message);
	            intent1.putExtras(bundle1);
	            context.sendBroadcast(intent1);
            }
		}
		else if(intent.getAction().equals(PushConstants.ACTION_RECEIVE)){
			// 处理 bind、setTags等方法口的返回数据
			final String method = intent.getStringExtra(PushConstants.EXTRA_METHOD);
			final int errorCode = intent.getIntExtra(PushConstants.EXTRA_ERROR_CODE, PushConstants.ERROR_SUCCESS);
			final String content = new String(intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
			if(method != null){
				Intent intent1 = new Intent(IntentConstants.INTENT_ACTION_BDPUSH_METHOD);
				intent1.putExtra(IntentConstants.KEY_INTENT_METHOD, method);
				intent1.putExtra(IntentConstants.KEY_INTENT_ERRCODE, errorCode);
				intent1.putExtra(IntentConstants.KEY_INTENT_EXCONTENT, content);
				context.sendBroadcast(intent1);
			}
		}
	}

}
