package com.dt.cloudmsg.views;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.component.ImageBtSingle;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * 自定义ToggleButton的例子
 * 
 * @author wwj 2013年8月14
 */
public class SettingActivityNew extends Activity {

	private ImageBtSingle cont_back;
	private LinearLayout setting_layer;
	private FrameLayout top_content;
	private FrameLayout function_content;

	private RelativeLayout top_frame;
	private ScrollView function_main_frame;

	private RelativeLayout sever_config;
	private TextView sever_config_tv_title;
	private TextView sever_config_tv_desc;

	private RelativeLayout account_status;
	private RelativeLayout black_list;
	private RelativeLayout about_us;
	private RelativeLayout check_update;
	private RelativeLayout give_suggestion;

	private ToggleButton toggle_Sever;
	private ToggleButton toggle_Push;
	private ImageButton toggleButton_Sever;
	private ImageButton toggleButton_Push;

	private static final String TOGGLE_SEVER_CHANGED = "toggle_sever_changed";
	private static final String TOGGLE_PUSH_CHANGED = "toggle_push_changed";

	private static final String SEVER_ONOFF = "server_onoff"; // 自动播放
	private static final String PUSH_ONOFF = "push_onoff"; // 开机自启动

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.setting_activity);

		initView();
		setListeners();
	}

	private void initView() {
		setting_layer = (LinearLayout) findViewById(R.id.setting_layer);
		top_content = (FrameLayout) findViewById(R.id.setting_top_layout);
		function_content = (FrameLayout) findViewById(R.id.setting_function_layout);
		initTopLayer();
		initFunctionLayer();

	}

	private void initTopLayer() {
		// TODO Auto-generated method stub
		top_frame = (RelativeLayout) View.inflate(this, R.layout.setting_top,
				null);
		top_content.addView(top_frame);

		cont_back = (ImageBtSingle) findViewById(R.id.setting_top_back_btn);
		cont_back.setImageResource(R.drawable.back);

		cont_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				backMng();
			}
		});

		setTitle();

	}

	private void setTitle() {
		// TODO Auto-generated method stub

	}

	private void initFunctionLayer() {
		// TODO Auto-generated method stub
		initSettingMain();
	}

	private void initSettingMain() {
		// TODO Auto-generated method stub
		function_main_frame = (ScrollView) View.inflate(this,
				R.layout.setting_main, null);
		function_content.addView(function_main_frame);
		sever_config = (RelativeLayout) findViewById(R.id.setting_sever_config);
		setConfigDesc();
		account_status = (RelativeLayout) findViewById(R.id.setting_account_status);
		black_list = (RelativeLayout) findViewById(R.id.setting_black_list);
		about_us = (RelativeLayout) findViewById(R.id.setting_about_us);
		check_update = (RelativeLayout) findViewById(R.id.setting_check_update);

		toggle_Sever = (ToggleButton) findViewById(R.id.setting_sever_toggle);
		toggle_Push = (ToggleButton) findViewById(R.id.setting_push_toggle);
		toggleButton_Sever = (ImageButton) findViewById(R.id.setting_sever_toggle_btn);
		toggleButton_Push = (ImageButton) findViewById(R.id.setting_push_toggle_btn);

		// 是否自动播放，获取SharePerference保存的用户配置
		boolean isSeverOn = getPreferences(this, SEVER_ONOFF, false);
		toggle_Sever.setChecked(isSeverOn);

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toggleButton_Sever
				.getLayoutParams();
		if (isSeverOn) { // 如果开启同步
			// 调整位置
			params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
			params.addRule(RelativeLayout.ALIGN_LEFT,
					R.id.setting_sever_toggle_btn);
			toggleButton_Sever.setLayoutParams(params);
			toggleButton_Sever.setImageResource(R.drawable.toggle_on);
			toggle_Sever.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		} else {
			// 调整位置
			params.addRule(RelativeLayout.ALIGN_RIGHT,
					R.id.setting_sever_toggle);
			params.addRule(RelativeLayout.ALIGN_LEFT, -1);
			toggleButton_Sever.setLayoutParams(params);
			toggleButton_Sever.setImageResource(R.drawable.toggle_off);
			toggle_Sever.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		}

		boolean isPushOn = getPreferences(this, PUSH_ONOFF, true);
		toggle_Push.setChecked(isPushOn);

		RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) toggleButton_Push
				.getLayoutParams();
		if (isPushOn) {
			// 调整位置
			params3.addRule(RelativeLayout.ALIGN_RIGHT, -1);
			params3.addRule(RelativeLayout.ALIGN_LEFT,
					R.id.setting_push_toggle_btn);
			toggleButton_Push.setLayoutParams(params3);
			toggleButton_Push.setImageResource(R.drawable.toggle_on);

			toggle_Push.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		} else {
			// 调整位置
			params3.addRule(RelativeLayout.ALIGN_RIGHT,
					R.id.setting_push_toggle);
			params3.addRule(RelativeLayout.ALIGN_LEFT, -1);
			toggleButton_Push.setLayoutParams(params3);
			toggleButton_Push.setImageResource(R.drawable.toggle_off);

			toggle_Push.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		}
	}

	private void setConfigDesc() {
		// TODO Auto-generated method stub
		TextView configMsgDesc = (TextView) findViewById(R.id.setting_sever_config_tv_msgdesc);
		TextView configCallDesc = (TextView) findViewById(R.id.setting_sever_config_tv_calldesc);
	}

	private void backMng() {
		// TODO Auto-generated method stub

	}

	private void setListeners() {
		toggle_Sever.setOnCheckedChangeListener(new ToggleListener(this,
				TOGGLE_SEVER_CHANGED, toggle_Sever, toggleButton_Sever));

		toggle_Push.setOnCheckedChangeListener(new ToggleListener(this,
				TOGGLE_PUSH_CHANGED, toggle_Push, toggleButton_Push));

		// UI事件，按钮点击事件
		OnClickListener clickToToggleSeverListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggle_Sever.toggle();
			}
		};
		// UI事件，按钮点击事件
		OnClickListener clickToTogglePushListener = new OnClickListener() {
			public void onClick(View v) {
				toggle_Push.toggle();
			}
		};
		toggleButton_Sever.setOnClickListener(clickToToggleSeverListener);
		toggleButton_Sever.setOnClickListener(clickToTogglePushListener);
	}

	/**
	 * 获取配置
	 * 
	 * @param context
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static boolean getPreferences(Context context, String name,
			boolean defaultValue) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean value = prefs.getBoolean(name, defaultValue);
		return value;
	}

	/**
	 * 保存用户配置
	 * 
	 * @param context
	 * @param name
	 * @param value
	 * @return
	 */
	public static boolean setPreferences(Context context, String name,
			boolean value) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putBoolean(name, value);
		return editor.commit(); // 提交
	}

	/**
	 * 状态按钮的监听事件
	 * 
	 * @author wwj
	 * 
	 */
	public class ToggleListener implements OnCheckedChangeListener {
		private Context context;
		private String settingName;
		private ToggleButton toggle;
		private ImageButton toggle_Button;

		public ToggleListener(Context context, String settingName,
				ToggleButton toggle, ImageButton toggle_Button) {
			this.context = context;
			this.settingName = settingName;
			this.toggle = toggle;
			this.toggle_Button = toggle_Button;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// 保存设置
			if (settingName.equals(TOGGLE_SEVER_CHANGED)) {
				setPreferences(context, SEVER_ONOFF, isChecked);
			} else if (settingName.equals(TOGGLE_PUSH_CHANGED)) {
				setPreferences(context, PUSH_ONOFF, isChecked);
			}
			// 改变按钮状态
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toggle_Button
					.getLayoutParams();
			if (isChecked) {
				// 调整位置
				params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
				if (settingName.equals(TOGGLE_SEVER_CHANGED)) {
					params.addRule(RelativeLayout.ALIGN_LEFT,
							R.id.setting_sever_toggle);
				} else if (settingName.equals(TOGGLE_PUSH_CHANGED)) {
					params.addRule(RelativeLayout.ALIGN_LEFT,
							R.id.setting_push_toggle);
				}
				toggle_Button.setLayoutParams(params);
				toggle_Button.setImageResource(R.drawable.toggle_on);
				toggle.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
				// 播放动画
				TranslateAnimation animation = new TranslateAnimation(dip2px(
						context, 40), 0, 0, 0);
				animation.setDuration(200);
				toggle_Button.startAnimation(animation);
			} else {
				// 调整位置
				if (settingName.equals(TOGGLE_SEVER_CHANGED)) {
					params.addRule(RelativeLayout.ALIGN_RIGHT,
							R.id.setting_sever_toggle);
				} else if (settingName.equals(TOGGLE_PUSH_CHANGED)) {
					params.addRule(RelativeLayout.ALIGN_RIGHT,
							R.id.setting_push_toggle);
				}
				params.addRule(RelativeLayout.ALIGN_LEFT, -1);
				toggle_Button.setLayoutParams(params);
				toggle_Button.setImageResource(R.drawable.toggle_off);

				toggle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				// 播放动画
				TranslateAnimation animation = new TranslateAnimation(dip2px(
						context, -40), 0, 0, 0);
				animation.setDuration(200);
				toggle_Button.startAnimation(animation);
			}
		}

	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

}
