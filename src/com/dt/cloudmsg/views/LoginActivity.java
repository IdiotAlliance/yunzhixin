package com.dt.cloudmsg.views;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.*;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.beans.*;
import com.dt.cloudmsg.communications.AccountConnectionMsgListener;
import com.dt.cloudmsg.communications.MsgSender;
import com.dt.cloudmsg.dao.AccountDAO;
import com.dt.cloudmsg.dao.DeviceDAO;
import com.dt.cloudmsg.model.Account;
import com.dt.cloudmsg.model.Device;
import com.dt.cloudmsg.service.MyService;
import com.dt.cloudmsg.util.*;
import com.dt.cloudmsg.util.IntentConstants.IntentCode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

public class LoginActivity extends BaseActivity{
	private boolean contactsReady;

    private Button login;
	private Button regist;
	private Button confirm;
	private Button cancel;
    private Button comfirmRules;
    private Button validCancel;
    private Button validComfirm;
    private Button validResend;
    private Button forgotDone;
    private Button forgotCancel;
    private Button changeDone;
    private Button changeCancel;
    private Button forgotSendCode;
    private Button forgotValidCancl;
    private Button forgotValidDone;

	private BroadcastReceiver loginReceiver;
	private BroadcastReceiver regReceiver;
    private BroadcastReceiver smsReceiver;
    private BroadcastReceiver comfirmReceiver;
    private BroadcastReceiver forgotReceiver;
    private BroadcastReceiver changeReceiver;
    private BroadcastReceiver forgotValidReceiver;

	private String regUsername;
	private String regPassword;
	private String regPComfirm;
    private String lastRegUsername;
    private String tempToken;
    private String tempUsername;

	private FrameLayout content;
    private FrameLayout loginUnameFrame;
    private FrameLayout loginPwordFrame;
    private FrameLayout regUnameFrame;
    private FrameLayout regPwordFrame;
    private FrameLayout regCPwordFrame;

	private RelativeLayout loginFrame;
	private RelativeLayout registFrame;
    private RelativeLayout validFrame;
    private RelativeLayout forgotFrame;
    private RelativeLayout changeFrame;
    private RelativeLayout forgotValidFrame;

    private LinearLayout rulesWebViewDialog;

    private CheckBox rulesCheckbox;
    private WebView rulesWebView;
    private ImageView forgotRDImageView;

	private EditText usernameInputView;
	private EditText passwordInputView;
	private EditText regUsernameInputView;
	private EditText regPasswordInputView;
	private EditText regPasswordToInputView;
    private EditText validCodeInputView;
    private EditText forgotAccountInput;
    private EditText forgotCodeInput;
    private EditText forgotValidCodeInput;
    private EditText changeNewInput;
    private EditText changeComfirmInput;

    // dialog
    private ImageView dialogImg;
    private EditText dialogText;
    private TextView dialogChange;
    private TextView forgotCodeSentTo;

    private TextView policyAgreed;
    private TextView validTextView;
    //private TextView countDownTextView;
    private TextView rulesAndPrivaceTextView;
    private TextView forgotPassword;
    private TextView forgotPasswordChangeCode;

    private Timer countDownTimer;
    private Timer forgotPassTimer;

	private Handler handler;
	private boolean login_shown = true;
    private boolean countingDown = false;

    private static final Pattern UNAME_PATTERN = Pattern.compile("[1-9]+[0-9]{5,16}");
    private static final Pattern CODE_PATTERN  = Pattern.compile("[0-9]{6,6}");

    //
    private static final int MSG_UPDATE_COUNT_DOWN = 0x00;
    private static final int MSG_COUNT_DOWN_OVER = 0x01;
    private static final int MSG_UPDATE_FORGOT_COUNT_DOWN = 0x02;
    private static final int MSG_UPDATE_FORGOT_COUNT_DOWN_OVER = 0x03;

    //
    private static final String MSG_COUNT_DOWN_COUNTER = "count_down_counter";

	//private MQ mq = MQClientFactory.createMQClient();

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
        this.setContentView(R.layout.login_activity);
        // 加载联系人
        MyService.loadContacts(LoginActivity.this.getApplicationContext());

