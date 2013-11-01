package com.dt.cloudmsg.beans;

import java.util.Date;
import java.util.List;

import com.dt.cloudmsg.model.Device;
import com.google.gson.annotations.Expose;

public class UserBasicInfo extends BaseBean{
	
	@Expose private String username;
	@Expose private int status;
	@Expose private int privilege;
	@Expose private Date expire;
    @Expose private String token;
    @Expose private List<Device> devices;
    @Expose private String apiKey;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getPrivilege() {
		return privilege;
	}
	public void setPrivilege(int privilege) {
		this.privilege = privilege;
	}
	public Date getExpire() {
		return expire;
	}
	public void setExpire(Date expire) {
		this.expire = expire;
	}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }
}
