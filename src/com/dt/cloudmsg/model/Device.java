package com.dt.cloudmsg.model;

import com.dt.cloudmsg.beans.BaseBean;
import com.google.gson.annotations.Expose;

public class Device extends BaseBean implements Comparable<Device>{

    /**
     *
     */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    private long _id;
    @Expose private String name;
    @Expose private String imei;
    @Expose private String number;
    @Expose private String account;
    @Expose private int status = STATUS_ONLINE;
    @Expose private boolean serverOn = false;
    @Expose private boolean pushOn = false;
    @Expose private boolean bound = false;
	@Expose private String userid  = null;
	@Expose private long channelid = 0;

    public static final transient int STATUS_ONLINE  = 0x00;
    public static final transient int STATUS_OFFLINE = 0x01;
    public static final transient int STATUS_ERROR = 0x02;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getImei() {
        return imei;
    }
    public void setImei(String imei) {
        this.imei = imei;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public boolean isServerOn() {
        return serverOn;
    }
    public void setServerOn(boolean serverOn) {
        this.serverOn = serverOn;
    }
    public boolean isPushOn() {
        return pushOn;
    }
    public void setPushOn(boolean pushOn) {
        this.pushOn = pushOn;
    }
    public boolean isBound() {
        return bound;
    }
    public void setBound(boolean bound) {
        this.bound = bound;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof Device))
            return false;
        return ((Device)object).getImei().equals(imei);
    }

    @Override
    public int hashCode(){
        return imei.hashCode();
    }

    @Override
    public int compareTo(Device device) {
        return this.number.compareTo(device.getNumber());
    }

    public String getAlias(){
        return name == null ? number : name + "(" + number + ")";
    }

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public long getChannelid() {
		return channelid;
	}

	public void setChannelid(long channelid) {
		this.channelid = channelid;
	}
    
}
