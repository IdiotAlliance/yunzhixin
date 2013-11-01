package com.dt.cloudmsg.service;

/**
 * Created by lvxiang on 13-10-23.
 */
public class SmsHolder {

    private int msgId;
    private String imei;

    public SmsHolder(int msgId, String imei){
        this.msgId = msgId;
        this.imei = imei;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }
}
