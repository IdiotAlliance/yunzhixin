package com.dt.cloudmsg.datasource;

import android.content.Context;
import android.database.Cursor;

import com.dt.cloudmsg.dao.DeviceDAO;
import com.dt.cloudmsg.model.Device;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/***
 * 提供设备信息的data source，加载时自动忽略本机信息
 */
public class ServerSource extends AbstractDataSource<Device>{

    private static List<Device> devices = new LinkedList<Device>();
    private String account;

	public ServerSource(Context context, String account) {
		super(context, DeviceDAO.CREATE_SQL);
        // 加载设备信息
        this.account = account;
        load();
        this.notifyChange();
	}

	@Override
	public int size() {
		return devices.size();
	}

	@Override
	public Device get(int index) {
        if(index < 0 || index > size())
            return null;
		return devices.get(index);
	}

    @Override
    public void onChange() {
        load();
        notifyChange();
    }

    @Override
    public void onUpdate(Device device) {
        int index = devices.indexOf(device);
        if(index >= 0){
            devices.add(index, device);
            devices.remove(index + 1);
            this.notifyUpdate(device);
        }
    }

    @Override
    public void onAdd(Device device) {
        devices.add(device);
        Collections.sort(devices);
        this.notifyAdd(device);
    }

    @Override
    public void onDelete(Device device) {
        devices.remove(device);
        this.notifyDelete(device);
    }

    @Override
    protected void load() {
        devices.clear();
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + DeviceDAO.TABLE_NAME + " WHERE _account=?",
                new String[]{account});
        while(cursor.moveToNext()){
            Device device = new Device();
            device.set_id(cursor.getInt(cursor.getColumnIndex(DeviceDAO._ID)));
            device.setName(cursor.getString(cursor.getColumnIndex(DeviceDAO._NAME)));
            device.setImei(cursor.getString(cursor.getColumnIndex(DeviceDAO._IMEI)));
            device.setNumber(cursor.getString(cursor.getColumnIndex(DeviceDAO._NUMBER)));
            device.setStatus(cursor.getInt(cursor.getColumnIndex(DeviceDAO._STATUS)));
            device.setPushOn(cursor.getInt(cursor.getColumnIndex(DeviceDAO._PUSHON)) == 1);
            device.setServerOn(cursor.getInt(cursor.getColumnIndex(DeviceDAO._SERVERON)) == 1);
            device.setBound(cursor.getInt(cursor.getColumnIndex(DeviceDAO._BOUND)) == 1);
            device.setAccount(cursor.getString(cursor.getColumnIndex(DeviceDAO._ACCOUNT)));
            devices.add(device);
        }
        cursor.close();
    }


}
