package com.dt.cloudmsg.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.dt.cloudmsg.beans.MessageBean;
import com.dt.cloudmsg.dao.MsgListDAO;
import com.dt.cloudmsg.model.ChatMsgEntity;
import com.dt.cloudmsg.model.MsgListEntity;
import com.dt.cloudmsg.util.ContentValuesBuilder;
import com.dt.cloudmsg.util.StringUtil;

public class MsgListSource extends AbstractDataSource<MsgListEntity>{


	private static List<MsgListEntity> msgList = new LinkedList<MsgListEntity>();
	private static List<MsgListEntity> tempList = new LinkedList<MsgListEntity>();
    private String serverNum;
    private String account;
    private String filtstr;
    private boolean filter = false;
    
	public MsgListSource(Context context, String account, String serverNum) {
		super(context, MsgListDAO.CREATE_SQL);
		msgList = new ArrayList<MsgListEntity>();

        this.account = account;
        if(serverNum != null){
            this.serverNum = serverNum;
            load();
            this.notifyChange();
        }
    }

    public void setServer(String serverNum){
        this.serverNum = serverNum;
        load();
        this.notifyChange();
    }

	@Override
	public int size() {
		if(filter)
			return tempList.size();
		return msgList.size();
	}

	@Override
	public MsgListEntity get(int index) {
		if(filter)
			return tempList.get(index);
		return msgList.get(index);
	}

	@Override
	public void registerDataChangeListener(XDataChangeListener<MsgListEntity> listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(XDataChangeListener<MsgListEntity> listener) {
		this.listeners.remove(listener);
	}

    @Override
    public void onChange() {
        load();
        if(filter){
            filter(filtstr);
        }
        this.notifyChange();
    }

    @Override
    public void onUpdate(MsgListEntity entity) {
        int index = msgList.indexOf(entity);
        if(index >= 0){
            msgList.remove(index);
        }
        msgList.add(0, entity);
        Collections.sort(msgList);
        this.notifyUpdate(entity);
    }

    @Override
    public void onAdd(MsgListEntity entity) {
        msgList.add(entity);
        Collections.sort(msgList);
        this.notifyAdd(entity);
    }

    @Override
    public void onDelete(MsgListEntity entity) {
        msgList.remove(entity);
        this.notifyDelete(entity);
    }

    @Override
    protected void load() {
        msgList.clear();
        Cursor cursor = this.db.rawQuery("select * from " + MsgListDAO.TABLE_NAME +
                " where " + MsgListDAO._ACCOUNT + " =? and " + MsgListDAO._SOURCE + "=? order by _stime desc",
                new String[]{account, serverNum});
        while (cursor.moveToNext()){
            MsgListEntity entity = new MsgListEntity();
            entity.setAccount(cursor.getString(cursor.getColumnIndex(MsgListDAO._ACCOUNT)));
            entity.setComname(cursor.getString(cursor.getColumnIndex(MsgListDAO._COMNAME)));
            entity.setComNumber(cursor.getString(cursor.getColumnIndex(MsgListDAO._NUMBER)));
            entity.setSource(cursor.getString(cursor.getColumnIndex(MsgListDAO._SOURCE)));
            entity.setId(cursor.getInt(cursor.getColumnIndex(MsgListDAO._ID)));
            entity.setType(cursor.getInt(cursor.getColumnIndex(MsgListDAO._TYPE)));
            entity.setLastMsg(cursor.getString(cursor.getColumnIndex(MsgListDAO._CONTENT)));
            entity.setNewCall(cursor.getInt(cursor.getColumnIndex(MsgListDAO._NUM_CALL)));
            entity.setMsgCount(cursor.getInt(cursor.getColumnIndex(MsgListDAO._NUM_MSG)));
            entity.setStime(cursor.getLong(cursor.getColumnIndex(MsgListDAO._STIME)));
            entity.setRtime(cursor.getLong(cursor.getColumnIndex(MsgListDAO._RTIME)));
            entity.setMsgCount(cursor.getInt(cursor.getColumnIndex(MsgListDAO._MSG_COUNT)));
            entity.setCount(cursor.getInt(cursor.getColumnIndex(MsgListDAO._COUNT)));
            msgList.add(entity);
        }
        cursor.close();
    }
    
    public void filter(String str){
    	filter = true;
        this.filtstr = str;
    	tempList.clear();
    	if(StringUtil.isNumber(str)){
    		for(MsgListEntity entity: msgList){
    			if(entity.getComNumber().contains(str)){
    				tempList.add(entity);
    			}
    		}
    	}
    	else{
    		for(MsgListEntity entity: msgList){
    			if(entity.getComname() != null && entity.getComname().contains(str)){
    				tempList.add(entity);
    			}
    			if(tempList.isEmpty()){
    				String pinyin   = StringUtil.getPinyin(str);
    				String shouzimu = StringUtil.getShouZiMu(str);
    				for(MsgListEntity entity1: msgList){
    					if(entity1.getQuanPin().contains(pinyin) || 
    							entity1.getShouZiMu().contains(shouzimu)){
    							tempList.add(entity1);
    					}
    				}
    			}
    		}
    	}
    	this.notifyChange();
    }
    
    public void cancelFilter(){
    	filter = false;
    	tempList.clear();
    	this.notifyChange();
    }
}
