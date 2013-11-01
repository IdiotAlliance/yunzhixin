package com.dt.cloudmsg.util;

import org.dom4j.xpp.ProxyXmlStartTag;

import java.util.concurrent.CopyOnWriteArrayList;

public class IntentConstants {

	public enum IntentCode{
		INTENT_OK, INTENT_ERROR;
	}
	
	public static final String KEY_INTENT_CODE = "intent_code";
	public static final String KEY_INTENT_MSG  = "intent_msg";
	public static final String KEY_INTENT_BODY = "intent_body";
    public static final String KEY_INTENT_IEMI = "intent_imei";
    public static final String KEY_INTENT_TOKEN = "intent_token";
    public static final String KEY_INTENT_MSGID = "intent_msgid";
    public static final String KEY_INTENT_METHOD = "intent_method";
    public static final String KEY_INTENT_ERRCODE = "intent_errcode";
    public static final String KEY_INTENT_EXCONTENT = "intent_excontent";

    //
    public static final String KEY_INTENT_SMS_BODY = "intent_sms_body";
    public static final String KEY_INTENT_SMS_FROM = "intent_sms_from";
    public static final String KEY_INTENT_SMS_TS   = "intent_sms_ts";

    public static final String KEY_INTENT_CALL_FROM = "intent_call_from";
    public static final String KEY_INTENT_CALL_TIME = "intent_call_time";
    public static final String KEY_INTENT_CALL_TYPE = "intent_call_type";
    public static final String KEY_INTENT_CALL_TS   = "intent_call_ts";

    public static final String KEY_INTENT_SVC_UNAME  = "intent_svc_username";
    public static final String KEY_INTENT_SVC_MSGID  = "intent_svc_msgid";
    public static final String KEY_INTENT_SVC_IMEI   = "intent_svc_imei";
    public static final String KEY_INTENT_SVC_KEY    = "intent_svc_key";
    public static final String KEY_INTENT_SVC_SOURCE = "intent_svc_source";
    public static final String KEY_INTENT_SVC_TARGET = "intent_svc_target";

    public static final String KEY_INTENT_MSG_SET_FIRST  = "intent_msg_set_first";
    public static final String KEY_INTENT_MSG_SET_CHANGE = "intent_msg_set_changed";
    public static final String KEY_INTENT_MSG_SET_MASK   = "intent_msg_set_mask";

    public static final String KEY_INTENT_MSG_CHAT_ACCOUNT = "intent_msg_chat_account";
    public static final String KEY_INTENT_MSG_CHAT_TARGET  = "intent_msg_chat_target";
    public static final String KEY_INTENT_MSG_CHAT_SOURCE  = "intent_msg_chat_source";
    public static final String KEY_INTENT_MSG_CHAT_NAME    = "intent_msg_chat_name";

    public static final String KEY_INTENT_CHAT_SOURCES = "intent_chat_sources";
    public static final String KEY_INTENT_CHAT_TARGETS = "intent_chat_targets";
    public static final String KEY_INTENT_CHAT_MSG     = "intent_chat_msg";

    public static final String KEY_INTENT_MSG_CONTACT_SERVER = "intent_msg_contact_number";
    public static final String KEY_INTENT_MSG_CONTACT_IMEI   = "intent_msg_contact_imei";
    public static final String KEY_INTENT_MSG_CONTACT_STATUS = "intent_msg_contact_status";

	// Intent Actions
	public static final String INTENT_ACTION_REGISTER = "com.dt.cloudmsg.intent.msg.register";
	public static final String INTENT_ACTION_LOGIN    = "com.dt.cloudmsg.intent.msg.login";
    public static final String INTENT_ACTION_SMS_RECV = "com.dt.cloudmsg.intent.sms";
    public static final String INTENT_ACTION_COMFIRM  = "com.dt.cloudmsg.intent.comfirm";
    public static final String INTENT_ACTION_BIND     = "com.dt.cloudmsg.intent.bind";
    public static final String INTENT_ACTION_MSG_RECV = "com.dt.cloudmsg.intent.msg";
    public static final String INTENT_ACTION_NOTE_RECV = "com.dt.cloudmsg.intent.note";
	public static final String INTENT_ACTION_CALL_RECV = "com.dt.cloudmsg.intent.call";
	public static final String INTENT_ACTION_CMD_RECV  = "com.dt.cloudmsg.intent.cmd";
    public static final String INTENT_ACTION_SEND_MSG  = "com.dt.cloudmsg.intent.sendmsg";
    public static final String INTENT_ACTION_RTMSG     = "com.dt.cloudmsg.intent.returnmsg";
    public static final String INTENT_ACTION_SENT_SMS  = "com.dt.cloudmsg.intent.sentsms";
    public static final String INTENT_ACTION_DELI_SMS  = "com.dt.cloudmsg.intent.deliversms";
    public static final String INTENT_ACTION_HB_MSG    = "com.dt.cloudmsg.intent.heartbeat.msg";
    public static final String INTENT_ACTION_FORGOT    = "com.dt.cloudmsg.intent.forgotpass";
    public static final String INTENT_ACTION_CHANGE    = "com.dt.cloudmsg.intent.changepass";
    public static final String INTENT_ACTION_SVC_INIT  = "com.dt.cloudmsg.intent.svc.initialized";
    public static final String INTENT_ACTION_SET_CHAN  = "com.dt.cloudmsg.intent.set.changed";
    public static final String INTENT_ACTION_CONFIG    = "com.dt.cloudmsg.intent.config";
    public static final String INTENT_ACTION_LOGOUT    = "com.dt.cloudmsg.intent.logout";
    public static final String INTENT_ACTION_SYNC_DEV  = "com.dt.cloudmsg.intent.sync.devices";
    public static final String INTENT_ACTION_UPDATE_SERVER = "com.dt.cloudmsg.intent.update.server";
    public static final String INTENT_ACTION_UPDATE_MSGLIST = "com.dt.cloudmsg.intent.update.msglist";
    public static final String INTENT_ACTION_SCROLL_TO_BOTTOM = "com.dt.cloudmsg.intent.scroll.tobottom";
    public static final String INTENT_ACTION_CMD_EXECUTED = "com.dt.cloudmsg.intent.cmd.executed";
    public static final String INTENT_ACTION_FORGOT_VALID = "com.dt.cloudmsg.intent.forgot.valid";
    public static final String INTENT_ACTION_BDPUSH_METHOD = "com.dt.cloudmsg.bdpush.method";
    public static final String INTENT_ACTION_BOUND_CHANNEL = "com.dt.cloudmsg.bound.channel";
    public static final String INTENT_ACTION_DELETE_SESSION = "com.dt.cloudmsg.delete.session";
    public static final String INTENT_ACTION_SET_BLACK = "com.dt.cloudmsg.set.black";
    public static final String INTENT_ACTION_CONTACT_LOADED = "com.dt.cloudmsg.contact.loaded";
}
