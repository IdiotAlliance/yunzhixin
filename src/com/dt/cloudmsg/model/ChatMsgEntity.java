package com.dt.cloudmsg.model;

import com.dt.cloudmsg.beans.MessageBean;

public class ChatMsgEntity{

    private long _id;

    private int msgId;

    private String account;

	private String comNumber;

	private String toNumber;

	private long rawtime;

    private long servertime;

	private String rawMsg;

	private int type; // 消息的类型

    private int status; // 消息的状态

	private boolean isComMsg = true;// 是否是对方的消息

    private MessageBean.BaseBody body;

    public static final int STATUS_OK = 0x00;
    public static final int STATUS_SENDING = 0x01; // 发送中
    public static final int STATUS_FAILED  = 0x02; // 发送失败

	public ChatMsgEntity() {
		super();
	}

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getComNumber() {
        return comNumber;
    }

    public void setComNumber(String comNumber) {
        this.comNumber = comNumber;
    }

    public String getToNumber() {
        return toNumber;
    }

    public void setToNumber(String toNumber) {
        this.toNumber = toNumber;
    }

    public long getRawtime() {
        return rawtime;
    }

    public void setRawtime(long rawtime) {
        this.rawtime = rawtime;
    }

    public long getServertime() {
        return servertime;
    }

    public void setServertime(long servertime) {
        this.servertime = servertime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isComMsg() {
        return isComMsg;
    }

    public void setComMsg(boolean comMsg) {
        isComMsg = comMsg;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public MessageBean.BaseBody getBody() {
        return body;
    }

    public void setBody(MessageBean.BaseBody body) {
        this.body = body;
    }

    public String getRawMsg() {
        return rawMsg;
    }

    public void setRawMsg(String rawMsg) {
        this.rawMsg = rawMsg;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object object){
        if(!(object instanceof ChatMsgEntity))
            return false;
        if(this == object)
            return true;
        return _id == ((ChatMsgEntity) object).get_id();
    }
}
