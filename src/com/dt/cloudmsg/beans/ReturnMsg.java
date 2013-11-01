package com.dt.cloudmsg.beans;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ReturnMsg extends BaseBean{

    @Expose private int fail;
    @Expose private String msg = null;
    @Expose private int msgId = 0;
    @Expose private int hbMsg = 0; // 心跳包携带的状态码
    @Expose List<SendReturnMsg> srm = null;

    public static final int SUCCESS = 0x00, // 成功
            FAIL = 0x01,    // 失败
            TOKEN_ERROR = 0x02; // token无效

    public static final int HB_INSUFFICIENT_BALANCE = 0x1001, //账户即将过期
            HB_ACCOUNT_EXPIRED      = 0x1002, //账户余额不足
            HB_ACCOUNT_LOCKED       = 0x1003, //账户被锁定
            HB_ACCOUNT_BANNED       = 0x1004; //账户被禁用

    public ReturnMsg(){
        this(SUCCESS);
    }

    public ReturnMsg(int fail){
        this(fail, null);
    }

    public ReturnMsg(int fail, int msgId){
        this(fail, msgId, null);
    }

    public ReturnMsg(int fail, String msg){
        this(fail, 0, msg);
    }

    public ReturnMsg(int fail, int msgId, String msg){
        if(fail != SUCCESS && fail != FAIL && fail != TOKEN_ERROR)
            throw new IllegalArgumentException();

        this.fail = fail;
        this.msgId = msgId;
        this.msg = msg;
    }

    public boolean fail(){
        return this.fail == FAIL;
    }

    public int getFail() {
        return fail;
    }

    public void setFail(int fail) {
        this.fail = fail;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<SendReturnMsg> getSrm() {
        return srm;
    }

    public void setSrm(List<SendReturnMsg> srm) {
        this.srm = srm;
    }

    public void addSendReturnMsg(SendReturnMsg srm){
        this.srm.add(srm);
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getHbMsg() {
        return hbMsg;
    }

    public void setHbMsg(int hbMsg) {
        this.hbMsg = hbMsg;
    }


}
