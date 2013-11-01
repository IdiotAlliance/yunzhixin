package com.dt.cloudmsg.service;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.dt.cloudmsg.R;
import com.dt.cloudmsg.beans.BindChannel;
import com.dt.cloudmsg.beans.Devices;
import com.dt.cloudmsg.beans.HeartBeat;
import com.dt.cloudmsg.beans.MessageBean;
import com.dt.cloudmsg.beans.ReturnMsg;
import com.dt.cloudmsg.communications.MsgSender;
import com.dt.cloudmsg.communications.MyServiceConnectionMsgListener;
import com.dt.cloudmsg.dao.AccountDAO;
import com.dt.cloudmsg.dao.ChatMsgDAO;
import com.dt.cloudmsg.dao.DeviceDAO;
import com.dt.cloudmsg.dao.MsgListDAO;
import com.dt.cloudmsg.dao.NoteDAO;
import com.dt.cloudmsg.datasource.ChatMsgSource;
import com.dt.cloudmsg.datasource.ContactsSource;
import com.dt.cloudmsg.datasource.MsgListSource;
import com.dt.cloudmsg.datasource.ServerSource;
import com.dt.cloudmsg.model.Account;
import com.dt.cloudmsg.model.ChatMsgEntity;
import com.dt.cloudmsg.model.Contact;
import com.dt.cloudmsg.model.ContactsEntity;
import com.dt.cloudmsg.model.Device;
import com.dt.cloudmsg.model.MsgListEntity;
import com.dt.cloudmsg.util.Encoder;
import com.dt.cloudmsg.util.IntentConstants;
import com.dt.cloudmsg.util.JsonUtil;
import com.dt.cloudmsg.util.MsgIdGenerator;
import com.dt.cloudmsg.util.NumberFormatter;
import com.dt.cloudmsg.util.Parser;
import com.dt.cloudmsg.util.StringUtil;
import com.dt.cloudmsg.util.SystemConstants;
import com.dt.cloudmsg.views.BaseActivity;
import com.dt.cloudmsg.views.LoginActivity;
import com.dt.cloudmsg.views.SettingActivity;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

public class MyService extends Service implements Handler.Callback {
	
	private static final String TAG = MyService.class.getName();

	private static NotificationManager nm;
    private static TelephonyManager tm;
    private static SharedPreferences sp;

    private static Account account;

    // SharedPreferences 配置信息
    private static String hostname;
    private static String hostnum;
    private static boolean isServerOn = false,
                           isMsgReplyOn = false,
                           isCallReplyOn = false,
                           isPushOn = false;
    private static String smsReply = null,
                          callReply = null;

    private static boolean running = false;
    private static boolean contactsLoadded = false;
    private static boolean pushon = false;

    // 用于存储发送出去的消息实体，如果发送成功，则从缓存中删除，否等待重新发送
    private static final Map<Integer, MessageBean> msgs = new HashMap<Integer, MessageBean>();
    // 用于记录自动回复的时间， 若最近回复过，则不发送自动回复
    private static final Map<String, Long> replyCounter = new HashMap<String, Long>();
    // 用于记录控制命令的缓存，从msgId到数据库中id的映射
    private static final Map<Integer, Long> sendingCache = new HashMap<Integer, Long>();
    // 用于缓存正在发送中的短消息，等待发送结果
    private static final Map<String, SmsHolder> smsSendCache = new HashMap<String, SmsHolder>();
    // 用于等待联系加载的消息缓存
    private static final Queue<MessageBean> mbCache = new PriorityQueue<MessageBean>();
    //
    private static final List<Contact> contacts = new ArrayList<Contact>();


    private static final IntentFilter smsFilter = new IntentFilter(IntentConstants.INTENT_ACTION_SMS_RECV);
    private static BroadcastReceiver smsReceiver  = null;

    private static final IntentFilter callFilter = new IntentFilter(IntentConstants.INTENT_ACTION_CALL_RECV);
    private static BroadcastReceiver callReceiver = null;

    private static final IntentFilter msgFilter = new IntentFilter(IntentConstants.INTENT_ACTION_MSG_RECV);
    private static BroadcastReceiver msgReceiver  = null;

    private static final IntentFilter sendFilter = new IntentFilter(IntentConstants.INTENT_ACTION_SEND_MSG);
    private static BroadcastReceiver sendMsgReceiver = null;

    private static final IntentFilter rtmsgFilter = new IntentFilter(IntentConstants.INTENT_ACTION_RTMSG);
    private static BroadcastReceiver rtMsgReceiver = null;

    private static final IntentFilter hbrmFilter = new IntentFilter(IntentConstants.INTENT_ACTION_HB_MSG);
    private static BroadcastReceiver hbrmReceiver = null;

    private static final IntentFilter logoutFilter = new IntentFilter(IntentConstants.INTENT_ACTION_LOGOUT);
    private static BroadcastReceiver logoutReceiver = null;

    private static final IntentFilter syncFilter = new IntentFilter(IntentConstants.INTENT_ACTION_SYNC_DEV);
    private static BroadcastReceiver syncReceiver = null;

    private static final IntentFilter configFilter = new IntentFilter(IntentConstants.INTENT_ACTION_SET_CHAN);
    private static BroadcastReceiver configReceiver = null;

    private static final IntentFilter updateMsglistFilter = new IntentFilter(IntentConstants.INTENT_ACTION_UPDATE_MSGLIST);
    private static BroadcastReceiver updateMsgListReceiver = null;

    private static final IntentFilter smsSentFilter = new IntentFilter(IntentConstants.INTENT_ACTION_SENT_SMS);
    private static BroadcastReceiver smsSentReceiver = null;
    
