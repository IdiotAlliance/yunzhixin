package com.dt.cloudmsg.beans;

import com.google.gson.annotations.Expose;

/**
 * Created by lvxiang on 13-10-7.
 */
public class ChangePassword extends BaseBean{

    @Expose private String newPass;
    @Expose private String token;

    public ChangePassword(String newPass, String token) {
        super();
        this.newPass = newPass;
        this.token = token;
    }
    public String getNewPass() {
        return newPass;
    }
    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

}
