package com.dt.cloudmsg.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;

import com.dt.cloudmsg.datasource.AbstractDataSource;
import com.dt.cloudmsg.datasource.XDataChangeListener;
import com.dt.cloudmsg.model.Account;
import com.dt.cloudmsg.util.ContentValuesBuilder;

import java.util.Date;

/**
 * Created by lvxiang on 13-9-26.
 */
public class AccountDAO extends AbstractDAO<Account>{

    private TelephonyManager tm = null;
    Account account = null;

    private static final String TABLE_NAME = "cmaccounts";
    private static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                                                " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                                " _username VARCHAR(20) NOT NULL, " +
                                                " _password VARCHAR(25) NOT NULL, " +
                                                " _status int(4) DEFAULT(0), " +
                                                " _privilege int(4) DEFAULT(0), " +
                                                " _expire LONG NOT NULL," +
                                                " _api_token VARCHAR(64) NOT NULL," +
                                                " _api_key VARCHAR(64) NOT NULL," +
                                                " _imei VARCHAR(64)" +
                                                " )";


    public AccountDAO(Context context) {
        super(context, CREATE_SQL);
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public long add(Account account) {
        ContentValues cv = getCV(account);
        this.account = account;
        long id = this.db.insert(TABLE_NAME, null, cv);
        this.notifyAdd(account);
        return id;
    }

    @Override
    public void update(Account account) {
        ContentValues cv = getCV(account);
        this.account = account;
        this.db.update(TABLE_NAME, cv, "_username=?", new String[]{account.getAccountName()});
        this.notifyUpdate(account);
    }

    @Override
    public void delete(Account account) {
        this.db.delete(TABLE_NAME, "_username=?", new String[]{account.getAccountName()});
        this.notifyDelete(account);
    }

    @Override
    public void deleteById(long id) {
        Account account = getById(id);
        if(account != null)
            this.notifyDelete(account);
    }

    @Override
    public Account getById(long id) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=?", new String[]{id+""});
        Account account = null;
        if(cursor.moveToNext()){
            account = new Account();
            account.setAccountName(cursor.getString(cursor.getColumnIndex("_username")));
            account.setExpire(new Date(cursor.getLong(cursor.getColumnIndex("_expire"))));
            account.setIMEI(cursor.getString(cursor.getColumnIndex("_imei")));
            account.setPassword(cursor.getString(cursor.getColumnIndex("_password")));
            account.setPrivilege(cursor.getInt(cursor.getColumnIndex("_privilege")));
            account.setStatus(cursor.getInt(cursor.getColumnIndex("_status")));
            account.setToken(cursor.getString(cursor.getColumnIndex("_api_token")));
            account.setKey(cursor.getString(cursor.getColumnIndex("_api_key")));
        }
        cursor.close();
        return account;
    }

    public Account getByAccount(String acc){
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _username=?", new String[]{acc});
        Account account = null;
        if(cursor.moveToNext()){
            account = new Account();
            account.setAccountName(cursor.getString(cursor.getColumnIndex("_username")));
            account.setExpire(new Date(cursor.getLong(cursor.getColumnIndex("_expire"))));
            account.setIMEI(cursor.getString(cursor.getColumnIndex("_imei")));
            account.setPassword(cursor.getString(cursor.getColumnIndex("_password")));
            account.setPrivilege(cursor.getInt(cursor.getColumnIndex("_privilege")));
            account.setStatus(cursor.getInt(cursor.getColumnIndex("_status")));
            account.setToken(cursor.getString(cursor.getColumnIndex("_api_token")));
            account.setKey(cursor.getString(cursor.getColumnIndex("_api_key")));
        }
        cursor.close();
        return account;
    }

    @Override
    public void addOrUpdate(Account account) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _username=?",
                new String[]{account.getAccountName()});
        boolean result = cursor.moveToNext();
        cursor.close();

        if(result)
            update(account);
        else
            add(account);
    }

    @Override
    public boolean exists(long id) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=?", new String[]{id+""});
        boolean result = cursor.moveToNext();
        cursor.close();
        return result;
    }

    private ContentValues getCV(Account account){
        ContentValues cv = ContentValuesBuilder.createBuilder()
                .appendString("_username", account.getAccountName())
                .appendString("_password", account.getPassword())
                .appendInteger("_status", account.getStatus())
                .appendInteger("_privilege", account.getPrivilege())
                .appendString("_imei", account.getIMEI())
                .appendLong("_expire", account.getExpire().getTime())
                .appendString("_api_token", account.getToken())
                .appendString("_api_key", account.getKey())
                .create();
        return cv;
    }

}
