package com.dt.cloudmsg.beans;

import com.google.gson.annotations.Expose;

/**
 * Created by lvxiang on 13-9-30.
 */
public class MessageBean extends BaseBean{


    @Expose private String from; // 源设备的imei，为空意味着是系统服务器发出的消息
    @Expose private String to; // 目标设备imei，为空意味着是广播消息
    @Expose private String account; // 消息来自的帐号
    @Expose private int msgId; // 消息id，由消息的发送者管理并创建
    @Expose private int type; // 消息的类型

    @Expose private long rawTime; // 消息发送的原始时间戳
    @Expose private long serverTime; // 经过服务器转发的中转时间戳
    @Expose private BaseBody body; // 消息体

    // 消息的相关常量
    public static transient final int TYPE_MSG          = 0x10000000;// 一般消息
    public static transient final int TYPE_MSG_SMS      = 0x10000001;// 短信消息
    public static transient final int TYPE_MSG_CAL_MISS = 0x10000002;//电话未接通
    public static transient final int TYPE_MSG_CAL_RECV = 0x10000003;//电话接通
    public static transient final int TYPE_MSG_MMG      = 0x10000004;// 彩信消息

    // 命令的相关常量
    public static transient final int TYPE_CMD           = 0x01000000;// 命令
    public static transient final int TYPE_CMD_SEND_SMS  = 0x01000001;// 发送短信命令
    public static transient final int TYPE_CMD_SYNC_DEV  = 0x01000002;// 同步设备状态
    public static transient final int TYPE_CMD_SEND_OK   = 0x01000003;// 消息发送成功，取消发送状态
    public static transient final int TYPE_CMD_SEND_FAIL = 0x01000004;// 消息发送失败，取消发送状态

    // 通知消息相关的常量
    public static transient final int TYPE_NOT          = 0x00100000;// 通知
    public static transient final int TYPE_NOT_NORM     = 0x00110000; // 一般性通知
    public static transient final int TYPE_NOT_NORM_DEVICE_OFFLINE = 0x00110001; // 设备正常离线通知

    public static transient final int TYPE_NOT_WARN     = 0x00101000; // 警告通知
    public static transient final int TYPE_NOT_WARN_DEVICE_OFFLINE = 0x00101001; // 设备异常离线通知
    public static transient final int TYPE_NOT_WARN_INS_BALANCE    = 0x00101002; // 账户即将过期通知
    public static transient final int TYPE_NOT_WARN_ACCOUNT_EXCEP  = 0x00101003; // 帐号异常通知

    public static transient final int TYPE_NOT_ERROR = 0x1001000; // 错误通知
    public static transient final int TYPE_NOT_ERROR_OUTOF_BALANCE = 0x00100101; // 账户余额不足错误
    public static transient final int TYPE_NOT_ERROR_FORCE_LOGOUT  = 0x00100102; // 强制下线通知
    public static transient final int TYPE_NOT_ERROR_FORCE_UPDATE  = 0x00100103; // 强制更新通知

    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }
    public int getMsgId() {
        return msgId;
    }
    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public long getRawTime() {
        return rawTime;
    }
    public void setRawTime(long rawTime) {
        this.rawTime = rawTime;
    }
    public long getServerTime() {
        return serverTime;
    }
    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public void createBaseBody(String from, String to, String msg){
        this.body = new BaseBody(from, to, msg, type);
    }

    public void createCodeBody(String from, String to, String msg, int code){
        this.body = new CodeMessage(from, to, msg, type, code);
    }

    public void createCodeAndUrlBody(String from, String to, String msg, int code, String url){
        this.body = new CodeAndUrlMessage(from, to, msg, type, code, url);
    }

    public BaseBody getMessageBody(){
        return body;
    }

    public class BaseBody extends BaseBean{
        @Expose protected String from;
        @Expose protected String to;
        @Expose protected String msg;

        public BaseBody(String from, String to, String msg, int type){
            this.from = from;
            this.to = to;
            this.msg = msg;
        }
        public String getFrom() {
            return from;
        }
        public void setFrom(String from) {
            this.from = from;
        }
        public String getTo() {
            return to;
        }
        public void setTo(String to) {
            this.to = to;
        }
        public String getMsg() {
            return msg;
        }
        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public final class CodeMessage extends BaseBody {
        @Expose private int code;

        public CodeMessage(String from, String to, String msg, int type) {
            super(from, to, msg, type);
        }

        public CodeMessage(String from, String to, String msg, int type, int code){
            super(from, to, msg, type);
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    public final class CodeAndUrlMessage extends BaseBody {
        @Expose private int code;
        @Expose private String url;

        public CodeAndUrlMessage(String from, String to, String msg, int type) {
            super(from, to, msg, type);
        }

        public CodeAndUrlMessage(String from, String to, String msg, int type, int code, String url){
            super(from, to, msg, type);
            this.code = code;
            this.url = url;
        }

        public int getCode() {
            return code;
        }
        public void setCode(int code) {
            this.code = code;
        }
        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
    }


}
