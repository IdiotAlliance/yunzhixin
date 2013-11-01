package com.dt.cloudmsg.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.dt.cloudmsg.model.MsgListEntity;
import com.dt.cloudmsg.util.ContentValuesBuilder;

/**
 * Created by lvxiang on 13-10-11.
 */
public class MsgListDAO extends AbstractDAO<MsgListEntity> {

    public static final String TABLE_NAME = "msg_list_table";
    public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
            " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            " _type INTEGER NOT NULL," +
            " _account VARCHAR(64) NOT NULL," + // entity属于的帐号
            " _number VARCHAR(20) NOT NULL," + // 消息来源的号码
            " _source VARCHAR(20) NOT NULL," + // 来自的服务器号码
            " _comname TEXT," +
            " _num_msg INTEGER NOT NULL DEFAULT(0)," +
            " _num_call INTEGER NOT NULL DEFAULT(0)," +
            " _msg_count INTEGER NOT NULL DEFAULT(0)," +
            " _content TEXT," +
            " _stime long," +
            " _rtime long," +
            " _count int DEFAULT(0)" +
            " );";
    public static final String _ID = "_id",
                               _TYPE = "_type",
                               _ACCOUNT = "_account",
                               _NUMBER = "_number",
                               _SOURCE = "_source",
                               _COMNAME = "_comname",
                               _NUM_MSG = "_num_msg",
                               _NUM_CALL = "_num_call",
                               _MSG_COUNT = "_msg_count",
                               _CONTENT = "_content",
                               _STIME = "_stime",
                               _RTIME = "_rtime",
                               _COUNT = "_count";

    public MsgListDAO(Context context, String account) {
        super(context, CREATE_SQL);
        // this.account = account;
    }

    @Override
    public long add(MsgListEntity entity) {
        ContentValues cv = getCV(entity);
        long id = this.db.insert(TABLE_NAME, null, cv);
        this.notifyAdd(entity);
        return id;
    }

    @Override
    public void update(MsgListEntity entity) {
    	ContentValues cv = getCV(entity);
        db.update(TABLE_NAME, cv, _ID + "=?", new String[]{entity.getId() + ""});
        this.notifyUpdate(entity);
    }

    @Override
    public void delete(MsgListEntity entity) {
        this.deleteById(entity.getId());
    }

    @Override
    public void deleteById(long id) {
        MsgListEntity entity = getById(id);
        this.db.delete(TABLE_NAME, _ID + "=?", new String[]{id + ""});
        this.notifyDelete(entity);
    }

    public MsgListEntity getEntity(String account, String source, String comNum){
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _account=? and _source=? and _number=?",
                                         new String[]{account, source, comNum});
        MsgListEntity entity = null;
        if(cursor.moveToNext()){
            entity = getEntity(cursor);
        }
        return entity;
    }

    @Override
    public MsgListEntity getById(long id) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=?", new String[]{id + ""});
        MsgListEntity entity = null;
        if(cursor.moveToNext()){
            entity = getEntity(cursor);
        }
        cursor.close();
        return entity;
    }

    @Override
    public void addOrUpdate(MsgListEntity entity) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _account=? and _source=? and _number=?",
                                         new String[]{entity.getAccount(), entity.getSource(), entity.getComNumber()});
        if(cursor.moveToNext()){
            this.update(entity);
        }
        else
            this.add(entity);
    }

    public void setRead(String account, String source, String target){
        ContentValues cv = ContentValuesBuilder.createBuilder()
                                .appendInteger(_NUM_CALL, 0)
                                .appendInteger(_MSG_COUNT, 0)
                                .create();
        this.db.update(TABLE_NAME, cv,
                       "_account=? and _source=? and _number=?",
                       new String[]{account, source, target});
        this.nofiyChange();
    }

    @Override
    public boolean exists(long id) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=?", new String[]{id + ""});
        boolean result = false;
        if(cursor.moveToNext()){
            result = true;
        }
        cursor.close();
        return result;
    }

    private ContentValues getCV(MsgListEntity entity){
        ContentValues cv = ContentValuesBuilder.createBuilder()
                .appendString(_ACCOUNT, entity.getAccount())
                .appendInteger(_TYPE, entity.getType())
                .appendString(_NUMBER, entity.getComNumber())
                .appendString(_SOURCE, entity.getSource())
                .appendString(_COMNAME, entity.getComname())
                .appendInteger(_NUM_MSG, entity.getMsgCount())
                .appendInteger(_NUM_CALL, entity.getNewCall())
                .appendInteger(_MSG_COUNT, entity.getMsgCount())
                .appendString(_CONTENT, entity.getLastMsg())
                .appendLong(_STIME, entity.getStime())
                .appendLong(_RTIME, entity.getRtime())
                .appendInteger(_COUNT, entity.getCount())
                .create();
        return cv;
    }

    private MsgListEntity getEntity(Cursor cursor){
        MsgListEntity entity = new MsgListEntity();
        entity.setId(cursor.getLong(cursor.getColumnIndex(_ID)));
        entity.setAccount(cursor.getString(cursor.getColumnIndex(_ACCOUNT)));
        entity.setLastMsg(cursor.getString(cursor.getColumnIndex(_CONTENT)));
        entity.setNewCall(cursor.getInt(cursor.getColumnIndex(_NUM_CALL)));
        entity.setComNumber(cursor.getString(cursor.getColumnIndex(_NUMBER)));
        entity.setComname(cursor.getString(cursor.getColumnIndex(_COMNAME)));
        entity.setRtime(cursor.getLong(cursor.getColumnIndex(_RTIME)));
        entity.setId(cursor.getInt(cursor.getColumnIndex(_ID)));
        entity.setMsgCount(cursor.getInt(cursor.getColumnIndex(_MSG_COUNT)));
        entity.setSource(cursor.getString(cursor.getColumnIndex(_SOURCE)));
        entity.setStime(cursor.getLong(cursor.getColumnIndex(_STIME)));
        entity.setType(cursor.getInt(cursor.getColumnIndex(_TYPE)));
        entity.setCount(cursor.getInt(cursor.getColumnIndex(_COUNT)));
        return entity;
    }
}
