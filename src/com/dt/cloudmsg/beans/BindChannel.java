package com.dt.cloudmsg.beans;

import com.google.gson.annotations.Expose;

public class BindChannel extends BaseBean{

	@Expose private String imei;
	@Expose private long   channleID;
	@Expose private String userID;
	
	public BindChannel(String imei, long channelID, String userID){
		this.imei = imei;
		this.channleID = channelID;
		this.userID = userID;
	}
	
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public long getChannleID() {
		return channleID;
	}
	public void setChannleID(long channleID) {
		this.channleID = channleID;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
}
