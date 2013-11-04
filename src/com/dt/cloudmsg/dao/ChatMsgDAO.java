package com.dt.cloudmsg.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.dt.cloudmsg.beans.MessageBean;
import com.dt.cloudmsg.datasource.AbstractDataSource;
import com.dt.cloudmsg.model.ChatMsgEntity;
import com.dt.cloudmsg.util.ContentValuesBuilder;
import com.dt.cloudmsg.util.JsonUtil;

/**
 * Created by lvxiang on 13-10-8.
 */
public class ChatMsgDAO extends AbstractDAO<ChatMsgEntity>{

    public static final String TABLE_NAME = "chatmsgs";
    public static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
            " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            " _msgId INTEGER," +
            " _account varchar(16) NOT NULL," +
            " _status int(4) DEFAULT(0)," +
            " _from varchar(16) NOT NULL," +
            " _to varchar(16) NOT NULL," +
            " _type INTEGER NOT NULL," +
            " _stime LONG NOT NULL," +
            " _rtime LONG NOT NULL," +
            " _body text," +
            " _come int(2) DEFAULT(1)" +
            " );";
    public static final String _ID = "_id",
                               _MSGID = "_msgId",
                               _ACCOUNT = "_account",
                               _STATUS = "_status",
                               _FROM = "_from",
                               _TO = "_to",
                               _TYPE = "_type",
                               _STIME = "_stime",
                               _RTIME = "_rtime",
                               _BODY = "_body",
                               _COME = "_come";

    public ChatMsgDAO(Context context) {
        super(context, CREATE_SQL);
    }

    @Override
    public long add(ChatMsgEntity entity) {
        ContentValues cv = getCV(entity);
        long id = this.db.insert(TABLE_NAME, null, cv);
        this.notifyAdd(entity);
        return id;
    }

    @Override
    public void update(ChatMsgEntity chatMsgEntity) {
        ContentValues cv = getCV(chatMsgEntity);
        this.db.update(TABLE_NAME, cv, "_id=?", new String[]{chatMsgEntity.get_id() + ""});
        this.notifyUpdate(chatMsgEntity);
    }

    @Override
    public void delete(ChatMsgEntity chatMsgEntity) {
        this.deleteById(chatMsgEntity.get_id());
        this.notifyDelete(chatMsgEntity);
    }

    public void delete(String account, String source, String target){
        this.db.delete(TABLE_NAME,
                       "_account=? and ((_from=? and _to=?) or (_from=? and _to=?))",
                       new String[]{account, source, target, target, source});
        this.nofiyChange();
    }

    @Override
    public void deleteById(long id) {
        ChatMsgEntity entity = getById(id);
        this.db.delete(TABLE_NAME, "_id=?", new String[]{id + ""});
        this.notifyDelete(entity);
    }

