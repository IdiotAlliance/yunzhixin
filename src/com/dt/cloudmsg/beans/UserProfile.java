package com.dt.cloudmsg.beans;

import java.util.Date;

import com.google.gson.annotations.Expose;

public class UserProfile extends BaseBean{

	@Expose private String name;
	@Expose private String email;
	@Expose private String addr;
	@Expose private Date birth;
	@Expose private int gender;
	
	public static final int GENDER_MAIL   = 0x00;
	public static final int GENDER_FEMALE = 0x01;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public Date getBirth() {
		return birth;
	}
	public void setBirth(Date birth) {
		this.birth = birth;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
}
