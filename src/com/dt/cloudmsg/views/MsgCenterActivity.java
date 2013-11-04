package com.dt.cloudmsg.views;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.preference.PreferenceManager;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.adapter.MsgListAdapter;
import com.dt.cloudmsg.adapter.ServerListAdapter;
import com.dt.cloudmsg.communications.MessageCenterMsgListener;
import com.dt.cloudmsg.communications.MsgSender;
import com.dt.cloudmsg.component.ImageBtSingle;
import com.dt.cloudmsg.component.ImageBtSpinner;
import com.dt.cloudmsg.datasource.ContactsSource;
import com.dt.cloudmsg.datasource.ServerSource;
import com.dt.cloudmsg.datasource.MsgListSource;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.dt.cloudmsg.model.Device;
import com.dt.cloudmsg.model.MsgListEntity;
import com.dt.cloudmsg.service.MyService;
import com.dt.cloudmsg.util.IntentConstants;
import com.dt.cloudmsg.util.StringUtil;
import com.dt.cloudmsg.util.SystemConstants;

public class MsgCenterActivity extends BaseActivity {

    private static final String SP_KEY_FIRST = "msg_center_first";

    protected static final int STATE_SEARCH = 0x01;
    protected static final int STATE_NORMAL = 0x02;
    protected static final int START_SERVICE = 0x01;

    //private CommandReceiver commandReceiver;

    private boolean needInit;
    //private boolean isServiceExist;
    private ServerSource serverSource;
    private MsgListSource msgListSource;
    private ContactsSource contactsSource;
    private int functionState = STATE_NORMAL;
    private long exitTime;
    private Device current;

    private int selected = -1;
    private String selected_imei;
    private SelfOptionHolder selfHolder;

    private FrameLayout main_content;
    private FrameLayout top_content;
    private FrameLayout function_content;
    private FrameLayout content_frame;
    private FrameLayout dashboard;
    private LinearLayout searchLayer;

    private PopupWindow sever_switch_dropdown;

    private RelativeLayout top_frame;
    private RelativeLayout function_frame;

    private LinearLayout server_selector;
    private FrameLayout  self_option;
    private ListView     server_list;
    private ServerListAdapter serverListAdapter;
    private LinearLayout operation_selector;
    private LinearLayout make_call;
    private LinearLayout switch_sever;
    private LinearLayout add_contact;
    private LinearLayout delete_msg;
    private LinearLayout set_black;

    private EditText search_txt;
    private EditText filter_txt;

    private ImageBtSpinner server_switch;
    private ImageBtSingle config_button;
    private ImageBtSingle function_button;
    private ImageBtSingle restore_button;

    private MsgListAdapter msgListAdapter;

    private ListView msgList;
    private ListView searchList;


    private BindOrUpdateDeviceReceiver bindReceiver = null;
    private MsgReceiver msgReceiver = null;
    private NotificationReceiver noteReceiver = null;

    private SharedPreferences sp;

    private IntentFilter serviceInitFilter;
    private BroadcastReceiver serviceInitReceiver;
    private BroadcastReceiver syncDeviceReceiver;

    private TextView dashboardNum;
    private TextView dashboardOnOff;
    private TextView dashboardSmsSync;
    private TextView dashboardSmsReply;
    private TextView dashboardCallSync;
    private TextView dashboardCallReply;
    private LinearLayout dashboardConfig;
    private Button dashboardSwitcher;
    private ImageView selfSpan;

    public static final int RESULT_CODE_SETTING = 1;
    public static final int RESULT_CODE_CHAT    = 2;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Log.d("gusername", gusername);

        this.setContentView(R.layout.msg_center);
        // 初始化界面
        initView();

        sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        serviceInitReceiver = new ServiceInitializationReceiver();
        syncDeviceReceiver = new SyncDeviceReceiver();
        this.registerReceiver(serviceInitReceiver, new IntentFilter(IntentConstants.INTENT_ACTION_SVC_INIT));
        this.registerReceiver(syncDeviceReceiver, new IntentFilter(IntentConstants.INTENT_ACTION_UPDATE_SERVER));

