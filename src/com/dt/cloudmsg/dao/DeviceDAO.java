package com.dt.cloudmsg.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.dt.cloudmsg.model.Device;
import com.dt.cloudmsg.util.ContentValuesBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lvxiang on 13-10-11.
 */
public class DeviceDAO extends AbstractDAO<Device>{

    public static final String TABLE_NAME = "devices";
    public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
            " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            " _name varchar(32)," +
            " _number varchar(16)," +
            " _imei varchar(64) NOT NULL," +
            " _status int(4) DEFAULT(0)," +
            " _serveron int(2) DEFAULT(0)," +
            " _pushon int(2) DEFAULT(0)," +
            " _bound int(2) DEFAULT(0)," +
            " _account varchar(64) NOT NULL" +
            " )";

    public static final String _ID   = "_id",
                               _NAME = "_name",
                               _NUMBER = "_number",
                               _IMEI   = "_imei",
                               _STATUS = "_status",
                               _SERVERON = "_serveron",
                               _PUSHON   = "_pushon",
                               _BOUND = "_bound",
                               _ACCOUNT = "_account";

    public DeviceDAO(Context context) {
        super(context, CREATE_SQL);
    }

    @Override
    public long add(Device device) {
        ContentValues cv = getCV(device);
        long id = this.db.insert(TABLE_NAME, null, cv);
        this.notifyAdd(device);
        return id;
    }

    @Override
    public void update(Device device) {
        ContentValues cv = getCV(device);
        this.db.update(TABLE_NAME, cv, "_account=? and _imei=?",
                new String[]{device.getAccount(), device.getImei()});
        this.notifyUpdate(device);
    }

    @Override
    public void delete(Device device) {
        this.db.delete(TABLE_NAME, "_account=? and _imei=?",
                new String[]{device.getAccount(), device.getImei()});
        this.notifyDelete(device);
    }

    @Override
    public void deleteById(long id) {
        Device device = getById(id);
        if(device != null)
            this.delete(device);
    }

    @Override
    public Device getById(long id) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=?", new String[]{id + ""});
        Device device = null;
        if(cursor.moveToNext()){
            device = new Device();
            device.set_id(cursor.getLong(cursor.getColumnIndex(_ID)));
            device.setImei(cursor.getString(cursor.getColumnIndex(_IMEI)));
            device.setName(cursor.getString(cursor.getColumnIndex(_NAME)));
            device.setNumber(cursor.getString(cursor.getColumnIndex(_NUMBER)));
            device.setServerOn(cursor.getInt(cursor.getColumnIndex(_SERVERON)) == 1);
            device.setPushOn(cursor.getInt(cursor.getColumnIndex(_PUSHON)) == 1);
            device.setBound(cursor.getInt(cursor.getColumnIndex(_BOUND)) == 1);
            device.setAccount(cursor.getString(cursor.getColumnIndex(_ACCOUNT)));
        }
        cursor.close();
        return device;
    }

    public List<Device> getDeivces(String account){
        List<Device> devices = new ArrayList<Device>();
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _account=?", new String[]{account});
        while(cursor.moveToNext()){
            Device device = new Device();
            device = new Device();
            device.set_id(cursor.getInt(cursor.getColumnIndex(_ID)));
            device.setImei(cursor.getString(cursor.getColumnIndex(_IMEI)));
            device.setName(cursor.getString(cursor.getColumnIndex(_NAME)));
            device.setNumber(cursor.getString(cursor.getColumnIndex(_NUMBER)));
            device.setServerOn(cursor.getInt(cursor.getColumnIndex(_SERVERON)) == 1);
            device.setPushOn(cursor.getInt(cursor.getColumnIndex(_PUSHON)) == 1);
            device.setBound(cursor.getInt(cursor.getColumnIndex(_BOUND)) == 1);
            device.setAccount(cursor.getString(cursor.getColumnIndex(_ACCOUNT)));
            devices.add(device);
        }
        return devices;
    }

    @Override
    public void addOrUpdate(Device device) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _account=? and _imei=?",
                new String[]{device.getAccount(), device.getImei()});
        if(cursor.moveToNext()){
            Log.d("found device:", device.getImei());
            this.update(device);
        }
        else {
            Log.d("device not found:", device.getImei());
            this.add(device);
        }
        cursor.close();
    }

    public void addExclusively(List<Device> devices){
        // 清空数据库中的设备信息
        for(Device device: devices){
            addOrUpdate(device);
        }
    }

    @Override
    public boolean exists(long id) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=?", new String[]{id+""});
        boolean result = cursor.moveToNext();
        cursor.close();
        return result;
    }

    public boolean isBound(String account, String imei){
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _account=? and _imei=?",
                                    new String[]{account, imei});
        boolean result = cursor.moveToNext();
        cursor.close();
        return result;
    }

    private ContentValues getCV(Device device){
        ContentValues cv = ContentValuesBuilder.createBuilder()
                                .appendInteger(_STATUS, device.getStatus())
                                .appendBoolean(_PUSHON, device.isPushOn())
                                .appendBoolean(_SERVERON, device.isServerOn())
                                .appendBoolean(_BOUND, device.isBound())
                                .appendString(_IMEI, device.getImei())
                                .appendString(_NUMBER, device.getNumber())
                                .appendString(_NAME, device.getName())
                                .appendString(_ACCOUNT, device.getAccount())
                                .create();
        return cv;
    }

    public String getImei(String account, String number){
        Cursor cursor = this.db.rawQuery("SELECT _imei FROM " + TABLE_NAME + " WHERE _account=? and _number=?",
                                    new String[]{account, number});
        String imei = null;
        if(cursor.moveToNext()){
            imei = cursor.getString(cursor.getColumnIndex(_IMEI));
        }
        cursor.close();
        return imei;
    }

}
