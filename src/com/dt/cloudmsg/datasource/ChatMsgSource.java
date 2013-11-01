package com.dt.cloudmsg.datasource;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.dt.cloudmsg.beans.MessageBean;
import com.dt.cloudmsg.dao.ChatMsgDAO;
import com.dt.cloudmsg.model.ChatMsgEntity;
import com.dt.cloudmsg.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;


public class ChatMsgSource extends AbstractDataSource<ChatMsgEntity>{

    private String account; // 当前使用的账户
    private String target; // 对话的联系人号码
    private String source; // 对话来源的设备号码

    private List<ChatMsgEntity> msgList = new ArrayList<ChatMsgEntity>();

	public ChatMsgSource(Context context, String account, String source, String target) {
		super(context, ChatMsgDAO.CREATE_SQL);

        if(account != null && source != null && target != null){
            this.account = account;
            this.source = source;
            this.target = target;

            // 初始化数据
            load();
            this.notifyChange();
        }
	}

    public void setParams(String account, String source, String target){
        if(account != null && source != null && target != null){
            this.account = account;
            this.source = source;
            this.target = target;
            Log.d("account: ", account);
            Log.d("target: ", this.target);
            // 初始化数据
            load();
            this.notifyChange();
        }
    }

	@Override
	public int size() {
		return msgList.size();
	}

	@Override
	public ChatMsgEntity get(int index) {
        if(index < 0 || index >= size())
            return null;
		return msgList.get(index);
	}

	@Override
	public void registerDataChangeListener(XDataChangeListener<ChatMsgEntity> listener) {
	    super.registerDataChangeListener(listener);
	}

	@Override
	public void removeListener(XDataChangeListener<ChatMsgEntity> listener) {
		super.removeListener(listener);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
	}

    @Override
    public void onChange() {
        load();
        this.notifyChange();
    }

    @Override
    public void onUpdate(ChatMsgEntity entity) {
        if(entity != null && concerned(entity)){
            int index = msgList.indexOf(entity);
            if(index >= 0){
                msgList.add(index, entity);
                msgList.remove(index + 1);
                this.notifyUpdate(entity);
            }
        }
    }

    @Override
    public void onAdd(ChatMsgEntity entity) {
        Log.d("new msg", entity.getRawMsg());
        if(entity != null && concerned(entity)){
            Log.d("new msg", entity.getRawMsg());
            msgList.add(entity);
            this.notifyAdd(entity);
        }
    }

    @Override
    public void onDelete(ChatMsgEntity entity) {
        if(entity != null && concerned(entity)){
            msgList.remove(entity);
            this.notifyDelete(entity);
        }
    }

    protected void load(){
        if(account != null && source != null && target != null){
            msgList.clear();
            Cursor cursor = this.db.rawQuery(
                    "SELECT * FROM " + ChatMsgDAO.TABLE_NAME +
                            " WHERE " + ChatMsgDAO._ACCOUNT + "=? AND " +
                            "((" + ChatMsgDAO._TO + "=? AND " + ChatMsgDAO._FROM + "=?)" + " OR (" +
                                   ChatMsgDAO._TO + "=? AND " + ChatMsgDAO._FROM + "=?))" +
                            " order by " + ChatMsgDAO._STIME + " asc"
                    , new String[]{account, target, source, source, target});
            while(cursor.moveToNext()){
                ChatMsgEntity entity = new ChatMsgEntity();
                entity.set_id(cursor.getInt(cursor.getColumnIndex(ChatMsgDAO._ID)));
                entity.setStatus(cursor.getInt(cursor.getColumnIndex(ChatMsgDAO._STATUS)));
                entity.setMsgId(cursor.getInt(cursor.getColumnIndex(ChatMsgDAO._MSGID)));
                entity.setRawtime(cursor.getLong(cursor.getColumnIndex(ChatMsgDAO._RTIME)));
                entity.setServertime(cursor.getLong(cursor.getColumnIndex(ChatMsgDAO._STIME)));
                entity.setAccount(cursor.getString(cursor.getColumnIndex(ChatMsgDAO._ACCOUNT)));
                entity.setType(cursor.getInt(cursor.getColumnIndex(ChatMsgDAO._TYPE)));
                entity.setComMsg(cursor.getInt(cursor.getColumnIndex(ChatMsgDAO._COME)) == 1);
                entity.setComNumber(cursor.getString(cursor.getColumnIndex(ChatMsgDAO._FROM)));
                entity.setToNumber(cursor.getString(cursor.getColumnIndex(ChatMsgDAO._TO)));
                entity.setBody(JsonUtil.fromJson(cursor.getString(cursor.getColumnIndex(ChatMsgDAO._BODY)),
                        MessageBean.BaseBody.class));
                Log.d("msg loaded", entity.getBody().getMsg());
                msgList.add(entity);
            }
            cursor.close();
        }
    }

    private boolean concerned(ChatMsgEntity entity){
        return entity.getAccount().equals(account) &&
                ((entity.getComNumber().equals(target) && entity.getToNumber().equals(source)) ||
                 (entity.getToNumber().equals(target) && entity.getComNumber().equals(source)));
    }
}