    private static final IntentFilter pushMethodFilter = new IntentFilter(IntentConstants.INTENT_ACTION_BDPUSH_METHOD);
    private static BroadcastReceiver pushMethodReceiver = null;

    private static final IntentFilter boundChannelFilter = new IntentFilter(IntentConstants.INTENT_ACTION_BOUND_CHANNEL);
    private static BroadcastReceiver boundChannelReceiver = null;    
    
    private static Timer heartBeatScheduler = null;
    private static ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

    private static AccountDAO accountDAO;
    private static ChatMsgDAO chatMsgDAO;
    private static NoteDAO noteDAO;
    private static DeviceDAO deviceDAO;
    private static MsgListDAO msgListDAO;

    private static MsgListSource msgListSource;
    private static ServerSource serverSource;
    private static ChatMsgSource chatMsgSource;
    private static ContactsSource contactsSource;

    private static Handler handler;

    public static final int MSG_SEND = 0x00; // 短消息已发出
    public static final int CONTACT_LOADED = 0x01; // 联系人已加载

    public static final String KEY_TARGET  = "target";
    public static final String KEY_CONTENT = "content";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        handler = new Handler(this);
    }

	@SuppressWarnings("unchecked")
	@Override
	public int onStartCommand(Intent intent, int arg0, int arg1) {
        super.onStartCommand(intent, arg0, arg1);
        Log.d("on start", "new version");

        if(intent == null){
            this.stopSelf();
            return 0;
        }
        // 初始化全局数据
        String username  = intent.getStringExtra(IntentConstants.KEY_INTENT_SVC_UNAME);
        // 初始化数据库
        accountDAO = new AccountDAO(this);
        chatMsgDAO = new ChatMsgDAO(this);
        deviceDAO  = new DeviceDAO(this);
        msgListDAO = new MsgListDAO(this, username);
        //noteDAO    = new NoteDAO(this);
        initData(username);

        // register sources
        serverSource = new ServerSource(this, account.getAccountName());
        deviceDAO.register(serverSource);

        msgListSource = new MsgListSource(this, account.getAccountName(), null);
        msgListDAO.register(msgListSource);

        chatMsgSource = new ChatMsgSource(this, null, null, null);
        chatMsgDAO.register(chatMsgSource);

        // 启用异步线程加载联系人
        new AsyncTask(){

            @Override
            protected Object doInBackground(Object[] objects) {
                contactsSource = new ContactsSource(MyService.this);
                // 加载联系人，并排序
                int position=0;
                contacts.clear();
                while (position<contactsSource.size()) {
                    ContactsEntity contactsEntity=contactsSource.get(position);
                    String contact_id = contactsEntity.getContactID();
                    String name = contactsEntity.getDisplayName();
                    List<String> number = contactsEntity.getNumber();
                    if(number.size() > 0){
                        // 只考虑有号码的联系人
                        for(String num: number){
                            Contact c = new Contact(contacts.size(), contact_id, name, num);
                            int i = contacts.size();
                            while (i - 1 >= 0 && c.compareTo(contacts.get(i - 1)) < 0)
                                i--;
                            contacts.add(i, c);
                        }
                    }
                    position++;
                }
                handler.sendEmptyMessage(CONTACT_LOADED);
                return null;
            }
        }.execute(null, null, null);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        isServerOn = sp.getBoolean(SettingActivity.SP_KEY_SERVER_ON_OFF, false);
        isCallReplyOn = sp.getBoolean(SettingActivity.SP_KEY_SERVER_CALL_ON_OFF, false);
        isMsgReplyOn = sp.getBoolean(SettingActivity.SP_KEY_SERVER_MSG_ON_OFF, false);
        isPushOn = sp.getBoolean(SettingActivity.SP_KEY_MSG_PUSH_ON_OFF, false);

        smsReceiver = new SmsReceiver();
        callReceiver = new CallReceiver();
        msgReceiver = new MsgReceiver();
        sendMsgReceiver = new SendMsgReceiver();
        rtMsgReceiver = new ReturnMsgReceiver();
        hbrmReceiver = new HBReturnMsgReceiver();
        logoutReceiver = new LogoutReceiver();
        syncReceiver = new SyncDevicesReceiver();
        configReceiver = new ConfigReceiver();
        updateMsgListReceiver = new UpdateMsgListReceiver();
        smsSentReceiver = new SmsSentReceiver();
        pushMethodReceiver = new PushMethodReceiver();
        boundChannelReceiver = new BoundChannelReceiver();

        this.registerReceiver(smsReceiver, smsFilter);
        this.registerReceiver(callReceiver, callFilter);
        this.registerReceiver(msgReceiver, msgFilter);
        this.registerReceiver(sendMsgReceiver, sendFilter);
        this.registerReceiver(rtMsgReceiver, rtmsgFilter);
        this.registerReceiver(hbrmReceiver, hbrmFilter);
        this.registerReceiver(logoutReceiver, logoutFilter);
        this.registerReceiver(syncReceiver, syncFilter);
        this.registerReceiver(configReceiver, configFilter);
        this.registerReceiver(updateMsgListReceiver, updateMsglistFilter);
        this.registerReceiver(smsSentReceiver, smsSentFilter);
        this.registerReceiver(pushMethodReceiver, pushMethodFilter);
        this.registerReceiver(boundChannelReceiver, boundChannelFilter);

        // 开启极光推送服务
//        startJPush();
        startBDPush();
        
        // 若开启服务器，则开启心跳包
        if(isServerOn){
            startHeartBeat();
        }

        running = true;
        sendBroadcast(new Intent(IntentConstants.INTENT_ACTION_SVC_INIT));

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(smsReceiver != null) this.unregisterReceiver(smsReceiver);
        if(callReceiver != null) this.unregisterReceiver(callReceiver);
        if(msgReceiver != null) this.unregisterReceiver(msgReceiver);
        if(sendMsgReceiver != null) this.unregisterReceiver(sendMsgReceiver);
        if(rtMsgReceiver != null) this.unregisterReceiver(rtMsgReceiver);
        if(hbrmReceiver != null) this.unregisterReceiver(hbrmReceiver);
        if(logoutReceiver != null) this.unregisterReceiver(logoutReceiver);
        if(syncReceiver != null) this.unregisterReceiver(syncReceiver);
        if(configReceiver != null) this.unregisterReceiver(configReceiver);
        if(updateMsgListReceiver != null) this.unregisterReceiver(updateMsgListReceiver);
        if(smsSentReceiver != null) this.unregisterReceiver(smsSentReceiver);
        if(pushMethodReceiver != null) this.unregisterReceiver(pushMethodReceiver);
        if(boundChannelReceiver != null) this.unregisterReceiver(boundChannelReceiver);
        
        //stopJPush();
        stopBDPush();
        if(isServerOn)
        	stopHeartBeat();

        // 关闭数据库
        if(accountDAO != null) accountDAO.close();
        if(msgListDAO != null) msgListDAO.close();
        if(chatMsgDAO != null) chatMsgDAO.close();
        if(deviceDAO != null) deviceDAO.close();

        if(serverSource != null) serverSource.close();
        if(msgListSource != null) msgListSource.close();
        if(chatMsgSource != null) chatMsgSource.close();

        running = false;
        contactsLoadded = false;
        pushon = false;
    }

    public static boolean isRunning(){
        return running;
    }

    public static boolean isBound(){
        return deviceDAO.isBound(account.getIMEI());
    }
