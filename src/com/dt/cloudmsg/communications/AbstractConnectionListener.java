package com.dt.cloudmsg.communications;

import android.util.Log;
import com.dt.cloudmsg.R;
import com.dt.cloudmsg.util.IntentConstants;
import com.dt.cloudmsg.util.IntentConstants.IntentCode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.apache.http.HttpStatus;

public abstract class AbstractConnectionListener implements HttpConnectionListener{

	protected String action = null;
	protected Context context = null;
	
	
	
	public AbstractConnectionListener(Context context, String intent){
		this.context = context;
		this.action = intent;
	}

    public void onMessage(int statusCode, String msg){
        switch(statusCode){
            case HttpStatus.SC_OK:{
                Intent intent = new Intent(this.action);
                Bundle bundle = new Bundle();
                bundle.putSerializable(IntentConstants.KEY_INTENT_CODE, IntentCode.INTENT_OK);
                bundle.putString(IntentConstants.KEY_INTENT_BODY, msg);
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
	
	protected void broadcastForbidden(){
		Intent intent = new Intent(this.action);
		Bundle exstras = new Bundle();
		exstras.putSerializable(IntentConstants.KEY_INTENT_CODE, IntentCode.INTENT_ERROR);
		exstras.putString(IntentConstants.KEY_INTENT_MSG, context.getString(R.string.intent_http_forbidden));
		intent.putExtras(exstras);
		this.broadcast(intent);
	}
	
	protected void broadcaseNotFound(){
		Intent intent = new Intent(this.action);
		Bundle exstras = new Bundle();
		exstras.putSerializable(IntentConstants.KEY_INTENT_CODE, IntentCode.INTENT_ERROR);
		exstras.putString(IntentConstants.KEY_INTENT_MSG, context.getString(R.string.intent_http_notfound));
		intent.putExtras(exstras);
		this.broadcast(intent);
	}
	
	protected void broadcastTimeout(){
		Intent intent = new Intent(this.action);
		Bundle exstras = new Bundle();
		exstras.putSerializable(IntentConstants.KEY_INTENT_CODE, IntentCode.INTENT_ERROR);
		exstras.putString(IntentConstants.KEY_INTENT_MSG, context.getString(R.string.intent_http_timeout));
		intent.putExtras(exstras);
		this.broadcast(intent);
	}
	
	protected void broadcastUnknown(){
		Intent intent = new Intent(this.action);
		Bundle exstras = new Bundle();
		exstras.putSerializable(IntentConstants.KEY_INTENT_CODE, IntentCode.INTENT_ERROR);
		exstras.putString(IntentConstants.KEY_INTENT_MSG, context.getString(R.string.intent_http_error));
		intent.putExtras(exstras);
		this.broadcast(intent);
	}
	
	protected void broadcastNetworkUnusable(){
		Intent intent = new Intent(this.action);
		Bundle exstras = new Bundle();
		exstras.putSerializable(IntentConstants.KEY_INTENT_CODE, IntentCode.INTENT_ERROR);
		exstras.putString(IntentConstants.KEY_INTENT_MSG, context.getString(R.string.error_network_unusable));
		intent.putExtras(exstras);
		this.broadcast(intent);
	}

    protected void broadcastConnectionException(){
        Intent intent = new Intent(this.action);
        Bundle exstras = new Bundle();
        exstras.putSerializable(IntentConstants.KEY_INTENT_CODE, IntentCode.INTENT_ERROR);
        exstras.putString(IntentConstants.KEY_INTENT_MSG, context.getString(R.string.error_connection_exception));
        intent.putExtras(exstras);
        this.broadcast(intent);
    }
	
	protected void broadcast(Intent intent){
        Log.d("send broadcast", "some content");
        context.sendBroadcast(intent);
	}
	
}
