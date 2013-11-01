package com.dt.cloudmsg.beans;

import java.util.ArrayList;
import java.util.List;

import com.dt.cloudmsg.model.Device;
import com.google.gson.annotations.Expose;

public class Devices extends BaseBean{
	
	@Expose private List<Device> devices;
	
	public Devices(){
		this.devices = new ArrayList<Device>();
	}

	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}
	
	public void addDevice(Device device){
		this.devices.add(device);
	}
}
