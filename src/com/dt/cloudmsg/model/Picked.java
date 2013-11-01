package com.dt.cloudmsg.model;

public class Picked {
	private int index;
	private String name;
	private String number;
	public Picked(int index,String name,String number){
		this.index=index;
		this.name=name;
		this.number=number;
	}
	public int getIndex(){
		return index;
	}
	public String getName(){
		return name;
	}
	public String getNumber(){
		return number;
	}
}
