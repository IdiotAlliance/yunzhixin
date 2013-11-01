package com.dt.cloudmsg.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pack implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int DEVICE_SERVER  = 0x00000000;
	public static final int DEVICE_ANDROID = 0x00000001;
	public static final int DEVICE_IOS	   = 0x00000002;
	public static final int DEVICE_WP	   = 0x00000003;
	
	public static final String PACK_TYPE_RESPONSE = "res";
	
	///Server-side handler
	public static final String HANDLER_NULL     = "NullHandler";
	public static final String HANDLER_LOGIN    = "LoginHandler";
	public static final String HANDLER_LOGOUT   = "LogoutHandler";
	public static final String HANDLER_REGISTER = "RegisterHandler";
	
	private String type;  // package dispatch type
	private int    device;// device category
	private long   pid;   // package id
	private Account account;
	private List<Multipart> data;
	
	public Pack(){
		
	}
	
	public Pack(String type,long pid,int device){
		this.type = type;
		this.pid  = pid;
		this.device = device;
	}
	
	public Pack(String type,long pid,int device, Account account, List<Multipart> data){
		this.type    = type;
		this.pid     = pid;
		this.device  = device;
		this.account = account;
		this.data    = data;
	}
	
	public void setPkgType(String pkgType){
		this.type = pkgType;
	}
	
	public String getPkgType(){
		return this.type;
	}
	
	public int getDevice(){
		return this.device;
	}
	
	public void setDevice(int device){
		this.device = device;
	}
	
	public long getPID(){
		return this.pid;
	}
	
	public void setPID(long pid){
		this.pid = pid;
	}
	
	public Account getAccount(){
		return this.account;
	}
	
	public void setAccount(Account account){
		this.account = account;
	}
	
	public List<Multipart> getData(){
		return this.data;
	}
	
	public void addData(Multipart multi){
		if(data==null)
			data = new ArrayList<Multipart>();
		data.add(multi);
	}
	
	@Override
	public String toString(){
		String string = "<package type=\"" + type +"\" pid=\"" + pid + "\" device=\"" + device +"\" >";
		string += account;
		for(Multipart multipart: data)
			string += multipart;
		string += "</package>";
		return string;
	}
}
