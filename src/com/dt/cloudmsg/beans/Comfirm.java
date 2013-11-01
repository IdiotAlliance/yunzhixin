package com.dt.cloudmsg.beans;

import com.google.gson.annotations.Expose;

/**
 * Created by lvxiang on 13-9-24.
 */
public class Comfirm extends BaseBean{

    @Expose String username;
    @Expose String password;
    @Expose String code;
    @Expose String nonce;
    @Expose String api_key;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getNonce() {
        return nonce;
    }
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
    public String getApi_key() {
        return api_key;
    }
    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