        if (!SPExists(SP_KEY_FIRST + gusername)) {
            startSettingsActivity(true);
            Toast.makeText(getApplicationContext(), "第一次登录，请先配置",
                    Toast.LENGTH_SHORT).show();
        }
        else if(!MyService.isRunning()){
            startMyService();
            Log.d("service not running", "");
        }
    }


    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        this.unregisterReceiver(serviceInitReceiver);
        this.unregisterReceiver(syncDeviceReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int ResultCode, Intent data) {

        super.onActivityResult(requestCode, ResultCode, data);
        switch (ResultCode){
            case RESULT_CODE_SETTING:{
                // initDashboard();
                // 若配置有改变
                if (data.getBooleanExtra("isChanged", true)) {
                    setSharedPreferences(SP_KEY_FIRST + gusername, false);
                    Toast.makeText(getApplicationContext(), "配置已更新", Toast.LENGTH_SHORT).show();
                }
                // 察看服务是否开启
                if(!MyService.isRunning()){
                    startMyService();
                }else {
                    postInit();
                }
                break;
            }
            case RESULT_CODE_CHAT:{
                data.setAction(IntentConstants.INTENT_ACTION_UPDATE_MSGLIST);
                sendBroadcast(data);
                break;
            }
        }
    }

    // 开启配置界面
    private void startSettingsActivity(boolean first){
        Intent intent = new Intent(MsgCenterActivity.this, SettingActivity.class);

        intent.putExtra(IntentConstants.KEY_INTENT_SVC_UNAME, gusername);
        intent.putExtra(IntentConstants.KEY_INTENT_IEMI, gIMEI);
        intent.putExtra(IntentConstants.KEY_INTENT_MSG_SET_FIRST, first);
        intent.putExtra(IntentConstants.KEY_INTENT_TOKEN, gapiToken);
        startActivityForResult(intent, RESULT_CODE_SETTING);
    }

    // 开启后台的服务
    private void startMyService(){
        // tell service current username
        Intent intent = new Intent(MsgCenterActivity.this, MyService.class);
        intent.putExtra(IntentConstants.KEY_INTENT_SVC_UNAME, gusername);
        this.startService(intent);

        // 等待后台初始化
        showLoadingLayer();
    }

    private void initView() {
        main_content = (FrameLayout) findViewById(R.id.main_layout);
        top_content = (FrameLayout) findViewById(R.id.top_layout);
        function_content = (FrameLayout) findViewById(R.id.function_layout);
        content_frame = (FrameLayout) findViewById(R.id.msg_center_content_frame);
        msgList = (ListView) findViewById(R.id.msg_center_listview);
        dashboard = (FrameLayout) findViewById(R.id.msg_center_self_dashboard);
        initLoadingLayer(main_content); // 初始化loading窗口

        initTopLayer();
        initFunctionLayer();
        //initDashboard();
        initSearchLayer();
        initServerSelector(main_content);

        initOperationSelector(main_content);
        registerForContextMenu(main_content);

        msgList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MsgListEntity entity = (MsgListEntity) msgListAdapter.getItem(i);
                String account = entity.getAccount();
                String target  = entity.getComNumber();
                String source  = entity.getSource();

                Intent intent = new Intent(MsgCenterActivity.this, ConversationActivity.class);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_ACCOUNT, account);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_SOURCE, source);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_TARGET, target);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_NAME, entity.getComname());
                startActivityForResult(intent, RESULT_CODE_CHAT);
            }
            
        });
        msgList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                MsgListEntity entity = (MsgListEntity) msgListAdapter.getItem(i);
                String account = entity.getAccount();
                String target  = entity.getComNumber();
                String source  = entity.getSource();
                boolean isContact = entity.getComname()==null;
                showOperationSelector(entity.getComname(), target, isContact);
                return true;
            }
		});
    }


    /***
     * 初始化最顶栏
     */
    private void initTopLayer() {

        top_frame = (RelativeLayout) View.inflate(this, R.layout.msg_center_top, null);
        top_content.addView(top_frame);

        server_switch = (ImageBtSpinner) findViewById(R.id.number_selector);
        server_switch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showServerSelector();
            }
        });

        config_button = (ImageBtSingle) findViewById(R.id.config_btn);
        config_button.setImageResource(R.drawable.configure);
        config_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingsActivity(false);
            }
        });

    }

    private void initDashboard(){
        dashboardNum = (TextView) findViewById(R.id.msg_center_self_num);
        dashboardOnOff = (TextView) findViewById(R.id.msg_center_self_status);
        dashboardConfig = (LinearLayout) findViewById(R.id.msg_center_self_config);
        dashboardSmsSync = (TextView) findViewById(R.id.msg_center_self_msg_onoff);
        dashboardSmsReply = (TextView) findViewById(R.id.msg_center_self_msg_reply);
        dashboardCallSync = (TextView) findViewById(R.id.msg_center_self_call_onoff);
        dashboardCallReply = (TextView) findViewById(R.id.msg_center_self_call_reply);
        dashboardSwitcher = (Button) findViewById(R.id.msg_center_self_onoffbtn);
        dashboardNum.setText("当前设备:" + sp.getString(SettingActivity.SP_KEY_LOCALHOST_NUM + gusername, "未知"));
        if(sp.getBoolean(SettingActivity.SP_KEY_SERVER_ON_OFF + gusername, false)){
            dashboardOnOff.setText("同步已开启");
            dashboardOnOff.setTextColor(getResources().getColor(R.color.msg_blue));
            dashboardSwitcher.setBackgroundResource(R.drawable.button_single_red);
            dashboardSwitcher.setText("关闭同步服务");
            dashboardSmsSync.setText("短信同步:开启");
            dashboardCallSync.setText("来电同步:开启");
        }
        else{
            dashboardOnOff.setText("同步已关闭");
            dashboardOnOff.setTextColor(getResources().getColor(R.color.dark_gray));
            dashboardSwitcher.setBackgroundResource(R.drawable.button_single_blue);
            dashboardSwitcher.setText("开启同步服务");
            dashboardSmsSync.setText("短信同步:关闭");
            dashboardCallSync.setText("来电同步:关闭");
        }
        if(sp.getBoolean(SettingActivity.SP_KEY_SERVER_MSG_ON_OFF + gusername, false)){
            dashboardSmsReply.setText("短信回执:开启");
        }
        else {
            dashboardSmsReply.setText("短信回执:关闭");
        }

        if(sp.getBoolean(SettingActivity.SP_KEY_SERVER_CALL_ON_OFF + gusername, false)){
            dashboardCallReply.setText("来电回执:开启");
        }
        else {
            dashboardCallReply.setText("来电回执:关闭");
        }

        dashboardConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startSettingsActivity(false);
            }
        });

        final boolean current = sp.getBoolean(SettingActivity.SP_KEY_SERVER_ON_OFF + gusername, false);
        dashboardSwitcher.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                SharedPreferences.Editor editor = sp.edit();
