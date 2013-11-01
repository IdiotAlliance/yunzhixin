package com.dt.cloudmsg.beans;

import com.google.gson.annotations.Expose;

/**
 * Created by lvxiang on 13-10-5.
 */
public class SendReturnMsg extends BaseBean{

    @Expose private String imei;
    @Expose private int errcode;
    @Expose private String msg;
    public String getImei() {
        return imei;
    }
    public void setImei(String imei) {
        this.imei = imei;
    }
    public int getErrcode() {
        return errcode;
    }
    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
