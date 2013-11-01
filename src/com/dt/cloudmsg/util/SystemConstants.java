package com.dt.cloudmsg.util;

public class SystemConstants {

	public static final String DES_KEY1 = "JH8MFI02VSEQBX6LP9AZ4Y3D6UNC3GS";
	public static final String DES_KEY2 = "B2JFLANM3UXVA9Z0J3BD72HSMDOENRC";
	public static final String DES_KEY3 = "6HSLIEBZ92NEBSJA2LZOCP0DN4JSB8S";
	public static final String DES_KEY4 = "PWMX8DBAK3NS0XYEB2JAK1NS7XBGZMA";
	public static final String DES_KEY5 = "H2SNF8XHAMDUT0B4YZA0DNGV85LPEC3";

    //
    public static final String KEY_JSON = "json";

    // http params
    public static final String PARAM_TOKEN = "token";

    // http constants
    public static final String BASE_URL = "http://192.168.44.216:8080/cloudmsg/services";

    // account services
    public static final String URL_LOGIN    = "/account/login";
    public static final String URL_REGISTER = "/account/register";
    public static final String URL_COMFIRM  = "/account/comfirm";
    public static final String URL_FORGOT_PASS = "/account/forgotpass/";
    public static final String URL_CHANGE_PASS = "/account/changepass/";
    public static final String URL_BIND_CHANNEL = "/account/bindchannel/";

    // device services
    public static final String URL_CONFIG_DEVICE = "/account/device/config/";
    public static final String URL_SYNC_DEVICES  = "/account/sync/devices/";

    // msg services
    public static final String URL_SEND_MSG = "/msg/send/";
    public static final String URL_SEND_HEARTBEAT = "/msg/heartbeat/";

    // sms services
    public static final String URL_SEND_CODE = "/sms/send/";
}
