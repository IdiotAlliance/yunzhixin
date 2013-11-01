package com.dt.cloudmsg.beans;

/**
 * Created by lvxiang on 13-10-5.
 */
public class HeartBeat extends BaseBean{

    private String token;
    private String imei;

    public HeartBeat(String token, String imei){
        this.token = token;
        this.imei  = imei;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String json) {
        this.token = json;
    }
    public String getImei() {
        return imei;
    }
    public void setImei(String imei) {
        this.imei = imei;
    }

}