//                editor.putBoolean(SettingActivity.SP_KEY_SERVER_ON_OFF + gusername, !current);
//                Intent intent = new Intent()
            }
        });
    }

    /***
     * 初始化功能栏
     */
    private void initFunctionLayer() {
        function_frame = (RelativeLayout) View.inflate(this,
                R.layout.msg_center_function, null);
        function_content.addView(function_frame);

        function_button = (ImageBtSingle) function_frame.findViewById(R.id.function_btn);
        function_button.setImageResource(R.drawable.new_msg);
        function_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected != -1){
                    Intent intent = new Intent(MsgCenterActivity.this, ContactChooseActivity.class);
                    Device dev = (Device) serverListAdapter.getItem(selected);
                    Log.d("selected number:", dev.getNumber());
                    intent.putExtra(IntentConstants.KEY_INTENT_MSG_CONTACT_SERVER, dev.getNumber());
                    intent.putExtra(IntentConstants.KEY_INTENT_MSG_CONTACT_IMEI, dev.getImei());
                    intent.putExtra(IntentConstants.KEY_INTENT_MSG_CONTACT_STATUS, dev.getStatus());
                    startActivity(intent);
                }
                if(serverSource.size() <= 0){
                    // 告知用户无法发送

                }
            }
        });

        search_txt = (EditText) function_frame.findViewById(R.id.msg_center_search_txt);
        search_txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && selected != -1)// 如果组件获得焦点
                    setFunctionState(STATE_SEARCH);
                	search_txt.clearFocus();
            }
        });
        search_txt.clearFocus();
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(search_txt.getWindowToken(), 0);
    }

    private void initSearchLayer(){
    	
        searchLayer = (LinearLayout) this.findViewById(R.id.msg_center_search_layer);
        filter_txt = (EditText) searchLayer.findViewById(R.id.msg_center_search_layer_txt);
        restore_button = (ImageBtSingle) searchLayer.findViewById(R.id.msg_center_search_layer_function_btn);
        searchList = (ListView) searchLayer.findViewById(R.id.msg_center_search_layer_list);

        filter_txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // TODO
            	String str = filter_txt.getText().toString();
                if(str != null && str.length() > 0){
                	msgListSource.filter(str);
                }else{
                	msgListSource.cancelFilter();
                }
            }
        });
        //filter_txt.setFocusable(false);
        filter_txt.clearFocus();

        restore_button.setImageResource(R.drawable.cancel);
        restore_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setFunctionState(STATE_NORMAL);
            }
        });

        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            	MsgListEntity entity = (MsgListEntity) msgListAdapter.getItem(i);
                String account = entity.getAccount();
                String target  = entity.getComNumber();
                String source  = entity.getSource();

                Intent intent = new Intent(MsgCenterActivity.this, ConversationActivity.class);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_ACCOUNT, account);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_SOURCE, source);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_TARGET, target);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_NAME, entity.getComname());
                startActivityForResult(intent, RESULT_CODE_CHAT);
            }
        });
        searchList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                MsgListEntity entity = (MsgListEntity) msgListAdapter.getItem(i);
                String account = entity.getAccount();
                String target  = entity.getComNumber();
                String source  = entity.getSource();
                boolean isContact = entity.getComname()==null;
                showOperationSelector(entity.getComname(), target,isContact);
                return true;
            }
		});
    }

    private void setFunctionState(int state) {
        functionState = state;
        switch (state){
            case STATE_SEARCH:{
                searchLayer.setVisibility(View.VISIBLE);
                filter_txt.setFocusable(true);
                filter_txt.requestFocus();
                // 自动弹出软键盘
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                searchList.setAdapter(msgListAdapter);
                break;
            }
            case STATE_NORMAL:{
            	msgListSource.cancelFilter();
                filter_txt.setText("");
                searchLayer.setVisibility(View.INVISIBLE);
                filter_txt.clearFocus();
                // 隐藏软键盘
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(filter_txt.getWindowToken(), 0);
                break;
            }
        }
    }

    /***
     * 初始化服务器选择面板
     */
    private void initServerSelector(final FrameLayout container){
        server_selector = (LinearLayout) View.inflate(this, R.layout.msg_center_sever_selector, null);
        self_option = (FrameLayout) server_selector.findViewById(R.id.msg_center_server_selector_self_option);
        server_list = (ListView) server_selector.findViewById(R.id.msg_center_sever_list);
        selfSpan = (ImageView) server_selector.findViewById(R.id.meg_center_server_selector_self_divider);
        container.addView(server_selector);

        server_selector.setVisibility(View.INVISIBLE);
        server_selector.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissServerSelector();
            }
        });

        server_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(selected == i){
                    server_selector.setVisibility(View.INVISIBLE);
                }
                else{
                    selected = i;
                    server_selector.setVisibility(View.INVISIBLE);
                    Device dev = (Device) serverListAdapter.getItem(selected);
                    server_switch.setTextViewText(dev.getAlias());
                    switch (dev.getStatus()){
                        case Device.STATUS_ONLINE:
                            server_switch.setStatus(ImageBtSpinner.SERVER_ONLINE);
                            break;
                        case Device.STATUS_OFFLINE:
                            server_switch.setStatus(ImageBtSpinner.SERVER_OFFLINE);
                            break;
                        case Device.STATUS_ERROR:
                            server_switch.setStatus(ImageBtSpinner.SERVER_UNUSUAL);
                            break;
                    }
                    // TODO reset msglistadapter
                    Device device = (Device) serverListAdapter.getItem(i);
                    msgListSource = MyService.getMsgListSource(device.getNumber());
                    if(msgListAdapter != null)
                        msgListAdapter.unregister();
                    msgListAdapter = new MsgListAdapter(MsgCenterActivity.this, msgListSource);
                    msgList.setAdapter(msgListAdapter);
                    server_switch.setTextViewText(device.getAlias());
                    dashboard.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
    private void initOperationSelector(final FrameLayout container){
    	operation_selector = (LinearLayout) View.inflate(this, R.layout.msg_center_operation_selector, null);
    	make_call = (LinearLayout) operation_selector.findViewById(R.id.msg_center_operation_make_call);
    	switch_sever = (LinearLayout) operation_selector.findViewById(R.id.msg_center_operation_switch_sever);
    	add_contact = (LinearLayout) operation_selector.findViewById(R.id.msg_center_operation_add_contact);
    	delete_msg = (LinearLayout) operation_selector.findViewById(R.id.msg_center_operation_delete_msg);
    	set_black = (LinearLayout) operation_selector.findViewById(R.id.msg_center_operation_set_black);
    	container.addView(operation_selector);
    	operation_selector.setVisibility(View.INVISIBLE);
    }

    private void showServerSelector(){
        server_selector.setVisibility(View.VISIBLE);
    }

    private void dismissServerSelector(){
        server_selector.setVisibility(View.INVISIBLE);
    }
    
    private void showOperationSelector(final String targetname, final String target,boolean isContact){
    	operation_selector.setVisibility(View.VISIBLE);
    	operation_selector.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            	dismissOperationSelector();
            }
        });
    	make_call.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            	try {
                    operation_selector.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + target));
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("SampleApp", "Failed to invoke call", e);
                }
            }
        });
    	switch_sever.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (target != null&& !target.equals("")) {
                    operation_selector.setVisibility(View.INVISIBLE);
					// 调用系统短信界面
					Uri smsToUri = Uri.parse("smsto:"+ target);
					Intent intent = new Intent(Intent.ACTION_SENDTO,smsToUri);
					startActivity(intent);
				}
            }
        });
    	if(!isContact){
    		add_contact.setVisibility(View.VISIBLE);
    		add_contact.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
    	}else{
    		add_contact.setVisibility(View.GONE);
    	}
    	delete_msg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = "";
                if(targetname != null){
                    msg = "确认要删除与" + targetname + "的会话吗?";
                }else if(target != null){
                    msg = "确认要删除与" + target + "的会话吗?";
                }else
                    msg = "确认要删除该会话吗?";

                AlertDialog.Builder builder = new AlertDialog.Builder(MsgCenterActivity.this)
                                                    .setTitle("警告")
                                                    .setMessage(msg)
                                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                            operation_selector.setVisibility(View.INVISIBLE);
                                                            Intent intent = new Intent(IntentConstants.INTENT_ACTION_DELETE_SESSION);
                                                            Device device = (Device) serverListAdapter.getItem(selected);
                                                            intent.putExtra(IntentConstants.KEY_INTENT_SVC_SOURCE, device.getNumber());
                                                            intent.putExtra(IntentConstants.KEY_INTENT_SVC_TARGET, target);
                                                            sendBroadcast(intent);
                                                        }
                                                    })
                                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                            operation_selector.setVisibility(View.INVISIBLE);
                                                        }
                                                    });
                builder.create().show();
            }
        });

        set_black.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                operation_selector.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(IntentConstants.INTENT_ACTION_SET_BLACK);
                sendBroadcast(intent);
            }
        });
    }
    private void dismissOperationSelector(){
    	operation_selector.setVisibility(View.INVISIBLE);
    	
    }

    private void setMsgLsit() {

    }

    private boolean isOnServer() {
        return sp.getBoolean(SettingActivity.SP_KEY_SERVER_ON_OFF + gusername, false)
                || MyService.isBound(gusername, gIMEI);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case START_SERVICE: {
                //startService();
                return true;
            }
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * add()方法的四个参数，依次是： 1、组别，不分组就Menu.NONE, 2、Id，Android根据这个Id来确定不同的菜单
		 * 3、顺序，菜单现在在前面由这个参数的大小决定 4、文本，菜单的显示文本
		 */
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "退出").setIcon(
                android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                SharedPreferences settings = PreferenceManager
                        .getDefaultSharedPreferences(this);

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("HAVE_LOG_IN", false);
                editor.commit();
                // TODO Auto-generated method stub

                finish();
                System.exit(0);
                break;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 获取
            // back键
            if (operation_selector.isShown()){
                operation_selector.setVisibility(View.INVISIBLE);
                return true;
            }else if(server_selector.isShown()){
                server_selector.setVisibility(View.INVISIBLE);
                return true;
            }
            else if (functionState == STATE_SEARCH) { // 如果是search模式 ，则先恢复普通模式
                setFunctionState(STATE_NORMAL);
                return true;
            } else if ((System.currentTimeMillis() - exitTime) > 2000) { // 连按两次退出
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                // 关闭service
                Intent intent = new Intent(MsgCenterActivity.this, MyService.class);
                this.stopService(intent);

                finish();
                // System.exit(0);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU
                && event.getRepeatCount() == 0) {
            if (functionState == STATE_SEARCH) { // search模式下，禁止menu呼出
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);

    }

    /****
     *
     */
    private class BindOrUpdateDeviceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    private class NotificationReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    private class MsgReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    private class ServiceInitializationReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_SVC_INIT)){
                postInit();
                dismissLoadingLayer();
            }
        }
    }

    private void postInit(){

        //filter_txt.setFocusable(true);

        // 初始化dashboard
        initDashboard();

        // 初始化server list
        serverSource = MyService.getServerSource();
        Log.d("本机IMEI", gIMEI);
        serverListAdapter = new ServerListAdapter(this, serverSource, isOnServer(), gIMEI, gusername);
        server_list.setAdapter(serverListAdapter);

        // 若开启服务器，显示本地状态
        if(isOnServer()){
            // -1表示本机
            selected = -1;
            server_switch.setTextViewText(R.string.localhost);
            server_switch.setStatus(ImageBtSpinner.SERVER_LOCAL);
            setSelfOption();
            dashboard.setVisibility(View.VISIBLE);
        }
        else if(serverSource.size() > 0){
            Log.d("post init", "");
            // 默认选中第一台设备
            Device dev = (Device) serverListAdapter.getItem(0);
            selected = 0;
            selected_imei = dev.getImei();
            server_switch.setTextViewText(dev.getAlias());
            switch (dev.getStatus()){
                case Device.STATUS_ONLINE:
                    server_switch.setStatus(ImageBtSpinner.SERVER_ONLINE);
                    break;
                case Device.STATUS_OFFLINE:
                    server_switch.setStatus(ImageBtSpinner.SERVER_OFFLINE);
                    break;
                case Device.STATUS_ERROR:
                    server_switch.setStatus(ImageBtSpinner.SERVER_UNUSUAL);
                    break;
            }
            msgListSource = MyService.getMsgListSource(dev.getNumber());
            msgListAdapter = new MsgListAdapter(this, msgListSource);
            msgList.setAdapter(msgListAdapter);
            msgListAdapter.notifyDataSetChanged();
            dashboard.setVisibility(View.INVISIBLE);

            switch (dev.getStatus()){
                case Device.STATUS_ONLINE:{
                    break;
                }
                case Device.STATUS_OFFLINE:{
                    break;
                }
                case Device.STATUS_ERROR:{
                    break;
                }
            }
        }
        else{
            dashboard.setVisibility(View.INVISIBLE);
        }
    }

    /***
     * 接收设备同步广播
     */
    private class SyncDeviceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_UPDATE_SERVER)){
                if(selected >= 0 && serverSource.size() > 0){
                    int size = serverSource.size();
                    boolean found = false;
                    for(int i = 0; i < size; i ++){
                        Device dev = serverSource.get(i);
                        if(dev.getImei().equals(selected_imei)){
                            found = true;
                            selected = i;
                            server_switch.setTextViewText(dev.getAlias());
                        }
                    }
                    // 刚才选中的服务器已经不存在了
                    if(!found){
                        // 若本机是服务器，则显示本机信息
                        if(isOnServer()){
                            selected = -1;
                            selected_imei = null;
                            server_switch.setTextViewText(R.string.localhost);
                            dashboard.setVisibility(View.VISIBLE);
                            if(selfHolder == null){
                                setSelfOption();
                            }
                        }
                        // 若本机不是服务器，则现实第一台服务器的信息
                        else if(serverSource.size() > 0){
                            // 选中第一台设备
                            Device dev = serverSource.get(0);
                            selected = 0;
                            selected_imei = dev.getImei();
                            server_switch.setTextViewText(dev.getAlias());
                            msgListSource = MyService.getMsgListSource(dev.getNumber());
                            if(msgListAdapter != null) msgListAdapter.unregister();
                            msgListAdapter = new MsgListAdapter(MsgCenterActivity.this, msgListSource);
                            msgList.setAdapter(msgListAdapter);
                            dashboard.setVisibility(View.INVISIBLE);
                        }
                        // 否则，什么都不显示
                        else if(selfHolder != null){
                            removeSelfOption();
                            server_switch.setTextViewText("");
                            selected_imei = null;
                            selected = -1;
                        }
                    }
                    // 若刚才的服务器还存在，察看本机是否是服务器
                    else{
                        if(isOnServer() && selfHolder == null){
                            setSelfOption();
                        }
                        else if(!isOnServer() && selfHolder != null){
                            removeSelfOption();
                        }
                    }
                }
                else if(!isOnServer() && serverSource.size() < 0){
                    server_switch.setTextViewText("您尚未绑定任何设备");
                }
            }
        }
    }