    @Override
    public ChatMsgEntity getById(long id) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=?", new String[]{id + ""});
        ChatMsgEntity entity = null;
        if(cursor.moveToNext()){
            entity = new ChatMsgEntity();
            entity.setAccount(cursor.getString(cursor.getColumnIndex("_account")));
            entity.setStatus(cursor.getInt(cursor.getColumnIndex("_status")));
            entity.setComMsg(cursor.getInt(cursor.getColumnIndex("_come")) == 1);
            entity.setRawMsg(cursor.getString(cursor.getColumnIndex("_body")));
            entity.setType(cursor.getInt(cursor.getColumnIndex("_type")));
            entity.setToNumber(cursor.getString(cursor.getColumnIndex("_to")));
            entity.setComNumber(cursor.getString(cursor.getColumnIndex("_from")));
            entity.setRawtime(cursor.getLong(cursor.getColumnIndex("_rtime")));
            entity.setServertime(cursor.getLong(cursor.getColumnIndex("_stime")));
            entity.setMsgId(cursor.getInt(cursor.getColumnIndex("_msgId")));
            entity.setBody(JsonUtil.fromJson(entity.getRawMsg(), MessageBean.BaseBody.class));
            entity.set_id(id);
        }
        cursor.close();
        return entity;
    }

    /****
     * 获取指定账户下指定服务器与目标号码最近的一次消息内容
     * @param account
     * @param source
     * @param target
     * @return
     */
    public ChatMsgEntity getLatest(String account, String source, String target){
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _account=? and " +
                                         "((_from=? and _to=?) or (_from=? and _to=?)) " +
                                         "ORDER BY _stime DESC LIMIT 1", new String[]{account, source, target, target, source});
        ChatMsgEntity entity = null;
        if(cursor.moveToNext()){
            entity = new ChatMsgEntity();
            entity.setAccount(cursor.getString(cursor.getColumnIndex("_account")));
            entity.setStatus(cursor.getInt(cursor.getColumnIndex("_status")));
            entity.setComMsg(cursor.getInt(cursor.getColumnIndex("_come")) == 1);
            entity.setRawMsg(cursor.getString(cursor.getColumnIndex("_body")));
            entity.setType(cursor.getInt(cursor.getColumnIndex("_type")));
            entity.setToNumber(cursor.getString(cursor.getColumnIndex("_to")));
            entity.setComNumber(cursor.getString(cursor.getColumnIndex("_from")));
            entity.setRawtime(cursor.getLong(cursor.getColumnIndex("_rtime")));
            entity.setServertime(cursor.getLong(cursor.getColumnIndex("_stime")));
            entity.setMsgId(cursor.getInt(cursor.getColumnIndex("_msgId")));
            entity.setBody(JsonUtil.fromJson(entity.getRawMsg(), MessageBean.BaseBody.class));
            entity.set_id(cursor.getLong(cursor.getColumnIndex(_ID)));
        }
        cursor.close();
        return entity;
    }

    @Override
    public void addOrUpdate(ChatMsgEntity chatMsgEntity) {
        if(exists(chatMsgEntity.get_id()))
            this.add(chatMsgEntity);
        else this.update(chatMsgEntity);
    }

    @Override
    public boolean exists(long id) {
        Cursor cursor = this.db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE _id=?", new String[]{id + ""});
        boolean result = cursor.moveToNext();
        cursor.close();
        return result;
    }

    private ContentValues getCV(ChatMsgEntity entity){
        ContentValues cv = ContentValuesBuilder.createBuilder()
                .appendString("_account", entity.getAccount())
                .appendInteger("_status", entity.getStatus())
                .appendInteger("_msgId", entity.getMsgId())
                .appendString("_from", entity.getComNumber())
                .appendString("_to", entity.getToNumber())
                .appendInteger("_type", entity.getType())
                .appendLong("_stime", entity.getServertime())
                .appendLong("_rtime", entity.getRawtime())
                .appendString("_body", entity.getRawMsg())
                .appendBoolean("_come", entity.isComMsg())
                .create();
        return cv;
    }

    public long getLatestServerTime(){
        Cursor cursor = this.db.rawQuery("SELECT _stime FROM " + TABLE_NAME +
                                         " WHERE _stime NOT IN(" +
                                         "SELECT M1._stime FROM chatmsgs as M1, chatmsgs as M2 WHERE " +
                                         " M1._stime < M2._stime)", null);
        long time = 0;
        if(cursor.moveToNext()){
            time = cursor.getLong(cursor.getColumnIndex(_STIME));
        }else
            time = System.currentTimeMillis();
        cursor.close();
        return time;
    }

    public void setFail(long id){
        ContentValues cv = ContentValuesBuilder.createBuilder()
                            .appendInteger(_STATUS, ChatMsgEntity.STATUS_FAILED)
                            .create();
        this.db.update(TABLE_NAME, cv, "_id=?", new String[]{id + ""});
        nofiyChange();
    }

    public void setSuccess(long id){
        ContentValues cv = ContentValuesBuilder.createBuilder()
                .appendInteger(_STATUS, ChatMsgEntity.STATUS_OK)
                .create();
        this.db.update(TABLE_NAME, cv, "_id=?", new String[]{id + ""});
        nofiyChange();
    }

}
