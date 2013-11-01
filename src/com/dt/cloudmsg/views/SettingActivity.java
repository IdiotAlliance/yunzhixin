package com.dt.cloudmsg.views;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.beans.ReturnMsg;
import com.dt.cloudmsg.communications.MessageCenterMsgListener;
import com.dt.cloudmsg.communications.MsgSender;
import com.dt.cloudmsg.communications.SettingsMsgListener;
import com.dt.cloudmsg.dao.DeviceDAO;
import com.dt.cloudmsg.model.Device;
import com.dt.cloudmsg.util.Encoder;
import com.dt.cloudmsg.util.IntentConstants;
import com.dt.cloudmsg.util.JsonUtil;
import com.dt.cloudmsg.util.LogUtils;
import com.dt.cloudmsg.util.Parser;
import com.dt.cloudmsg.util.StringUtil;
import com.dt.cloudmsg.util.SystemConstants;

import static com.dt.cloudmsg.util.IntentConstants.IntentCode.*;

public class SettingActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private Handler handler;
	private SharedPreferences sp;
	private Dialog dialog;

	protected static final int PREFERENCE_CHANGED = 0x01;
	// 定义相关变量
	private EditTextPreference localhost_name;
	private EditTextPreference localhost_number;

	private CheckBoxPreference update_server_onoff;

	private CheckBoxPreference update_msgreply_onoff;
	private EditTextPreference update_msgreply_tv;

	private CheckBoxPreference update_callreply_onoff;
	private EditTextPreference update_callreply_tv;

	private CheckBoxPreference update_push_onoff;
	// private EditTextPreference update_push_number;

	// intent args
	private int result;
	private String username;
	private String imei;
	private String token;
	private boolean first = false;
	private boolean bound = false;
	private ConfigReceiver configReceiver;

	private String localhostName;
	private String localhostNum;
	private boolean UserverOnOff;
	private boolean UisMsgReply;
	private String UmsgReply;
	private boolean UisCallReply;
	private String UcallReply;
	private boolean UpushOnOff;
	// private String UcurrentServer;

	// shared preference values
	public static final String SP_KEY_LOCALHOST_NAME = "setting_activity_localhost_name";
	public static final String SP_KEY_LOCALHOST_NUM = "setting_activity_localhost_number";
	public static final String SP_KEY_SERVER_ON_OFF = "setting_activity_server_onoff";
	public static final String SP_KEY_SERVER_MSG_ON_OFF = "setting_activity_server_msgreply_onoff";
	public static final String SP_KEY_SERVER_MSG_REPLY = "setting_activity_server_msgreply_tv";
	public static final String SP_KEY_SERVER_CALL_ON_OFF = "setting_activity_server_callreply_onoff";
	public static final String SP_KEY_SERVER_CALL_REPLY = "setting_activity_server_callreply_tv";
	public static final String SP_KEY_MSG_PUSH_ON_OFF = "setting_activity_push_onoff";
	// public static final String SP_KEY_MSG_PUSH_NUMBER =
	// "setting_activity_push_number";

	public static final int MASK_NAME_CHANGED = 0x00001;
	public static final int MASK_NUM_CHANGED = 0x00002;
	public static final int MASK_SERVER_ONOFF_CHANGED = 0x00004;
	public static final int MASK_MSG_ONOFF_CHANCHED = 0x00008;
	public static final int MASK_MSG_REPLY_CHANGED = 0x00010;
	public static final int MASK_CAL_ONOFF_CHANGED = 0x00020;
	public static final int MASK_CAL_REPLY_CHANGED = 0x00040;
	public static final int MASK_PUSH_ONOFF_CHANGED = 0x00080;
	// public static final int MASK_PUSH_NUM_CHANGED = 0x00100;

	private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 获取当前用户名
		Intent intent = this.getIntent();
		username = intent.getStringExtra(IntentConstants.KEY_INTENT_SVC_UNAME);
		imei = intent.getStringExtra(IntentConstants.KEY_INTENT_IEMI);
		first = intent.getBooleanExtra(
				IntentConstants.KEY_INTENT_MSG_SET_FIRST, false);
		token = intent.getStringExtra(IntentConstants.KEY_INTENT_TOKEN);
		Log.d(SettingActivity.class.getName(), username);

		// 从xml文件中添加Preference项
		addPreferencesFromResource(R.xml.setting_preferenceii);
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		initPreference();
		initValues();
		setPreferences();
		initListener();

		IntentFilter filter = new IntentFilter(
				IntentConstants.INTENT_ACTION_CONFIG);
		configReceiver = new ConfigReceiver();
		registerReceiver(configReceiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(configReceiver);
	}

	private void initPreference() {
		// 获取各个Preference
		localhost_name = (EditTextPreference) findPreference(SP_KEY_LOCALHOST_NAME);
		localhost_name.getEditText().setSingleLine(true);
		localhost_number = (EditTextPreference) findPreference(SP_KEY_LOCALHOST_NUM);
		localhost_number.getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
		localhost_number.getEditText().setSingleLine(true);

		update_server_onoff = (CheckBoxPreference) findPreference(SP_KEY_SERVER_ON_OFF);
		update_msgreply_onoff = (CheckBoxPreference) findPreference(SP_KEY_SERVER_MSG_ON_OFF);
		update_msgreply_tv = (EditTextPreference) findPreference(SP_KEY_SERVER_MSG_REPLY);

		update_callreply_onoff = (CheckBoxPreference) findPreference(SP_KEY_SERVER_CALL_ON_OFF);
		update_callreply_tv = (EditTextPreference) findPreference(SP_KEY_SERVER_CALL_REPLY);

		update_push_onoff = (CheckBoxPreference) findPreference(SP_KEY_MSG_PUSH_ON_OFF);
		// update_push_number = (EditTextPreference)
		// findPreference(SP_KEY_MSG_PUSH_NUMBER);
		// update_push_number.getEditText().setInputType(InputType.TYPE_CLASS_PHONE);

	}

	private void initListener() {
		// 添加添加监听
		localhost_name.setOnPreferenceChangeListener(this);
		localhost_number.setOnPreferenceChangeListener(this);

		update_server_onoff.setOnPreferenceChangeListener(this);

		update_msgreply_onoff.setOnPreferenceChangeListener(this);
		update_msgreply_tv.setOnPreferenceChangeListener(this);

		update_callreply_onoff.setOnPreferenceChangeListener(this);
		update_callreply_tv.setOnPreferenceChangeListener(this);

		update_push_onoff.setOnPreferenceChangeListener(this);
		// update_push_number.setOnPreferenceChangeListener(this);
	}

	private void initValues() {
		localhostName = sp.getString(SP_KEY_LOCALHOST_NAME + username, "");
		localhostNum = sp.getString(SP_KEY_LOCALHOST_NUM + username, "");

		UserverOnOff = sp.getBoolean(SP_KEY_SERVER_ON_OFF + username, false);
		UisMsgReply = sp.getBoolean(SP_KEY_SERVER_MSG_ON_OFF + username, false);
		UmsgReply = sp.getString(SP_KEY_SERVER_MSG_REPLY + username, "");

		UisCallReply = sp.getBoolean(SP_KEY_SERVER_CALL_ON_OFF + username,
				false);
		UcallReply = sp.getString(SP_KEY_SERVER_CALL_REPLY + username, "");

		UpushOnOff = sp.getBoolean(SP_KEY_MSG_PUSH_ON_OFF + username, false);
		// UcurrentServer = sp.getString(SP_KEY_MSG_PUSH_NUMBER + username, "");

		// 如果是第一开启，查询当前设备是否绑定过，如果绑定过，则根据已知的信息来配置默认的信息
		DeviceDAO dd = new DeviceDAO(this);
		List<Device> devices = dd.getDeivces(username);
		for (Device device : devices) {
			if (imei.equals(device.getImei())) {
				// 该设备已经绑定过
				bound = true;
				if (first) {
					localhostName = device.getName();
					edit(SP_KEY_LOCALHOST_NAME, localhostName);

					localhostNum = device.getNumber();
					edit(SP_KEY_LOCALHOST_NUM, localhostNum);

					UserverOnOff = device.isServerOn();
					edit(SP_KEY_SERVER_ON_OFF, UserverOnOff);

					UpushOnOff = device.isPushOn();
					edit(SP_KEY_MSG_PUSH_ON_OFF, UpushOnOff);
				}
			}
		}

	}

	private void setPreferences() {
		update_server_onoff.setChecked(UserverOnOff);
		update_callreply_onoff.setChecked(UisCallReply);
		update_msgreply_onoff.setChecked(UisMsgReply);
		update_push_onoff.setChecked(UpushOnOff);

		if (localhostName.length() > 0) {
			localhost_name.setSummary(localhostName);
		}
		if (localhostNum.length() > 0)
			localhost_number.setSummary(localhostNum);
		if (UmsgReply.length() > 0)
			update_msgreply_tv.setSummary(UmsgReply);
		if (UcallReply.length() > 0)
			update_callreply_tv.setSummary(UcallReply);
		// if(UcurrentServer.length() > 0)
		// update_push_number.setSummary(UcurrentServer);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);

		if (preference.getKey().equals(SP_KEY_LOCALHOST_NAME)) {
			String name = localhost_name.getEditText().getText().toString();
			if (name == null)
				return false;
			if (name.length() > 12) {
				Toast.makeText(this, "名字过长", Toast.LENGTH_LONG).show();
				return false;
			}
			localhost_name.setSummary(name);
			return true;
		} else if (preference.getKey().equals(SP_KEY_LOCALHOST_NUM)) {
			String number = localhost_number.getEditText().getText().toString();
			if (number == null)
				return false;
			if (Pattern.compile("[1-9]+[0-9]+").matcher(number).matches()
					&& (number.length() >= 3 && number.length() <= 16)) {
				localhost_number.setSummary(number);																												
				return true;
			}
			Toast.makeText(this, "无效的号码", Toast.LENGTH_LONG).show();
			return false;
		} else if (preference.getKey().equals(SP_KEY_SERVER_ON_OFF)) {
			// 服务器开关
			return true;
		} else if (preference.getKey().equals(SP_KEY_SERVER_MSG_ON_OFF)) {
			return true;
		} else if (preference.getKey().equals(SP_KEY_SERVER_MSG_REPLY)) {
			// 短信回执内容
			String msgReply = update_msgreply_tv.getEditText().getText()
					.toString();
			if (msgReply.length() > 70) {
				LogUtils.d("test", msgReply.getBytes().length + "");
				Toast.makeText(getApplicationContext(), "请输入70字以内的回执",
						Toast.LENGTH_SHORT).show();
				return false;
			} else {
				update_msgreply_tv.setSummary(msgReply);
				return true;
			}
		} else if (preference.getKey().equals(SP_KEY_SERVER_CALL_ON_OFF)) {
			// 来电回执
			return true;
		} else if (preference.getKey().equals(SP_KEY_SERVER_MSG_REPLY)) {
			// 来电回执内容
			String callReply = update_callreply_tv.getEditText().getText()
					.toString();
			if (callReply.length() > 70) {
				Toast.makeText(getApplicationContext(), "请输入70字以内的回执",
						Toast.LENGTH_SHORT).show();
				return false;
			} else {
				update_callreply_tv.setSummary(callReply);
				return true;
			}
		} else if (preference.getKey().equals(SP_KEY_MSG_PUSH_ON_OFF)) {
			// 推送开关
			return true;
		}
		// else if (preference.getKey().equals(SP_KEY_MSG_PUSH_NUMBER)) {
		// // 推送号码修改
		// String currentServer = update_push_number.getEditText().getText()
		// .toString();
		// String serverNumber = sp.getString(SP_KEY_LOCALHOST_NUM, "");
		// if (currentServer.getBytes().length > 20) {
		// Toast.makeText(getApplicationContext(), "请输入20位以内的号码",
		// Toast.LENGTH_SHORT).show();
		// return false;
		// } else if (StringUtil.isEmpty(currentServer) ||
		// !Pattern.compile("[1-9]+[0-9]+").matcher(currentServer).matches()){
		// Toast.makeText(getApplicationContext(), "请输入正确号码",
		// Toast.LENGTH_SHORT).show();
		// return false;
		// } else if (currentServer.equals(serverNumber)) {
		// Toast.makeText(getApplicationContext(), "不能接收来自本机的消息",
		// Toast.LENGTH_SHORT).show();
		// return false;
		// }
		// update_push_number.setSummary(currentServer);
		// return true;
		//
		// }
		else {
			return true;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			String name = sp.getString(SP_KEY_LOCALHOST_NAME, "");
			String num = sp.getString(SP_KEY_LOCALHOST_NUM, "");
			boolean serverOnOff = sp.getBoolean(SP_KEY_SERVER_ON_OFF, false);

			boolean isMsgReply = sp.getBoolean(SP_KEY_SERVER_MSG_ON_OFF, false);
			String msgReply = sp.getString(SP_KEY_SERVER_MSG_REPLY, "");

			boolean isCallReply = sp.getBoolean(SP_KEY_SERVER_CALL_ON_OFF,
					false);
			String callReply = sp.getString(SP_KEY_SERVER_CALL_REPLY, "");

			boolean pushOnOff = sp.getBoolean(SP_KEY_MSG_PUSH_ON_OFF, false);
			if (name == null || num == null || name.length() == 0
					|| num.length() == 0) {
				Toast.makeText(this, "请您先配置本机的名称和号码", Toast.LENGTH_LONG).show();
				return true;
			}

			if (StringUtil.isEmpty(msgReply) && isMsgReply) {
				edit(SP_KEY_SERVER_MSG_ON_OFF, false);
				isMsgReply = false;
			}

			if (StringUtil.isEmpty(callReply) && isCallReply) {
				edit(SP_KEY_SERVER_CALL_ON_OFF, false);
				isCallReply = false;
			}

			result = 0;
			if (!name.equals(localhostName)) {
				result |= MASK_NAME_CHANGED;
			}
			if (!num.equals(localhostNum)) {
				result |= MASK_NUM_CHANGED;
			}
			if (serverOnOff != UserverOnOff) {
				result |= MASK_SERVER_ONOFF_CHANGED;
			}
			if (isMsgReply != UisMsgReply) {
				result |= MASK_MSG_ONOFF_CHANCHED;
			}
			if (!msgReply.equals(UmsgReply)) {
				result |= MASK_MSG_REPLY_CHANGED;
			}
			if (isCallReply != UisCallReply) {
				result |= MASK_CAL_ONOFF_CHANGED;
			}
			if (!callReply.equals(UcallReply)) {
				result |= MASK_CAL_REPLY_CHANGED;
			}
			if (pushOnOff != UpushOnOff) {
				result |= MASK_PUSH_ONOFF_CHANGED;
			}

			// 某些配置更改需要联网确认
			if (first || (bound && ((MASK_NAME_CHANGED & result) > 0))
					|| (bound && ((MASK_NUM_CHANGED & result) > 0))
					|| (MASK_SERVER_ONOFF_CHANGED & result) > 0
					|| (MASK_PUSH_ONOFF_CHANGED & result) > 0) {

				Device device = new Device();
				device.setName(name);
				device.setNumber(num);
				device.setImei(imei);
				device.setServerOn(serverOnOff);
				device.setPushOn(pushOnOff);

				MsgSender.post(SystemConstants.BASE_URL
						+ SystemConstants.URL_CONFIG_DEVICE + username + "?"
						+ SystemConstants.PARAM_TOKEN + "=" + token,
						new String[] { SystemConstants.KEY_JSON },
						new String[] { device.toDESJson() },
						new SettingsMsgListener(this,
								IntentConstants.INTENT_ACTION_CONFIG));

				// 弹出窗口提示等待
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
						.setTitle(null).setView(
								View.inflate(this, R.layout.loading_dialog,
										null));
				dialog = builder.create();
				dialog.show();
			} else {
				success();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	private void edit(String key, String value) {
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	private void edit(String key, Boolean value) {
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	private class ConfigReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(IntentConstants.INTENT_ACTION_CONFIG)) {
				dialog.dismiss();
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					switch ((IntentConstants.IntentCode) bundle
							.get(IntentConstants.KEY_INTENT_CODE)) {
					case INTENT_ERROR: {
						String msg = bundle
								.getString(IntentConstants.KEY_INTENT_MSG);
						Toast.makeText(SettingActivity.this, msg,
								Toast.LENGTH_LONG).show();
						fail(null);
						break;
					}
					case INTENT_OK: {
						String body = bundle
								.getString(IntentConstants.KEY_INTENT_BODY);
						try {
							ReturnMsg rm = Parser.fromEncodedJson(body, ReturnMsg.class);
                            if (rm != null) {
								if (rm.fail()) {
									fail(rm);
								} else {
									success();
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

	private void success() {

		String name = sp.getString(SP_KEY_LOCALHOST_NAME, "");
		String num = sp.getString(SP_KEY_LOCALHOST_NUM, "");
		boolean serverOnOff = sp.getBoolean(SP_KEY_SERVER_ON_OFF, false);

		boolean isMsgReply = sp.getBoolean(SP_KEY_SERVER_MSG_ON_OFF, false);
		String msgReply = sp.getString(SP_KEY_SERVER_MSG_REPLY, "");

		boolean isCallReply = sp.getBoolean(SP_KEY_SERVER_CALL_ON_OFF, false);
		String callReply = sp.getString(SP_KEY_SERVER_CALL_REPLY, "");

		boolean pushOnOff = sp.getBoolean(SP_KEY_MSG_PUSH_ON_OFF, false);
		// String currentServer = sp.getString(SP_KEY_MSG_PUSH_NUMBER, "");

		edit(SP_KEY_LOCALHOST_NAME + username, name);
		edit(SP_KEY_LOCALHOST_NUM + username, num);
		edit(SP_KEY_SERVER_ON_OFF + username, serverOnOff);
		edit(SP_KEY_SERVER_MSG_ON_OFF + username, isMsgReply);
		edit(SP_KEY_SERVER_MSG_REPLY + username, msgReply);
		edit(SP_KEY_SERVER_CALL_ON_OFF + username, isCallReply);
		edit(SP_KEY_SERVER_CALL_REPLY + username, callReply);
		edit(SP_KEY_MSG_PUSH_ON_OFF + username, pushOnOff);

		Intent data = new Intent(IntentConstants.INTENT_ACTION_SET_CHAN);
		data.putExtra(IntentConstants.KEY_INTENT_MSG_SET_CHANGE, result > 0);
		data.putExtra(IntentConstants.KEY_INTENT_MSG_SET_FIRST, first);
		data.putExtra(IntentConstants.KEY_INTENT_MSG_SET_MASK, result);
		SettingActivity.this.setResult(1, data);

		// 发送广播
		sendBroadcast(data);

		finish();
	}

	/***
	 * 若配置失败，回退到之前的状态
	 */
	private void fail(ReturnMsg rm) {
		// 将配置重置为之前的状态
		setPreferences();

		// 若是token过期，则强制登出
		if (rm != null) {
			Toast.makeText(this, rm.getMsg(), Toast.LENGTH_LONG).show();
			if (rm.getFail() == ReturnMsg.TOKEN_ERROR) {
				// TODO send broadcast to service to logout
				Intent intent = new Intent(IntentConstants.INTENT_ACTION_LOGOUT);
				sendBroadcast(intent);
			}
		}

		finish();
	}
}