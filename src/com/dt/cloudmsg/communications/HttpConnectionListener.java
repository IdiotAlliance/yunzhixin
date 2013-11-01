package com.dt.cloudmsg.communications;

public interface HttpConnectionListener {

	public void onMessage(int statusCode, String msg);
	
}
