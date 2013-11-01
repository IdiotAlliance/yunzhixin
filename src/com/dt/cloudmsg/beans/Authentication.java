package com.dt.cloudmsg.beans;

import com.dt.cloudmsg.util.Encoder;
import com.dt.cloudmsg.util.JsonUtil;
import com.dt.cloudmsg.util.SystemConstants;
import com.google.gson.annotations.Expose;

public class Authentication extends BaseBean{
	
	@Expose private String username;
	@Expose private String password;
    @Expose private String nonce;
    @Expose private String apiKey;
    @Expose private String imei;
	
	public Authentication(String username, String password){
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }
}
