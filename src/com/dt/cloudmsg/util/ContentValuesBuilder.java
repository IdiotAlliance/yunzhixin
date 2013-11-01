package com.dt.cloudmsg.util;

import android.content.ContentValues;

public class ContentValuesBuilder {
	
	private ContentValues cv;
	
	private ContentValuesBuilder(){
		this.cv = new ContentValues();
	}
	
	public static ContentValuesBuilder createBuilder(){
		return new ContentValuesBuilder();
	}
	
	public ContentValuesBuilder appendString(String key, String value){
		this.cv.put(key, value);
		return this;
	}
	
	public ContentValuesBuilder appendShort(String key, Short value){
		this.cv.put(key, value);
		return this;
	}
	
	public ContentValuesBuilder appendInteger(String key, Integer value){
		this.cv.put(key, value);
		return this;
	}
	
	public ContentValuesBuilder appendLong(String key, Long value){
		this.cv.put(key, value);
		return this;
	}

	public ContentValuesBuilder appendBoolean(String key, Boolean value){
		this.cv.put(key, value);
		return this;
	}
	
	public ContentValuesBuilder appendFloat(String key, Float value){
		this.cv.put(key, value);
		return this;
	}
	
	public ContentValuesBuilder appendDouble(String key, Double value){
		this.cv.put(key, value);
		return this;
	}
	
	public ContentValuesBuilder appendByte(String key, byte[] value){
		this.cv.put(key, value);
		return this;
	}
	
	public ContentValues create(){
		return this.cv;
	}
	
}