        if(activityStack.size() > 1){
            // 若之前启动过，则直接跳到之前的Activity
            LoginActivity.this.finish();

        }
        else if(MyService.isRunning()){
            Intent intent = new Intent(LoginActivity.this, MsgCenterActivity.class);
            startActivity(intent);
            finish();
        }
        else{

            // 获取SharedPreferences对象
            boolean firstStart = getSPBoolean(SP_KEY_FIRST, true);
            // 存入数据
            if (firstStart) {
                setSharedPreferences(SP_KEY_FIRST, false);
                // TODO 启动教学界面
            }

            content = (FrameLayout) findViewById(R.id.content_layout);

            initLoginLayer(content);// 初始化登录窗口
            initRegistLayer(content);// 初始化注册窗口
            initMsgValidationLayer(content);// 初始化短信验证窗口
            initForgotLayer(content);
            initChangeLayer(content);
            initForgotValidLayer(content);
            initLoadingLayer(content);// 初始化加载层
            getDialogBuilder();

            // 默认显示登录窗口
            loginFrame.setVisibility(View.VISIBLE);
            registFrame.setVisibility(View.INVISIBLE);
            validFrame.setVisibility(View.INVISIBLE);
            loadingLayer.setVisibility(View.INVISIBLE);
            forgotFrame.setVisibility(View.INVISIBLE);
            changeFrame.setVisibility(View.INVISIBLE);
            forgotValidFrame.setVisibility(View.INVISIBLE);

            handler = new Handler(this);
        }
	}

	@Override
    public void onResume(){
        super.onResume();
        // Register login intent filter
        IntentFilter loginFilter = new IntentFilter(IntentConstants.INTENT_ACTION_LOGIN);
        loginReceiver = new LoginReceiver();
        this.registerReceiver(loginReceiver, loginFilter);

        // Register registration intent filter
        IntentFilter regFilter = new IntentFilter(IntentConstants.INTENT_ACTION_REGISTER);
        regReceiver = new RegisterReceiver();
        this.registerReceiver(regReceiver, regFilter);

        IntentFilter forgotFilter = new IntentFilter(IntentConstants.INTENT_ACTION_FORGOT);
        forgotReceiver = new ForgotPassReceiver();
        this.registerReceiver(forgotReceiver, forgotFilter);

        IntentFilter changeFilter = new IntentFilter(IntentConstants.INTENT_ACTION_CHANGE);
        changeReceiver = new ChangePassReceiver();
        this.registerReceiver(changeReceiver, changeFilter);

        IntentFilter forgotValidFilter = new IntentFilter(IntentConstants.INTENT_ACTION_FORGOT_VALID);
        forgotValidReceiver = new ForgotPassValidReceiver();
        this.registerReceiver(forgotValidReceiver, forgotValidFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        this.unregisterReceiver(loginReceiver);
        this.unregisterReceiver(regReceiver);
        this.unregisterReceiver(forgotReceiver);
        this.unregisterReceiver(changeReceiver);
        this.unregisterReceiver(forgotValidReceiver);

        if(smsReceiver != null){
            this.unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }
        if(comfirmReceiver != null){
            this.unregisterReceiver(comfirmReceiver);
            comfirmReceiver = null;
        }
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initLoginLayer(final FrameLayout contentLayer) {
		loginFrame = (RelativeLayout) View.inflate(this, R.layout.dialog_login,
				null);
		contentLayer.addView(loginFrame);

		usernameInputView = (EditText) loginFrame
				.findViewById(R.id.username_input);
		passwordInputView = (EditText) loginFrame
				.findViewById(R.id.password_input);
        forgotPassword = (TextView) loginFrame
                .findViewById(R.id.dialog_login_forgot_password);

        String last = getSPString(SP_LAST_ACCOUNT, null);
        if(last != null)
            usernameInputView.setText(last);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(usernameInputView.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(passwordInputView.getWindowToken(), 0);

		login = (Button) loginFrame.findViewById(R.id.login_btn);
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String username = usernameInputView.getText().toString();
				final String password = passwordInputView.getText().toString();
                int msgRes = checkLogin(username, password);
                if(msgRes != 0){
                    showToast(LoginActivity.this, msgRes, Toast.LENGTH_LONG);
                    return;
                }

                // 若登陆过于频繁，则显示验证窗口
                long lastLoginTime = getSPLong(SP_LAST_LOGIN, 0);
                int  loginCount    = getSPInt(SP_LOGIN_COUNT, 0);
                long now = System.currentTimeMillis();
                // 若6分钟内有两次以上的登陆记录，要求输入验证码
                if(now - lastLoginTime <= 180000){
                    setSharedPreferences(SP_LAST_LOGIN, now);
                    setSharedPreferences(SP_LOGIN_COUNT, loginCount + 1);
                    if(loginCount >= 2){
                        AlertDialog.Builder builder = getDialogBuilder();
                        dialogImg.setImageBitmap(generator.createBitmap());
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String input = dialogText.getText().toString();
                                if (input != null &&
                                        generator.getCode().toLowerCase().equals(input.toLowerCase())) {
                                    dialogInterface.dismiss();
                                    dialogText.setText("");
                                    login(username, password);
                                } else {
                                    showToast(LoginActivity.this, R.string.error_invalid_code, Toast.LENGTH_LONG);
                                    dialogText.setText("");
                                    dialogImg.setImageBitmap(generator.createBitmap());
                                }
                            }
                        });
                        builder.create().show();
                        return;
                    }
                }else {
                    setSharedPreferences(SP_LAST_LOGIN, now);
                    setSharedPreferences(SP_LOGIN_COUNT, 1);
                }
                login(username, password);

			}

		});

		regist = (Button) loginFrame.findViewById(R.id.regist_btn);
		regist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				loginFrame.setVisibility(View.INVISIBLE);
				registFrame.setVisibility(View.VISIBLE);
				login_shown = false;
			}
		});

        forgotPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotFrame.setVisibility(View.VISIBLE);
                loginFrame.setVisibility(View.INVISIBLE);
                forgotRDImageView.setImageBitmap(RDBitmapGenerator.getInstance().createBitmap());
            }
        });
	}

	private void initRegistLayer(final FrameLayout contentLayer) {
		registFrame = (RelativeLayout) View.inflate(this,
				R.layout.dialog_regist, null);
		contentLayer.addView(registFrame);

		regUsernameInputView = (EditText) registFrame
				.findViewById(R.id.reg_username_input);
		regPasswordInputView = (EditText) registFrame
				.findViewById(R.id.reg_password_input);
		regPasswordToInputView = (EditText) registFrame
				.findViewById(R.id.reg_pword_comfirm_input);
        rulesWebViewDialog = (LinearLayout) registFrame
                .findViewById(R.id.dialog_register_webview_dialog);
        rulesWebView = (WebView) registFrame
                .findViewById(R.id.dialog_register_webview);
        rulesCheckbox = (CheckBox) registFrame
                .findViewById(R.id.dialog_regist_checkbox);
        policyAgreed = (TextView) registFrame
                .findViewById(R.id.dialog_register_policy_agreed);
        comfirmRules = (Button) registFrame
                .findViewById(R.id.dialog_register_webview_dialog_comfirm);

        rulesAndPrivaceTextView = (TextView) registFrame.findViewById(R.id.dialog_regist_rules_privacy);
        rulesAndPrivaceTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rulesWebViewDialog.setVisibility(View.VISIBLE);
            }
        });

        policyAgreed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rulesCheckbox.setChecked(!rulesCheckbox.isChecked());
            }
        });

        comfirmRules.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rulesWebViewDialog.setVisibility(View.INVISIBLE);
            }
        });

		cancel = (Button) registFrame.findViewById(R.id.cancel_btn);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				loginFrame.setVisibility(View.VISIBLE);
				registFrame.setVisibility(View.INVISIBLE);
				login_shown = true;

                // 清空注册信息
                regUsernameInputView.setText("");
                regPasswordInputView.setText("");
                regPasswordToInputView.setText("");
			}

		});

		confirm = (Button) registFrame.findViewById(R.id.confirm_btn);
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				regUsername = regUsernameInputView.getText().toString(); // TODO
																			// get
																			// regUsername
																			// from
																			// edittext
				regPassword = regPasswordInputView.getText().toString(); // TODE
																			// get
																			// regPassword
																			// from
																			// edittext
				regPComfirm = regPasswordToInputView.getText().toString(); // TODO get
																	// regEmail
																	// from
																	// edittext

                int msgRes = checkRegistration(regUsername, regPassword, regPComfirm);
                if(msgRes != 0){
                    showToast(LoginActivity.this, msgRes, Toast.LENGTH_LONG);
                    return;
                }

                // 预防无限次注册的逻辑
                long lastRegisterTime = getSPLong(SP_LAST_REG, 0);
                int regCount = getSPInt(SP_REG_COUNT, 0);
                long now = System.currentTimeMillis();
                if(now - lastRegisterTime <= 180000){
                    setSharedPreferences(SP_LAST_REG, now);
                    setSharedPreferences(SP_REG_COUNT, regCount + 1);
                    if(regCount >= 2){
                        //
                        AlertDialog.Builder builder = getDialogBuilder();
                        dialogImg.setImageBitmap(generator.createBitmap());
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String input = dialogText.getText().toString();
                                if (input != null &&
                                        generator.getCode().toLowerCase().equals(input.toLowerCase())) {
                                    dialogInterface.dismiss();
                                    dialogText.setText("");
                                    regist(regUsername, regPassword);
                                } else {
                                    showToast(LoginActivity.this, R.string.error_invalid_code, Toast.LENGTH_LONG);
                                    dialogText.setText("");
                                    dialogImg.setImageBitmap(generator.createBitmap());
                                }
                            }
                        });
                        builder.create().show();
                        return;
                    }
                }else{
                    setSharedPreferences(SP_LAST_REG, now);
                    setSharedPreferences(SP_REG_COUNT, 1);
                }
                regist(regUsername, regPassword);
			}

		});

	}

    private final void initMsgValidationLayer(final FrameLayout contentLayer){
        validFrame = (RelativeLayout) View.inflate(this, R.layout.dialog_msg_validation, null);
        validTextView = (TextView) validFrame.findViewById(R.id.dialog_msg_validation_phone_number);
        validCodeInputView = (EditText) validFrame.findViewById(R.id.dialog_msg_validation_code_input);
        //countDownTextView = (TextView) validFrame.findViewById(R.id.dialog_msg_validation_countdown);
        validResend = (Button) validFrame.findViewById(R.id.dialog_msg_validation_resend);
        contentLayer.addView(validFrame);

        validCancel = (Button) validFrame.findViewById(R.id.dialog_validation_cancel_btn);
        validComfirm = (Button) validFrame.findViewById(R.id.dialog_validation_confirm_btn);
        validCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(smsReceiver != null){
                    LoginActivity.this.unregisterReceiver(smsReceiver);
                    smsReceiver = null;
                }
                validFrame.setVisibility(View.INVISIBLE);
                registFrame.setVisibility(View.VISIBLE);
            }
        });

        validComfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = validCodeInputView.getText().toString();
                if(code != null && code.length() == 6 && Pattern.compile("[0-9]+").matcher(code).matches()){
                    if(smsReceiver != null){
                        LoginActivity.this.unregisterReceiver(smsReceiver);
                        smsReceiver = null;
                    }
                    if(countDownTimer != null){
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
                    comfirm(code);
                }
                else
                    showToast(LoginActivity.this, R.string.error_invalid_code, Toast.LENGTH_LONG);
            }
        });

        validResend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                restartTimer();
                MsgSender.get(SystemConstants.BASE_URL + SystemConstants.URL_SEND_CODE + gusername,
                        null, null, null);
            }
        });
    }

    private void initForgotLayer(FrameLayout content){
        forgotFrame = (RelativeLayout) View.inflate(this, R.layout.dialog_forgot_password, null);
        forgotAccountInput = (EditText) forgotFrame
                .findViewById(R.id.dialog_forgot_password_phone_input);
        forgotCodeInput = (EditText) forgotFrame.findViewById(R.id.dialog_forgot_password_code_input);
        forgotRDImageView = (ImageView) forgotFrame.findViewById(R.id.dialog_rdbitmap_img);
        forgotPasswordChangeCode = (TextView) forgotFrame.findViewById(R.id.dialog_rdbitmap_change);
        forgotDone = (Button) forgotFrame.findViewById(R.id.dialog_forgot_password_confirm_btn);
        forgotCancel = (Button) forgotFrame.findViewById(R.id.dialog_forgot_password_cancel_btn);
        content.addView(forgotFrame);
        forgotFrame.setVisibility(View.INVISIBLE);

        forgotDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = forgotAccountInput.getText().toString();
                if(!RDBitmapGenerator.getInstance().getCode().toLowerCase().equals(forgotCodeInput.getText().toString().toLowerCase())){
                    showToast(LoginActivity.this, R.string.error_invalid_code, Toast.LENGTH_LONG);
                    return;
                }
                if(account != null && UNAME_PATTERN.matcher(account).matches()){
                    MsgSender.get(SystemConstants.BASE_URL + SystemConstants.URL_SEND_CODE + account,
                            null, null, new AccountConnectionMsgListener(LoginActivity.this,
                                    IntentConstants.INTENT_ACTION_FORGOT_VALID));
                    showLoadingLayer();
                }
                else{
                    showToast(LoginActivity.this, R.string.error_malformed_username, Toast.LENGTH_LONG);
                }
            }
        });

        forgotCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotAccountInput.setText("");
                forgotCodeInput.setText("");
                forgotFrame.setVisibility(View.INVISIBLE);
                loginFrame.setVisibility(View.VISIBLE);
            }
        });

        forgotPasswordChangeCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotRDImageView.setImageBitmap(RDBitmapGenerator.getInstance().createBitmap());
            }
        });
    }

    /****
     * 初始化修改密码的短信验证界面
     */
    private void initForgotValidLayer(FrameLayout content){
        forgotValidFrame = (RelativeLayout) View.inflate(LoginActivity.this, R.layout.dialog_forgotpw_validation, null);
        forgotCodeSentTo = (TextView) forgotValidFrame.findViewById(R.id.dialog_forgotpw_validation_phone_number);
        forgotValidCodeInput = (EditText) forgotValidFrame.findViewById(R.id.dialog_forgotpw_validation_code_input);
        forgotSendCode = (Button) forgotValidFrame.findViewById(R.id.dialog_forgotpw_validation_resend);
        forgotValidCancl = (Button) forgotValidFrame.findViewById(R.id.dialog_forgotpw_validation_cancel_btn);
        forgotValidDone = (Button) forgotValidFrame.findViewById(R.id.dialog_forgotpw_validation_confirm_btn);
        content.addView(forgotValidFrame);
        forgotValidFrame.setVisibility(View.INVISIBLE);

        forgotSendCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = forgotAccountInput.getText().toString();
                MsgSender.get(SystemConstants.BASE_URL + SystemConstants.URL_SEND_CODE + account,
                        null, null, null);
                startForgotSendCountdown();
            }
        });

        forgotValidDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = forgotAccountInput.getText().toString();
                String code    = forgotValidCodeInput.getText().toString();
                int msgRes = checkAccountAndCode(account, code);
                if(msgRes != 0){
                    showToast(LoginActivity.this, msgRes, Toast.LENGTH_LONG);
                    return;
                }
                showLoadingLayer();
                tempUsername = account;
                MsgSender.get(SystemConstants.BASE_URL + SystemConstants.URL_FORGOT_PASS + account + "/" + code,
                        null, null,
                        new AccountConnectionMsgListener(LoginActivity.this, IntentConstants.INTENT_ACTION_FORGOT));
            }
        });

        forgotValidCancl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotAccountInput.setText("");
                forgotCodeInput.setText("");
                forgotValidCodeInput.setText("");
                forgotValidFrame.setVisibility(View.INVISIBLE);
                loginFrame.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startForgotSendCountdown(){
        if(forgotPassTimer != null)
            forgotPassTimer.cancel();
        forgotSendCode.setEnabled(false);
        forgotSendCode.setText("60秒后重新发送");
        forgotSendCode.setTextColor(getResources().getColor(R.color.light_black));
        forgotPassTimer = new Timer(false);
        forgotPassTimer.schedule(new TimerTask() {
            int counter = 60;

            @Override
            public void run() {
                if(counter > 0){
                    counter --;
                    Message message = new Message();
                    Bundle data = new Bundle();
                    data.putInt(MSG_COUNT_DOWN_COUNTER, counter);
                    message.setData(data);
                    message.what = MSG_UPDATE_FORGOT_COUNT_DOWN;
                    handler.sendMessage(message);
                }
                else{
                    forgotPassTimer.cancel();
                    handler.sendEmptyMessage(MSG_UPDATE_FORGOT_COUNT_DOWN_OVER);
                }
            }
        }, 1000, 1000);
    }

    private void initChangeLayer(FrameLayout content){
        changeFrame = (RelativeLayout) View.inflate(this, R.layout.dialog_change_password, null);
        changeNewInput = (EditText) changeFrame.findViewById(R.id.dialog_change_password_password_input);
        changeComfirmInput = (EditText) changeFrame.findViewById(R.id.dialog_change_password_comfirm_input);
        changeDone = (Button) changeFrame.findViewById(R.id.dialog_change_password_done_btn);
        changeCancel = (Button) changeFrame.findViewById(R.id.dialog_change_password_cancel_btn);
        content.addView(changeFrame);


        changeDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPass = changeNewInput.getText().toString();
                String comPass = changeComfirmInput.getText().toString();
                int resId = checkPassAndPass(newPass, comPass);
                if(resId != 0){
                    showToast(LoginActivity.this, resId, Toast.LENGTH_LONG);
                    return;
                }
                showLoadingLayer();
                ChangePassword cp = new ChangePassword(newPass, tempToken);
                MsgSender.post(SystemConstants.BASE_URL + SystemConstants.URL_CHANGE_PASS + tempUsername,
                               SystemConstants.KEY_JSON, cp.toDESJson(),
                               new AccountConnectionMsgListener(LoginActivity.this, IntentConstants.INTENT_ACTION_CHANGE));
            }
        });

        changeCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                changeNewInput.setText("");
                changeComfirmInput.setText("");
                changeFrame.setVisibility(View.INVISIBLE);
                loginFrame.setVisibility(View.VISIBLE);
            }
        });
    }

    private AlertDialog.Builder getDialogBuilder(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View content = View.inflate(this, R.layout.dialog_rdbitmap, null);
        dialogImg = (ImageView) content.findViewById(R.id.dialog_rdbitmap_img);
        dialogText = (EditText) content.findViewById(R.id.dialog_rdbitmap_edittext);
        dialogChange = (TextView) content.findViewById(R.id.dialog_rdbitmap_change);
        builder.setTitle(R.string.title_please_type_recaptcha)
               .setView(content)
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                   }
               });
        dialogChange.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogImg.setImageBitmap(generator.createBitmap());
            }
        });
        return builder;
    }

	/**
	 * Send a Login message to the background service by calling start service
	 * from the context
	 * 
	 * @param uname
	 * @param pword
	 * @return
	 */
	private void login(String uname, String pword) {

        //showToast(this, "login", Toast.LENGTH_LONG);

        gusername = uname;
        gpassword = pword;
        gapiKey = this.getSPString(SP_API_KEY, null);

        this.showLoadingLayer();
        Authentication auth = new Authentication(uname, pword);
        auth.setImei(gIMEI);
        auth.setNonce(RDStringGenerator.genNonce(7));
        if(gapiKey != null)
            auth.setApiKey(gapiKey);

        MsgSender.post(SystemConstants.BASE_URL + SystemConstants.URL_LOGIN,
                       SystemConstants.KEY_JSON, auth.toDESJson(),
                       new AccountConnectionMsgListener(this, IntentConstants.INTENT_ACTION_LOGIN));

    }

	private void regist(String regUsername, String regPassword) {
        lastRegUsername = new String(gusername);
        gusername = regUsername;
        gpassword = regPassword;
        showLoadingLayer();
        Registration reg = new Registration(regUsername, regPassword);
        MsgSender.post(SystemConstants.BASE_URL + SystemConstants.URL_REGISTER,
                       SystemConstants.KEY_JSON, reg.toDESJson(),
                       new AccountConnectionMsgListener(this, IntentConstants.INTENT_ACTION_REGISTER));
        return;
    }

    private int checkLogin(String username, String password){
        if(username == null)
            return R.string.error_empty_username;
        if(password == null)
            return R.string.error_empty_password;
        if(!UNAME_PATTERN.matcher(username).matches())
            return R.string.error_malformed_username;
        if(password.length() < 6 || password.length() > 16)
            return R.string.error_malformed_password;
        return 0;
    }

    private int checkRegistration(String username, String password, String comfirm){
        if(username == null)
            return R.string.error_empty_username;
        if(password == null)
            return R.string.error_empty_password;
        if(comfirm == null)
            return R.string.error_diff_pwords;
        if(!UNAME_PATTERN.matcher(username).matches()){
            return R.string.error_malformed_username;
        }
        if(password.length() < 6 || password.length() > 16){
            return R.string.error_malformed_password;
        }
        if(!password.equals(comfirm)){
            return R.string.error_diff_pwords;
        }
        if(!rulesCheckbox.isChecked())
            return R.string.error_protocol_agreement;
        return 0;
    }

    private int checkAccountAndCode(String account, String code){
        if(account == null)
            return R.string.error_empty_username;
        if(code == null)
            return R.string.error_empty_code;
        if(!UNAME_PATTERN.matcher(account).matches())
            return R.string.error_malformed_username;
        if(!CODE_PATTERN.matcher(code).matches())
            return R.string.error_malformed_code;
        return 0;
    }

    private int checkPassAndPass(String pass, String comfirm){
        if(pass == null)
            return R.string.error_empty_password;
        if(pass.length() < 6 || pass.length() > 16)
            return R.string.error_malformed_password;
        if(!pass.equals(comfirm))
            return R.string.error_diff_pwords;
        return 0;
    }

	private class LoginReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
            dismissLoadingLayer();
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				switch((IntentCode)bundle.get(IntentConstants.KEY_INTENT_CODE)){
				case INTENT_OK:{
					String body = bundle.getString(IntentConstants.KEY_INTENT_BODY);
					try {
						ReturnMsg msg = Parser.fromEncodedJson(body, ReturnMsg.class);
						if(msg.fail()){
							// 隐藏dialog
							showToast(LoginActivity.this, msg.getMsg(), Toast.LENGTH_LONG);
						}
						else{
                            setSharedPreferences(SP_LAST_ACCOUNT, gusername);

                            UserBasicInfo info = JsonUtil.fromJson(msg.getMsg(), UserBasicInfo.class);

                            // save account info
                            AccountDAO ads = new AccountDAO(LoginActivity.this);
                            Account account = new Account();
                            account.setAccountName(gusername);
                            account.setPassword(gpassword);
                            account.setStatus(info.getStatus());
                            account.setPrivilege(info.getPrivilege());
                            account.setToken(info.getToken());
                            account.setExpire(info.getExpire());
                            account.setIMEI(gIMEI);
                            account.setKey(info.getApiKey());
                            ads.addOrUpdate(account);
                            ads.close();
                            gapiToken = info.getToken();

                            // save device info
                            DeviceDAO dd = new DeviceDAO(LoginActivity.this);
                            Log.d("basic info:", info.toJson());
                            for(Device device: info.getDevices()){
                                device.setAccount(gusername);
                                dd.addOrUpdate(device);
                            }
                            dd.close();

                            Intent i = new Intent(LoginActivity.this, MsgCenterActivity.class);
							startActivity(i);
							LoginActivity.this.finish();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
				case INTENT_ERROR:{
					String msg = bundle.getString(IntentConstants.KEY_INTENT_MSG);
					showToast(LoginActivity.this, msg, Toast.LENGTH_LONG);
				    break;
                }
				}
			}
			else{
				showToast(LoginActivity.this, LoginActivity.this.getString(R.string.error_unknown), Toast.LENGTH_LONG);
			}
		}
	}

	private class RegisterReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
            dismissLoadingLayer();
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                switch((IntentCode)bundle.get(IntentConstants.KEY_INTENT_CODE)){
                    case INTENT_OK:{
                        String body = bundle.getString(IntentConstants.KEY_INTENT_BODY);

                        ReturnMsg msg = null;
                        try {
                            msg = Parser.fromEncodedJson(body, ReturnMsg.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(msg.fail()){
                            showToast(LoginActivity.this, msg.getMsg(), Toast.LENGTH_LONG);
                        }
                        else{
                            // 跳转到短信验证界面
                            registFrame.setVisibility(View.INVISIBLE);
                            validFrame.setVisibility(View.VISIBLE);
                            validTextView.setText("短信已发送到：" + gusername);

                            // 开始等待短信
                            if(smsReceiver!=null){
                                LoginActivity.this.unregisterReceiver(smsReceiver);
                            }
                            smsReceiver = new SmsReceiver();
                            IntentFilter filter = new IntentFilter(IntentConstants.INTENT_ACTION_SMS_RECV);
                            LoginActivity.this.registerReceiver(smsReceiver, filter);

                            if(lastRegUsername == null ||
                                    lastRegUsername.equals("") ||
                                    !gusername.equals(lastRegUsername)){
                                Log.d("login activity", "set resend button");
                                validResend.setText("60秒后重新发送");
                                validResend.setTextColor(LoginActivity.this.getResources().getColor(R.color.light_black));
                                restartTimer();
                            }
                        }
                        break;
                    }
                    case INTENT_ERROR:{
                        String msg = bundle.getString(IntentConstants.KEY_INTENT_MSG);
                        showToast(LoginActivity.this, msg, Toast.LENGTH_LONG);
                        break;
                    }
                }
            }
            else
                showToast(LoginActivity.this, LoginActivity.this.getString(R.string.error_unknown), Toast.LENGTH_LONG);
		}

	}

    // 监听确认消息
    private class ComfirmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            // remove the receiver
            LoginActivity.this.unregisterReceiver(this);
            comfirmReceiver = null;
            dismissLoadingLayer();

            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_COMFIRM)){


                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    switch((IntentCode)bundle.get(IntentConstants.KEY_INTENT_CODE)){
                        case INTENT_OK:{
                            String msg = bundle.getString(IntentConstants.KEY_INTENT_BODY);
                            LogUtils.d("msg received", msg);

                            ReturnMsg rm = null;
                            try {
                                rm = Parser.fromEncodedJson(msg, ReturnMsg.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                                showToast(LoginActivity.this, R.string.error_broken_data, Toast.LENGTH_LONG);
                                return;
                            }
                            if(rm == null){
                                showToast(LoginActivity.this, R.string.error_unknown, Toast.LENGTH_LONG);
                            }
                            else if(rm.fail()){
                                showToast(LoginActivity.this, rm.getMsg(), Toast.LENGTH_LONG);
                                registFrame.setVisibility(View.VISIBLE);
                                validFrame.setVisibility(View.INVISIBLE);
                            }
                            else{
                                UserBasicInfo info = JsonUtil.fromJson(rm.getMsg(), UserBasicInfo.class);
                                TelephonyManager tm = (TelephonyManager) LoginActivity.this.getSystemService(TELEPHONY_SERVICE);
                                gIMEI = tm.getDeviceId();
                                gapiToken = info.getToken();
                                Account account = new Account(gusername, gapiToken, gIMEI);
                                account.setStatus(info.getStatus());
                                account.setPrivilege(info.getPrivilege());
                                account.setPassword(gpassword);
                                account.setExpire(info.getExpire());
                                account.setKey(info.getApiKey());

                                // save the apiToken to db
                                AccountDAO ad = new AccountDAO(LoginActivity.this.getApplicationContext());
                                ad.addOrUpdate(account);
                                ad.close();

                                showToast(LoginActivity.this, R.string.reg_success, Toast.LENGTH_LONG);

                                //
                                validCodeInputView.setText("");
                                validFrame.setVisibility(View.INVISIBLE);
                                loginFrame.setVisibility(View.VISIBLE);
                            }
                            break;
                        }
                        case INTENT_ERROR:{
                            String msg = bundle.getString(IntentConstants.KEY_INTENT_MSG);
                            showToast(LoginActivity.this, msg, Toast.LENGTH_LONG);
                            break;
                        }
                    }
                }
            }
        }
    }

    private class ForgotPassReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_FORGOT)){
                dismissLoadingLayer();
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    switch((IntentCode)bundle.get(IntentConstants.KEY_INTENT_CODE)){
                        case INTENT_OK:{
                            String msg = bundle.getString(IntentConstants.KEY_INTENT_BODY);

                            ReturnMsg rm = null;
                            try {
                                rm = Parser.fromEncodedJson(msg, ReturnMsg.class);
                                if(rm.fail()){
                                    forgotCodeInput.setText("");
                                    showToast(LoginActivity.this, rm.getMsg(), Toast.LENGTH_LONG);
                                    return;
                                }
                                // 获取临时token
                                tempToken = rm.getMsg();
                                forgotValidFrame.setVisibility(View.INVISIBLE);
                                changeFrame.setVisibility(View.VISIBLE);
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                                showToast(LoginActivity.this, R.string.error_broken_data, Toast.LENGTH_LONG);
                                return;
                            }
                        }
                        case INTENT_ERROR:{
                            String msg = bundle.getString(IntentConstants.KEY_INTENT_MSG);
                            showToast(LoginActivity.this, msg, Toast.LENGTH_LONG);
                            break;
                        }
                    }
                }
            }
        }
    }

    private class ForgotPassValidReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_FORGOT_VALID)){
                dismissLoadingLayer();
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    switch((IntentCode)bundle.get(IntentConstants.KEY_INTENT_CODE)){
                        case INTENT_OK:{
                            String msg = bundle.getString(IntentConstants.KEY_INTENT_BODY);
                            ReturnMsg rm = null;
                            try {
                                rm = Parser.fromEncodedJson(msg, ReturnMsg.class);
                                if(rm.fail()){
                                    showToast(LoginActivity.this, rm.getMsg(), Toast.LENGTH_LONG);
                                }
                                else {
                                    forgotCodeSentTo.setText("验证短信已发送至:" + forgotAccountInput.getText().toString());
                                    forgotSendCode.setTextColor(LoginActivity.this.getResources().getColor(R.color.light_black));
                                    forgotFrame.setVisibility(View.INVISIBLE);
                                    forgotValidFrame.setVisibility(View.VISIBLE);
                                    startForgotSendCountdown();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showToast(LoginActivity.this, R.string.error_broken_data, Toast.LENGTH_LONG);
                                return;
                            }
                            break;
                        }
                        case INTENT_ERROR:{
                            String msg = bundle.getString(IntentConstants.KEY_INTENT_MSG);
                            showToast(LoginActivity.this, msg, Toast.LENGTH_LONG);
                            break;
                        }
                    }
                }
            }
        }
    }

    private class ChangePassReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_CHANGE)){
                dismissLoadingLayer();
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    switch((IntentCode)bundle.get(IntentConstants.KEY_INTENT_CODE)){
                        case INTENT_OK:{
                            String msg = bundle.getString(IntentConstants.KEY_INTENT_BODY);
                            LogUtils.d("msg received", msg);

                            ReturnMsg rm = null;
                            try {
                                rm = Parser.fromEncodedJson(msg, ReturnMsg.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                                showToast(LoginActivity.this, R.string.error_broken_data, Toast.LENGTH_LONG);
                                return;
                            }
                            if(rm.fail()){
                                changeNewInput.setText("");
                                changeComfirmInput.setText("");
                                forgotCodeInput.setText("");
                                changeFrame.setVisibility(View.INVISIBLE);
                                forgotValidFrame.setVisibility(View.VISIBLE);
                                showToast(LoginActivity.this, rm.getMsg(), Toast.LENGTH_LONG);
                                return;
                            }
                            forgotCodeInput.setText("");
                            changeNewInput.setText("");
                            changeComfirmInput.setText("");
                            changeFrame.setVisibility(View.INVISIBLE);
                            loginFrame.setVisibility(View.VISIBLE);
                            showToast(LoginActivity.this, R.string.change_success, Toast.LENGTH_LONG);
                            break;
                        }
                        case INTENT_ERROR:{
                            String msg = bundle.getString(IntentConstants.KEY_INTENT_MSG);
                            showToast(LoginActivity.this, msg, Toast.LENGTH_LONG);
                            break;
                        }
                    }
                }
            }
        }
    }

    // 监听短信
    private class SmsReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_SMS_RECV)){
                Bundle extras = intent.getExtras();
                if(extras != null){
                    //String from = extras.getSPString(IntentConstants.KEY_INTENT_SMS_FROM);
                    String body = extras.getString(IntentConstants.KEY_INTENT_SMS_BODY);
                    LogUtils.d(LoginActivity.class.getName(), body);
                    //long ts = extras.getLong(IntentConstants.KEY_INTENT_SMS_TS);
                    // 确认是否是验证短信
                    if(body != null && body.length() >= 6){
                        String code = body.substring(0, 6);
                        if(Pattern.compile("^([0-9]+).*").matcher(code).matches()){
                            validCodeInputView.setText(code);
                            comfirm(code);
                        }
                    }
                }
            }
        }
    }

    private void comfirm(String code){
        // remove sms receiver and stop timer
        if(smsReceiver != null){
            this.unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }
        if(countDownTimer != null){
            countDownTimer.cancel();
            countDownTimer = null;
        }

        // 将验证信息发送到服务器进行校验
        String nonce = RDStringGenerator.genNonce(7);
        Comfirm comfirm = new Comfirm();
        comfirm.setUsername(gusername);
        comfirm.setPassword(gpassword);
        comfirm.setApi_key(gapiKey);
        comfirm.setNonce(nonce);
        comfirm.setCode(code);

        showLoadingLayer();
        MsgSender.post(SystemConstants.BASE_URL + SystemConstants.URL_COMFIRM, SystemConstants.KEY_JSON, comfirm.toDESJson(),
                new AccountConnectionMsgListener(LoginActivity.this, IntentConstants.INTENT_ACTION_COMFIRM));

        if(comfirmReceiver != null){
            this.unregisterReceiver(comfirmReceiver);
            comfirmReceiver = null;
        }
        comfirmReceiver = new ComfirmReceiver();
        LoginActivity.this.registerReceiver(new ComfirmReceiver(),
                new IntentFilter(IntentConstants.INTENT_ACTION_COMFIRM));
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(loginFrame.isShown())
                LoginActivity.this.finish();
            else if(registFrame.isShown()){
                regUsernameInputView.setText("");
                regPasswordInputView.setText("");
                regPasswordToInputView.setText("");
                registFrame.setVisibility(View.INVISIBLE);
                loginFrame.setVisibility(View.VISIBLE);
            }else if(validFrame.isShown()){
                validCodeInputView.setText("");
                validFrame.setVisibility(View.INVISIBLE);
                registFrame.setVisibility(View.VISIBLE);
            }else if(forgotFrame.isShown()){
                forgotAccountInput.setText("");
                forgotCodeInput.setText("");
                forgotFrame.setVisibility(View.INVISIBLE);
                loginFrame.setVisibility(View.VISIBLE);
            }else if(forgotValidFrame.isShown()){
                forgotValidCodeInput.setText("");
                forgotValidFrame.setVisibility(View.INVISIBLE);
                forgotFrame.setVisibility(View.VISIBLE);
            }else if(changeFrame.isShown()){
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("警告")
                        .setMessage("是否放弃修改密码")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                forgotAccountInput.setText("");
                                forgotCodeInput.setText("");
                                forgotValidCodeInput.setText("");
                                changeFrame.setVisibility(View.INVISIBLE);
                                loginFrame.setVisibility(View.VISIBLE);
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                builder.create().show();
            }
            return true;
        }

        return false;
	}


	@Override
	public boolean handleMessage(Message msg) {
        switch(msg.what){
            case MSG_UPDATE_COUNT_DOWN:{
                String counter = msg.getData().getString(MSG_COUNT_DOWN_COUNTER);
                validResend.setText(counter + "秒后重新发送");
                break;
            }
            case MSG_COUNT_DOWN_OVER:{
                validResend.setText(R.string.resend_code);
                validResend.setEnabled(true);
                validResend.setTextColor(Color.WHITE);
                break;
            }
            case MSG_UPDATE_FORGOT_COUNT_DOWN:{
                int counter = msg.getData().getInt(MSG_COUNT_DOWN_COUNTER);
                forgotSendCode.setText(counter + "秒后重新发送");
                break;
            }
            case MSG_UPDATE_FORGOT_COUNT_DOWN_OVER:{
                forgotSendCode.setEnabled(true);
                forgotSendCode.setText(R.string.resend_code);
                forgotSendCode.setTextColor(Color.WHITE);
                break;
            }
        }
		return false;
	}

    private void restartTimer(){
        if(countDownTimer != null)
            countDownTimer.cancel();
        validResend.setEnabled(false);
        validResend.setTextColor(getResources().getColor(R.color.light_black));
        countDownTimer = new Timer(false);
        countDownTimer.schedule(new TimerTask() {
            int start = 60;
            @Override
            public void run() {
                if(start > 0){
                    start --;
                    Message m = new Message();
                    m.what = MSG_UPDATE_COUNT_DOWN;
                    Bundle b = new Bundle();
                    b.putString(MSG_COUNT_DOWN_COUNTER, start + "");
                    m.setData(b);
                    handler.sendMessage(m);
                }else{
                    countDownTimer.cancel();
                    countingDown = false;
                    handler.sendEmptyMessage(MSG_COUNT_DOWN_OVER);
                    // countDownTimer = null;
                }
            }
        }, 1000, 1000);
        countingDown = true;
    }
}