//
//    /****
//     * 开启极光推送服务
//     */
//    private void startJPush(){
//        // 开启JPush服务
//        JPushInterface.setDebugMode(true);
//        // 设置tag和alias
//        Set<String> tags = new HashSet<String>();
//        tags.add(account.getAccountName());
//        // alias = username_imei, tag = username
//        JPushInterface.setAliasAndTags(this, account.getAccountName() + "_" + account.getIMEI(), tags, new MyTagAliasCallback());
//        JPushInterface.init(this);
//        JPushInterface.resumePush(this);
//    }
//
//    /***
//     * 关闭极光推送服务
//     */
//    private void stopJPush(){
//        // 关闭JPush
//        JPushInterface.stopPush(this);
//    }
//
    /***
     * 开启百度推送服务
     */
    private void startBDPush(){
    	List<String> tags = new ArrayList<String>();
    	tags.add(account.getAccountName());
		PushManager.setTags(this, tags);
		
		// 以apikey的方式登录，一般放在主Activity的onCreate中
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, 
				StringUtil.getMetaValue(MyService.this, "api_key"));
    }
    
    /***
     * 关闭百度推送服务
     */
    private void stopBDPush(){
    	PushManager.stopWork(this);
    }
    

    /***
     * 开启心跳包
     */
    private void startHeartBeat(){
        // 开启心跳服务
        heartBeatScheduler = new Timer();
        heartBeatScheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                HeartBeat heartBeat = new HeartBeat(account.getToken(), account.getIMEI());
                MsgSender.post(SystemConstants.BASE_URL + SystemConstants.URL_SEND_HEARTBEAT + account.getAccountName(),
                        new String[]{SystemConstants.KEY_JSON}, new String[]{heartBeat.toDESJson()},
                        new MyServiceConnectionMsgListener(MyService.this, IntentConstants.INTENT_ACTION_HB_MSG));
            }
        }, 300000, 300000);

    }

    /***
     * 关闭心跳包
     */
    private void stopHeartBeat(){
        // 关闭心跳包
        if(heartBeatScheduler != null){
            heartBeatScheduler.cancel();
            heartBeatScheduler = null;
        }
    }

    private void initData(String username){
        if(username == null){
            stopSelf();
            Log.e("error in service", "illegal status");
        }
        account = accountDAO.getByAccount(username);
        if(account == null){
            stopSelf();
            Log.e("error in service", "illegal status");
        }

        // init data from settings
        String accountName = account.getAccountName();
        hostname = sp.getString(SettingActivity.SP_KEY_LOCALHOST_NAME + accountName, null);
        hostnum  = sp.getString(SettingActivity.SP_KEY_LOCALHOST_NUM + accountName, null);
        isServerOn = sp.getBoolean(SettingActivity.SP_KEY_SERVER_ON_OFF + accountName, false);
        smsReply = sp.getString(SettingActivity.SP_KEY_SERVER_MSG_REPLY + accountName, null);
        callReply = sp.getString(SettingActivity.SP_KEY_SERVER_CALL_REPLY + accountName, null);
        isPushOn = sp.getBoolean(SettingActivity.SP_KEY_MSG_PUSH_ON_OFF + accountName, false);

        Log.d("hostname", hostname);
        Log.d("hostnum", hostnum);
    }

	/**
	 * send a new notification to the system status bar 
	 * @param icon
	 * @param tickerText
	 * @param when
	 * @param intentFlags
	 * @param title
	 * @param msg
	 * @param target
	 */
	private void newNotification(int icon, CharSequence tickerText, long when,
			int intentFlags, CharSequence title, CharSequence msg, Class target) {
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL; 
		notification.defaults |= Notification.DEFAULT_SOUND;
		Intent intent = new Intent(this, target);
		intent.setFlags(intentFlags);
		PendingIntent pending = PendingIntent.getActivity(this, 0, intent, 0);
		notification.setLatestEventInfo(this, title, msg, pending);
		//nm.notify(notification_id++, notification);
	}


	/**
	 * Check internet connectivity
	 * @return
	 */
	public boolean isConnectedToInternet() {

		ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null)
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}
		}
		return false;
	}

    public static ChatMsgSource getChatMsgSource(String source, String target){
        chatMsgSource.setParams(account.getAccountName(), source, target);
        return chatMsgSource;
    }

    public static MsgListSource getMsgListSource(String number){
        msgListSource.setServer(number);
        return msgListSource;
    }

    public static ServerSource getServerSource(){
        return serverSource;
    }

    public static ContactsSource getContactsSource(){
        return contactsSource;
    }

    public static List<Contact> getSortedContacts(){
        return contacts;
    }

    public static boolean isBound(String imei){
        return deviceDAO.isBound(imei);
    }

    public static boolean isContactsLoadded(){
        return contactsLoadded;
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what){
            case CONTACT_LOADED:{
                contactsLoadded = true;
                // 联系人加载完毕
                while (mbCache.size() > 0){
                    handleMsg(mbCache.remove());
                }
                break;
            }
        }
        return false;
    }
    
    private class PushMethodReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(IntentConstants.INTENT_ACTION_BDPUSH_METHOD)){
				Log.d(TAG, "push method message received");
				String method = intent.getStringExtra(IntentConstants.KEY_INTENT_METHOD);
				String content = intent.getStringExtra(IntentConstants.KEY_INTENT_EXCONTENT);
				int errcode = intent.getIntExtra(IntentConstants.KEY_INTENT_ERRCODE, 0);
				if(method != null){
					// 无账号登陆的反馈消息
					if(method.equals(PushConstants.METHOD_BIND)){
						if (errcode == 0) {
							if(sp.getBoolean("baidubound" + account.getAccountName(), false))
								return;
							String appid = "";
							String channelid = "";
							String userid = "";

							try {
								JSONObject jsonContent = new JSONObject(content);
								JSONObject params = jsonContent
										.getJSONObject("response_params");
								appid = params.getString("appid");
								channelid = params.getString("channel_id");
								userid = params.getString("user_id");
							} catch (JSONException e) {
								Log.e(TAG, "Parse bind json infos error: " + e);
							}

							
							//if(sp.getString("bdappid", null) == null){
							Editor editor = sp.edit();
							editor.putString("bdappid", appid);
							editor.putString("bdchannel_id", channelid);
							editor.putString("bduser_id", userid);
							editor.commit();
							
							String url = SystemConstants.BASE_URL + SystemConstants.URL_BIND_CHANNEL +
									     account.getAccountName() + "?" + 
									     SystemConstants.PARAM_TOKEN + "=" + account.getToken();
							BindChannel bc = new BindChannel(account.getImei(), 
															 Long.parseLong(channelid),
															 userid);
							MsgSender.post(url, SystemConstants.KEY_JSON, bc.toDESJson(), 
									new MyServiceConnectionMsgListener(MyService.this, 
											IntentConstants.INTENT_ACTION_BOUND_CHANNEL));
							//}
						} else {
							// toastStr = "Bind Fail, Error Code: " + errorCode;
							if (errcode == 30607) {
								Log.d("Bind Fail", "update channel token-----!");
							}
						}
					}
				}
			}
		}
    }
    
    private class BoundChannelReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(IntentConstants.INTENT_ACTION_BOUND_CHANNEL)){
				Log.d(TAG, "boudn channel message received");
				Bundle bundle = intent.getExtras();
                if(bundle != null){
                    switch ((IntentConstants.IntentCode) bundle.getSerializable(IntentConstants.KEY_INTENT_CODE)){
                        case INTENT_ERROR:{
                        	String msg = bundle.getString(IntentConstants.KEY_INTENT_MSG);
                        	Toast.makeText(MyService.this, msg, Toast.LENGTH_LONG).show();
                        	break;
                        }
                        case INTENT_OK:{
                        	String body = bundle.getString(IntentConstants.KEY_INTENT_BODY);
                            try {
                                ReturnMsg rm = Parser.fromEncodedJson(body, ReturnMsg.class);
                                if(rm != null){
                                    if(rm.fail()){
                                        switch (rm.getFail()){
                                            case ReturnMsg.FAIL:{
                                                Toast.makeText(MyService.this, rm.getMsg(), Toast.LENGTH_LONG).show();
                                                break;
                                            }
                                            case ReturnMsg.TOKEN_ERROR:{
                                                logout();
                                                break;
                                            }
                                        }
                                    }else{
                                    	Log.d(TAG, "设备已与百度推送服务绑定");
                                    	pushon = true;
                                    	Editor editor = sp.edit();
                                    	editor.putBoolean("baidubound" + account.getAccountName(), true);
                                    	editor.commit();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        	break;
                        }
                    }
                }
			}
		}
    }

    /***
     * 接收配置状态更新的receiver
     */
    private class ConfigReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_SET_CHAN)){
                int result = intent.getIntExtra(IntentConstants.KEY_INTENT_MSG_SET_MASK, 0);
                if((result & SettingActivity.MASK_NAME_CHANGED) > 0){
                    hostname = sp.getString(SettingActivity.SP_KEY_LOCALHOST_NAME + account.getAccountName(), "");
                }
                if((result & SettingActivity.MASK_NUM_CHANGED) > 0){
                    hostnum = sp.getString(SettingActivity.SP_KEY_LOCALHOST_NUM + account.getAccountName(), "");
                }
                if((result & SettingActivity.MASK_SERVER_ONOFF_CHANGED) > 0){
                    // 服务器开关改变
                    isServerOn = sp.getBoolean(SettingActivity.SP_KEY_SERVER_ON_OFF + account.getAccountName(), false);
                    if(!isServerOn){
                        // 关闭服务器功能
                        stopHeartBeat();
                    }else{
                        // 开启服务器功能
                        startHeartBeat();
                    }
                }
                if((result & SettingActivity.MASK_PUSH_ONOFF_CHANGED) > 0){
                    isPushOn = sp.getBoolean(SettingActivity.SP_KEY_MSG_PUSH_ON_OFF + account.getAccountName(), false);
                }
                if((result & SettingActivity.MASK_MSG_ONOFF_CHANCHED) > 0){
                    isMsgReplyOn = sp.getBoolean(SettingActivity.SP_KEY_SERVER_MSG_ON_OFF + account.getAccountName(), false);
                }
                if((result & SettingActivity.MASK_MSG_REPLY_CHANGED) > 0){
                    smsReply = sp.getString(SettingActivity.SP_KEY_SERVER_MSG_REPLY, "");
                }
                if((result & SettingActivity.MASK_CAL_ONOFF_CHANGED) > 0){
                    isCallReplyOn = sp.getBoolean(SettingActivity.SP_KEY_SERVER_CALL_ON_OFF + account.getAccountName(), false);
                }
                if((result & SettingActivity.MASK_CAL_REPLY_CHANGED) > 0){
                    callReply = sp.getString(SettingActivity.SP_KEY_SERVER_CALL_REPLY + account.getAccountName(), "");
                }
            }
        }
    }

    private class SmsReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(isServerOn && intent.getAction().equals(IntentConstants.INTENT_ACTION_SMS_RECV)){
                Bundle extras = intent.getExtras();
                if(extras != null){
                	
                	Log.d(TAG, "sms received, prepare to send to server");
                	
                    String from = extras.getString(IntentConstants.KEY_INTENT_SMS_FROM);
                    String body = extras.getString(IntentConstants.KEY_INTENT_SMS_BODY);
                    long   ts   = extras.getLong(IntentConstants.KEY_INTENT_SMS_TS);

                    // 向服务器发送消息
                    int msgId = MsgIdGenerator.genIntegerId();
                    MessageBean mb = new MessageBean();
                    mb.setFrom(account.getIMEI());
                    mb.setTo(null);
                    mb.setType(MessageBean.TYPE_MSG_SMS);
                    mb.setRawTime(ts);
                    mb.setAccount(account.getAccountName());
                    mb.setMsgId(msgId);
                    mb.createBaseBody(from, hostnum, body);
                    String url = SystemConstants.BASE_URL +
                                 SystemConstants.URL_SEND_MSG + account.getAccountName() + "?" +
                                 SystemConstants.PARAM_TOKEN + "=" + account.getToken();
                    MsgSender.post(url, SystemConstants.KEY_JSON, mb.toDESJson(),
                            new MyServiceConnectionMsgListener(MyService.this, IntentConstants.INTENT_ACTION_RTMSG, msgId));
                    msgs.put(msgId, mb);

                    // 向消息发送者回复消息
                    if(isMsgReplyOn){
                        sendSms(from, smsReply, true, null, null);
                    }
                }
            }
        }
    }

    private class CallReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(isServerOn && intent.getAction().equals(IntentConstants.INTENT_ACTION_CALL_RECV)){
                Bundle extras = intent.getExtras();
                if(extras != null){
                    String from = extras.getString(IntentConstants.KEY_INTENT_CALL_FROM);
                    String time = extras.getString(IntentConstants.KEY_INTENT_CALL_TIME);
                    long     ts = extras.getLong(IntentConstants.KEY_INTENT_CALL_TS);
                    int    type = extras.getInt(IntentConstants.KEY_INTENT_CALL_TYPE);

                    int msgId = MsgIdGenerator.genIntegerId();
                    MessageBean mb = new MessageBean();
                    mb.setAccount(account.getAccountName());
                    mb.setFrom(account.getIMEI());
                    mb.setMsgId(msgId);
                    mb.setRawTime(ts);
                    mb.setType(type);
                    mb.setTo(null);
                    if(isCallReplyOn){
                    	mb.createBaseBody(from, hostnum, "未接来电(已回执);响铃" + time + "秒");
                    }else{
                    	mb.createBaseBody(from, hostnum, "未接来电(未回执);响铃" + time + "秒");
                    }
                    // 加入队列等待服务器的反馈
                    msgs.put(msgId, mb);
                    String url = SystemConstants.BASE_URL +
                                 SystemConstants.URL_SEND_MSG + account.getAccountName() + "?" +
                                 SystemConstants.PARAM_TOKEN+"=" + account.getToken();
                    MsgSender.post(url, SystemConstants.KEY_JSON, mb.toDESJson(),
                            new MyServiceConnectionMsgListener(MyService.this,
                                    IntentConstants.INTENT_ACTION_RTMSG, msgId));

                    // 若开启了电话回执，发送回复短信
                    if(isCallReplyOn){
                        sendSms(from, callReply, true, null, null);
                    }
                }
            }
        }
    }



    private class MsgReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_MSG_RECV)){
                String msg = intent.getExtras().getString(IntentConstants.KEY_INTENT_BODY);

                try {
                    MessageBean mb = Parser.fromEncodedJson(msg, MessageBean.class);
                    Log.d("msg received", mb.toJson());
                    if(mb != null){
                        if((mb.getType() & MessageBean.TYPE_MSG) > 0){
                            handleMsg(mb);
                        }
                        else if((mb.getType() & MessageBean.TYPE_CMD) > 0){
                            handleCMD(mb);
                        }
                        else if((mb.getType() & MessageBean.TYPE_NOT) > 0){
                            handleNot(msg, mb);
                        }
                        else{
                            // TODO do something to handle the problem
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /***
     * 监听由其他Activity发送的要求发送消息的命令
     */
    private class SendMsgReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
        	if(intent.getAction().equals(IntentConstants.INTENT_ACTION_SEND_MSG)){
	            String sources = intent.getStringExtra(IntentConstants.KEY_INTENT_CHAT_SOURCES);
	            String targets = intent.getStringExtra(IntentConstants.KEY_INTENT_CHAT_TARGETS);
	            String msg     = intent.getStringExtra(IntentConstants.KEY_INTENT_CHAT_MSG);
	            if(sources != null){
	                String[] numbers = sources.split(";");
	                String[] dests   = targets.split(";");
	                long now = System.currentTimeMillis();
	                for(String number: numbers){
	                    String imei = deviceDAO.getImei(number);
	                	Log.d(TAG, "using " + number + "(" + imei + ")" + " to send sms");
	                    if(imei != null){
	                        MessageBean mb = new MessageBean();
	
	                        mb.setType(MessageBean.TYPE_CMD_SEND_SMS);
	                        mb.setAccount(account.getAccountName());
	                        mb.setFrom(account.getIMEI());
	                        mb.setTo(imei);
	                        mb.setRawTime(System.currentTimeMillis());
	
	                        // 创建本地的ChatMsgEntity
	                        for(String dest: dests){
	                            final int msgId = MsgIdGenerator.genIntegerId();
                                Log.d(TAG, "sending msg with msgId: " + msgId);
	                            mb.setMsgId(msgId);
	                            mb.createBaseBody(number, dest, msg);
	
	                            // 发送命令
	                            String url = SystemConstants.BASE_URL + SystemConstants.URL_SEND_MSG +
	                                         account.getAccountName() + "?" +
	                                         SystemConstants.PARAM_TOKEN + "=" + account.getToken();
	                            MsgSender.post(url, SystemConstants.KEY_JSON, mb.toDESJson(),
	                                           new MyServiceConnectionMsgListener(MyService.this,
	                                                   IntentConstants.INTENT_ACTION_CMD_EXECUTED));
	
	                            ChatMsgEntity entity = new ChatMsgEntity();
	                            entity.setMsgId(msgId);
	                            entity.setStatus(ChatMsgEntity.STATUS_SENDING);
	                            entity.setComMsg(false);
	                            entity.setComNumber(number);
	                            entity.setToNumber(NumberFormatter.normalizeNumber(dest));
	                            entity.setAccount(account.getAccountName());
	                            entity.setRawMsg(mb.getMessageBody().toJson());
	                            entity.setRawtime(now);
	                            entity.setServertime(chatMsgDAO.getLatestServerTime() + 1);
	                            entity.setType(MessageBean.TYPE_MSG_SMS);
	                            entity.setBody(mb.getMessageBody());
	                            final long id = chatMsgDAO.add(entity);
	                            sendingCache.put(msgId, id);
	                            
	                            Log.d(TAG, "SENDING MSG");
	                            MsgListEntity mle = new MsgListEntity();
	                            mle.setAccount(account.getAccountName());
	                            mle.setComNumber(NumberFormatter.normalizeNumber(dest));
	                            mle.setComname(contactsSource.getDisplayName(dest));
	                            mle.setRtime(now);
	                            mle.setStime(entity.getServertime());
	                            mle.setLastMsg(msg);
	                            mle.setSource(number);
	                            MsgListEntity old = msgListDAO.getEntity(account.getAccountName(), number,
                                        NumberFormatter.normalizeNumber(dest));
	                            if(old == null){
	                            	mle.setCount(1);
                                }
	                            else{
	                            	mle.setCount(old.getCount() + 1);
                                    mle.setId(old.getId());
                                }
	                            msgListDAO.addOrUpdate(mle);
	                            
	                            // 若30秒后依然没有回复，则判定发送失败
	                            ses.schedule(new Runnable() {
	                                @Override
	                                public void run() {
	                                    if(sendingCache.containsKey(msgId)){
	                                        long id = sendingCache.remove(msgId);
	                                        chatMsgDAO.setFail(id);
	                                    }
	                                }
	                            }, 30, TimeUnit.SECONDS);
	                        }
	                    }
	                }
	            }
	        }
	        }
    }

    /***
     * 接受同步短信和电话后返回的return msg，如果发送失败，保存需要发送的消息并在一段时间俺后尝试重新
     * 发送。
     */
    private class ReturnMsgReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_RTMSG)){
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    switch ((IntentConstants.IntentCode) bundle.getSerializable(IntentConstants.KEY_INTENT_CODE)){
                        case INTENT_ERROR:{
                            String msg = bundle.getString(IntentConstants.KEY_INTENT_MSG);
                            Toast.makeText(MyService.this, msg, Toast.LENGTH_LONG).show();
                            break;
                        }
                        case INTENT_OK:{
                            String body = bundle.getString(IntentConstants.KEY_INTENT_BODY);
                            try {
                                ReturnMsg rm = Parser.fromEncodedJson(body, ReturnMsg.class);
                                if(rm != null){
                                    if(rm.fail()){
                                        switch (rm.getFail()){
                                            case ReturnMsg.FAIL:{
                                                Toast.makeText(MyService.this, rm.getMsg(), Toast.LENGTH_LONG).show();
                                                break;
                                            }
                                            case ReturnMsg.TOKEN_ERROR:{
                                                logout();
                                                break;
                                            }
                                        }
                                    }else{
                                        // 发送成功，删掉消息的缓存
                                        msgs.remove(bundle.getInt(IntentConstants.KEY_INTENT_MSGID));
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private class HBReturnMsgReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }


    /***
     * 接收要求登出当前帐号的广播
     */
    private class LogoutReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_LOGOUT)){
                logout();
            }
        }
    }

    private class SyncDevicesReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_SYNC_DEV)){
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    switch ((IntentConstants.IntentCode)bundle.get(IntentConstants.KEY_INTENT_CODE)){
                        case INTENT_ERROR:{
                            String msg = bundle.getString(IntentConstants.KEY_INTENT_MSG);
                            Toast.makeText(MyService.this, msg, Toast.LENGTH_LONG).show();
                            break;
                        }
                        case INTENT_OK:{
                            String body = bundle.getString(IntentConstants.KEY_INTENT_BODY);
                            try {
                                ReturnMsg rm = Parser.fromEncodedJson(body, ReturnMsg.class);
                                Log.d("sync message:", rm.toJson());
                                if(!rm.fail()){
                                    Devices devices = JsonUtil.fromJson(rm.getMsg(), Devices.class);
                                    if(devices != null){
                                        for(Device device: devices.getDevices()){
                                        	device.setAccount(account.getAccountName());
                                            deviceDAO.addOrUpdate(device);
                                        }
                                    }
                                    // 发送广播通知msgcenter
                                    Intent intent1 = new Intent(IntentConstants.INTENT_ACTION_UPDATE_SERVER);
                                    sendBroadcast(intent1);
                                    Toast.makeText(MyService.this, "成功更新设备状态", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    switch (rm.getFail()){
                                        case ReturnMsg.TOKEN_ERROR:{
                                            logout();
                                            break;
                                        }
                                        case ReturnMsg.FAIL:{
                                            Toast.makeText(MyService.this, rm.getMsg(), Toast.LENGTH_LONG).show();
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private class UpdateMsgListReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_UPDATE_MSGLIST)){
                String source = intent.getStringExtra(IntentConstants.KEY_INTENT_MSG_CHAT_SOURCE);
                String target = intent.getStringExtra(IntentConstants.KEY_INTENT_MSG_CHAT_TARGET);
                msgListDAO.setRead(account.getAccountName(), source, target);
            }
        }
    }

    /****
     * 接收短消息发送成功的广播
     */
    private class SmsSentReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            int msgId = intent.getIntExtra(IntentConstants.KEY_INTENT_SVC_MSGID, -1);
            String imei = intent.getStringExtra(IntentConstants.KEY_INTENT_SVC_IMEI);
            String key  = intent.getStringExtra(IntentConstants.KEY_INTENT_SVC_KEY);
            Log.d(TAG, "sms has beean sent, response to msg with id:" + msgId);
            Log.d(TAG, "sms has beean sent, response to msg with key:" + key);
            Log.d(TAG, "sms has beean sent, response to msg with imei:" + imei);

            smsSendCache.remove(key);
            long now = System.currentTimeMillis();
            MessageBean mb = new MessageBean();
            mb.setFrom(account.getIMEI());
            mb.setTo(imei);
            mb.setRawTime(now);
            mb.setServerTime(now);
            mb.setAccount(account.getAccountName());
            mb.createBaseBody(null, null, msgId + "");
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Log.d("sms has been sent", "");
                    mb.setType(MessageBean.TYPE_CMD_SEND_OK);
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Log.d("sms send failed", "");
                    mb.setType(MessageBean.TYPE_CMD_SEND_FAIL);
                    break;
            }
            String url = SystemConstants.BASE_URL + SystemConstants.URL_SEND_MSG +
                         account.getAccountName() + "?" + SystemConstants.PARAM_TOKEN + "=" + account.getToken();
            MsgSender.post(url, SystemConstants.KEY_JSON, mb.toDESJson(), null);
            MyService.this.unregisterReceiver(this);
        }
    }

//    /****
//     *
//     */
//    private class SmsDeliveredReceiver extends BroadcastReceiver{
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//        }
//    }

//    /***
//     *
//     */
//    private class MyTagAliasCallback implements TagAliasCallback{
//
//        @Override
//        public void gotResult(int i, String s, Set<String> strings) {
//
//        }
//    }

    /***
     * 登出
     */
    private void logout(){
        // 首先关闭所有正在运行的activity
        BaseActivity.logout();

        // 开启登陆界面
        Intent intent1 = new Intent(MyService.this, LoginActivity.class);
        startActivity(intent1);
        Toast.makeText(MyService.this, R.string.error_please_signin_again, Toast.LENGTH_LONG).show();

        // 关闭service
        stopSelf();
    }

    private void sendSms(String target, String content, boolean check,
                         PendingIntent sentIntent, PendingIntent delivIntent){
        if(check){
            if(replyCounter.containsKey(target)){
                long now = System.currentTimeMillis();
                if(System.currentTimeMillis() - replyCounter.get(target) < 10 * 60 * 1000){
                    replyCounter.put(target, now);
                    return;
                }
            }else{
                replyCounter.put(target, System.currentTimeMillis());
            }
        }

        if(target != null && content != null){
            SmsManager sm = SmsManager.getDefault();
            List<String> fragments = sm.divideMessage(content);
            for(String fragment: fragments){
                sm.sendTextMessage(target, null, fragment, sentIntent, delivIntent);
            }
        }
    }


    private void handleMsg(MessageBean messageBean){
        if(!contactsLoadded){
            mbCache.add(messageBean);
            return;
        }
        // 先把数据存到数据库
        String body = messageBean.getMessageBody().toJson();
        Log.d("raw msg", body);
        ChatMsgEntity entity = new ChatMsgEntity();
        entity.setAccount(account.getAccountName());
        entity.setMsgId(messageBean.getMsgId());
        entity.setRawtime(messageBean.getRawTime());
        entity.setServertime(messageBean.getServerTime());
        entity.setType(messageBean.getType());
        entity.setComNumber(NumberFormatter.normalizeNumber(messageBean.getMessageBody().getFrom()));
        entity.setToNumber(NumberFormatter.normalizeNumber(messageBean.getMessageBody().getTo()));
        entity.setComMsg(true);
        entity.setRawMsg(messageBean.getMessageBody().toJson());
        entity.setBody(JsonUtil.fromJson(body, MessageBean.BaseBody.class));
        chatMsgDAO.add(entity);

        // 再更改MsgList数据库
        MsgListEntity entity1 = msgListDAO.getEntity(account.getAccountName(),
                entity.getToNumber(), entity.getComNumber());
        if(entity1 == null){
            entity1 = new MsgListEntity();
            entity1.setAccount(account.getAccountName());
            entity1.setNewCall(0);
            entity1.setMsgCount(0);
            entity1.setComname(contactsSource.getDisplayName(entity.getComNumber()));
            entity1.setComNumber(entity.getComNumber());
            entity1.setSource(entity.getToNumber());
            entity1.setType(messageBean.getType());
            entity1.setCount(1);
        }
        else
        	entity1.setCount(entity1.getCount() + 1);
        entity1.setLastMsg(messageBean.getMessageBody().getMsg());
        entity1.setRtime(messageBean.getRawTime());
        entity1.setStime(messageBean.getServerTime());
        switch (messageBean.getType()){
            case MessageBean.TYPE_MSG_SMS:{
                entity1.setMsgCount(entity1.getMsgCount() + 1);
                break;
            }
            case MessageBean.TYPE_MSG_CAL_MISS:{
                entity1.setNewCall(entity1.getNewCall() + 1);
                break;
            }
        }
        msgListDAO.addOrUpdate(entity1);
    }

    private void handleCMD(MessageBean messageBean){
        switch (messageBean.getType()){
            case MessageBean.TYPE_CMD_SEND_SMS:{
                final int msgId = messageBean.getMsgId();
                Log.d(TAG, "msgId received: " + msgId);
                final String fromIMEI = messageBean.getFrom();
                final String to  = messageBean.getMessageBody().getTo();
                final String msg = messageBean.getMessageBody().getMsg();
                final String key = System.currentTimeMillis() + "";
                Log.d("sending sms", "to:" + to + " msg:" + msg);
                Log.d("sending key", key);
                smsSendCache.put(key, new SmsHolder(msgId, fromIMEI));

                final BroadcastReceiver ssr = new SmsSentReceiver();
                MyService.this.registerReceiver(ssr,
                        new IntentFilter(IntentConstants.INTENT_ACTION_SENT_SMS + key));

                // 发送短消息
                Intent intent = new Intent(IntentConstants.INTENT_ACTION_SENT_SMS + key);
                intent.putExtra(IntentConstants.KEY_INTENT_SVC_MSGID, msgId);
                intent.putExtra(IntentConstants.KEY_INTENT_SVC_IMEI, fromIMEI);
                intent.putExtra(IntentConstants.KEY_INTENT_SVC_KEY, key);
                Log.d(TAG, "id set:" + intent.getIntExtra(IntentConstants.KEY_INTENT_SVC_MSGID, -1));
                PendingIntent sendPI = PendingIntent.getBroadcast(MyService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                sendSms(to, msg, false, sendPI, null);

                // 15秒后发送失败
                ses.schedule(new Runnable() {
                    @Override
                    public void run() {
                        if(smsSendCache.remove(key) != null){
                            MyService.this.unregisterReceiver(ssr);
                            // 发送失败
                            MessageBean mb = new MessageBean();
                            mb.setType(MessageBean.TYPE_CMD_SEND_FAIL);
                            mb.setFrom(account.getIMEI());
                            mb.setTo(fromIMEI);
                            mb.setRawTime(System.currentTimeMillis());
                            mb.setAccount(account.getAccountName());
                            mb.createBaseBody(null, null, msgId + "");
                            String url = SystemConstants.BASE_URL + SystemConstants.URL_SEND_MSG +
                                         account.getAccountName() + "?" +
                                         SystemConstants.PARAM_TOKEN + "=" + account.getToken();
                            MsgSender.post(url, SystemConstants.KEY_JSON, mb.toDESJson(), null);
                        }
                    }
                }, 15, TimeUnit.SECONDS);

                break;
            }
            case MessageBean.TYPE_CMD_SEND_FAIL:{
                int msgId = Integer.parseInt(messageBean.getMessageBody().getMsg());
                if(sendingCache.containsKey(msgId)){
                    long id = sendingCache.remove(msgId);
                    chatMsgDAO.setFail(id);
                }
                break;
            }
            case MessageBean.TYPE_CMD_SEND_OK:{
                int msgId = Integer.parseInt(messageBean.getMessageBody().getMsg());
                if(sendingCache.containsKey(msgId)){
                    long id = sendingCache.remove(msgId);
                    chatMsgDAO.setSuccess(id);
                }
                break;
            }
            case MessageBean.TYPE_CMD_SYNC_DEV:{
                // 同步设备状态
                MsgSender.get(SystemConstants.BASE_URL + SystemConstants.URL_SYNC_DEVICES + account.getAccountName(),
                              SystemConstants.PARAM_TOKEN, account.getToken(),
                              new MyServiceConnectionMsgListener(this, IntentConstants.INTENT_ACTION_SYNC_DEV), 0);
                break;
            }
        }
    }

    private void handleNot(String rawMsg, MessageBean messageBean){

    }

    private void handleUnknown(String rawMsg, MessageBean messageBean){

    }

    private void fakeData(){
    	
    }

//
//
//    private void initFakeData(){
//
//    }
}