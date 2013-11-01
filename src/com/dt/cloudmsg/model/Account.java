package com.dt.cloudmsg.model;

import java.io.Serializable;
import java.util.Date;

public class Account implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
    private String password;
	private String token;
	private String imei;
    private String key;
    private int status;
    private int privilege;
    private Date expire;

    public Account(){

    }

	public Account(String name,String token,String imei){
		this.name = name;
		this.token = token;
		this.imei = imei;
	}
	
	public String getAccountName(){
		return this.name;
	}
	
	public void setAccountName(String name){
		this.name = name;
	}
	
	public String getToken(){
		return this.token;
	}
	
	public String getIMEI(){
		return this.imei;
	}
	
	public void setIMEI(String imei){
		this.imei = imei;
	}
	
	public void setToken(String token){
		this.token = token;
	}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
	public String toString(){
		String string = "<account>";
		string += "<name>" + name + "</name><imei>" + imei + "</imei>";
		if(token!=null)
			string += "<token>" + token + "</token>";
		string += "</account>";
		
		return string;
	}

}
