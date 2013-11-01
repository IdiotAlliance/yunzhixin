package com.dt.cloudmsg.model;

import com.dt.cloudmsg.util.StringUtil;

import java.util.Date;

public class MsgListEntity implements Comparable<MsgListEntity>{

	private long id;
	private int type;
    private String account;
    private String comname;
    private String source;
    private String comNumber;
    private int msgCount;
    private long rtime;
    private long stime;
    private String lastMsg;
    private int newCall;
    private int count;
    private String quanPin; // 名称全拼
    private String shouZiMu;// 首字母

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getComNumber() {
        return comNumber;
    }

    public void setComNumber(String comNumber) {
        this.comNumber = comNumber;
    }

    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public int getNewCall() {
        return newCall;
    }

    public void setNewCall(int newCall) {
        this.newCall = newCall;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public MsgListEntity() {
    	super();
    }

    public long getRtime() {
        return rtime;
    }

    public void setRtime(long rtime) {
        this.rtime = rtime;
    }

    public long getStime() {
        return stime;
    }

    public void setStime(long stime) {
        this.stime = stime;
    }

    public String getComname() {
        return comname;
    }

    public void setComname(String comname) {
        this.comname = comname;
        String qp  = StringUtil.getPinyin(comname);
        this.quanPin = qp == null ? "" : qp;
        String szm = StringUtil.getShouZiMu(comname);
        this.shouZiMu = szm == null ? "" : szm;
    }

    public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getQuanPin() {
		return quanPin;
	}

	public void setQuanPin(String quanPin) {
		this.quanPin = quanPin;
	}

	public String getShouZiMu() {
		return shouZiMu;
	}

	public void setShouZiMu(String shouZiMu) {
		this.shouZiMu = shouZiMu;
	}

	@Override
    public boolean equals(Object o){
        if(!(o instanceof MsgListEntity)){
            return false;
        }
        if(this == o)
            return  true;

        MsgListEntity entity = (MsgListEntity) o;
        return entity.getAccount().equals(account) && 
        		entity.getSource().equals(source) && 
        		entity.getComNumber().equals(comNumber);
    }

    @Override
    public int hashCode(){
        return (source + comNumber).hashCode();
    }

    @Override
    public int compareTo(MsgListEntity entity) {
        if(stime < entity.getStime())
            return 1;
        if(stime == entity.getStime())
            return 0;
        return -1;
    }
}