//
//    private void setServerSwitch(){
//        if(selected == -1){
//            server_switch.setTextViewText(R.string.localhost);
//            server_switch.setStatus(ImageBtSpinner.SERVER_LOCAL);
//        }
//        else{
//
//        }
//    }

    private class SelfOptionHolder{
        ImageView hasMsg;
        TextView  name;
        ImageView status;
        
    }

    private void setSelfOption(){
        RelativeLayout self = (RelativeLayout) View.inflate(MsgCenterActivity.this, R.layout.item_sever_selector, null);
        ImageView msg = (ImageView) self.findViewById(R.id.server_selector_have_msg);
        TextView name = (TextView) self.findViewById(R.id.server_selector_option_number);
        ImageView status = (ImageView) self.findViewById(R.id.server_selector_option_icon);
        name.setText(R.string.localhost);
        selfHolder = new SelfOptionHolder();
        selfHolder.hasMsg = msg;
        selfHolder.name = name;
        selfHolder.status = status;

        self.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                selected = -1;
                selected_imei = null;
                dashboard.setVisibility(View.VISIBLE);
                server_selector.setVisibility(View.INVISIBLE);
                server_switch.setTextViewText(R.string.localhost);
                server_switch.setStatus(ImageBtSpinner.SERVER_LOCAL);
            }
        });
        self_option.addView(self);
        selfSpan.setVisibility(View.VISIBLE);
    }

    private void removeSelfOption(){
        self_option.removeAllViews();
        selfHolder = null;
        selfSpan.setVisibility(View.INVISIBLE);
        if(selected == -1){
            if(serverListAdapter.getCount() > 0){
                Device dev = serverSource.get(0);
                selected = 0;
                selected_imei = dev.getImei();
                server_switch.setTextViewText(dev.getAlias());
                msgListSource = MyService.getMsgListSource(dev.getNumber());
                if(msgListAdapter != null) msgListAdapter.unregister();
                msgListAdapter = new MsgListAdapter(MsgCenterActivity.this, msgListSource);
                msgList.setAdapter(msgListAdapter);
                dashboard.setVisibility(View.INVISIBLE);
            }
            else{
                server_switch.setTextViewText("");
                server_switch.setItemNum(0);
            }
        }
    }
}
