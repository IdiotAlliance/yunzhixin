package com.dt.cloudmsg.beans;

import com.google.gson.annotations.Expose;

public class Registration extends BaseBean{

	@Expose
	private String username;
	@Expose
	private String password;

	public Registration(String username, String password) {
		super();
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


}
