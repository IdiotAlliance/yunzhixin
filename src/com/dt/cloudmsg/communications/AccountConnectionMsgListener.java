package com.dt.cloudmsg.communications;

import com.dt.cloudmsg.util.LogUtils;
import org.apache.http.HttpStatus;

import com.dt.cloudmsg.util.IntentConstants;
import com.dt.cloudmsg.util.IntentConstants.IntentCode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AccountConnectionMsgListener extends AbstractConnectionListener{

	public AccountConnectionMsgListener(Context context, String action) {
		super(context, action);
	}

	@Override
	public void onMessage(int statusCode, String msg) {
        super.onMessage(statusCode, msg);
        LogUtils.d(AccountConnectionMsgListener.class.getName(), msg);
    }

}
