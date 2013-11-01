package com.dt.cloudmsg.model;

import java.io.Serializable;
import java.util.Date;

public class Multipart implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int TYPE_PASSWD = 0x0000001;
	public static final int TYPE_TEXT   = 0x0000002;
	public static final int TYPE_IMAGE  = 0x0000003;
	public static final int TYPE_AUDIO  = 0x0000004;
	public static final int TYPE_ERROR  = 0x0000005;
	
	public static final String ENCODING_BASE64   = "BASE64";
	
	
	private int type;
	private String encoding;
	private Date time;
	private String data;
	
	public Multipart(int type,String encoding,String data,Date time){
		this.type = type;
		this.encoding = encoding;
		this.data = data;
		this.time = time;
	}
	
	public int getType(){
		return this.type;
	}
	
	public String getEncoding(){
		return this.encoding;
	}
	
	public Date getTime(){
		return this.time;
	}
	
	public String getData(){
		return this.data;
	}
	
	@Override
	public String toString(){
		String string = "<multipart>";
		string += "<type>" + type + "</type>";
		if(time!=null)
			string += "<time>" + time.getTime() + "</time>";
		if(encoding!=null)
			string += "<encoding>" + encoding + "</encoding>";
		string += "<data>" + data + "</data></multipart>";

		return string;
	}
	
}
