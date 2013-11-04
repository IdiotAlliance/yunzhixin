package com.dt.cloudmsg.views;

import java.util.ArrayList;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.adapter.ChatMsgViewAdapter;
import com.dt.cloudmsg.beans.MessageBean;
import com.dt.cloudmsg.component.ImageBtSingle;
import com.dt.cloudmsg.component.PullToRefreshListView;
import com.dt.cloudmsg.datasource.ChatMsgSource;
import com.dt.cloudmsg.datasource.ServerSource;
import com.dt.cloudmsg.model.ChatMsgEntity;
import com.dt.cloudmsg.service.MyService;
import com.dt.cloudmsg.util.HandleTimer;
import com.dt.cloudmsg.util.IntentConstants;
import com.dt.cloudmsg.util.WidgetsUtil;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ConversationActivity extends BaseActivity {

    private static final String TAG = ConversationActivity.class.getName();

    private ChatMsgSource chatMsgSource;
    private ServerSource serverSource;
	private String newestMsg;
	private ArrayList<String> infoList;

    private String account;
    private String source;
    private String target;
    private String name;
    private ChatMsgEntity selectedEntity;

	private RelativeLayout main_content;

	private FrameLayout top_content;
	private FrameLayout info_content;
	private FrameLayout function_content;

	private RelativeLayout top_frame;
	private RelativeLayout info_frame;
	private RelativeLayout function_frame;
    private AbsoluteLayout operation_container;
    private LinearLayout operation_panel;

	private EditText message_txt;

	private ImageBtSingle conv_back;
	private ImageBtSingle sever_switch;
	private ImageBtSingle make_call;
	private ImageBtSingle add_attachment;
	private ImageBtSingle send_message;
    private TextView serverNumber;
    private TextView serverName;
    private TextView deleteMsg;
    private TextView copyContent;
    private TextView viewDetail;

    private ChatMsgViewAdapter chatMsgViewAdapter;
	private PullToRefreshListView msgList;
    private ListView serverList;// 主机列表;
	
	private HandleTimer infoTimer;
    private BroadcastReceiver scrollReceiver;
    private BroadcastReceiver onItemLongClickedReceiver;

    public static final int MAKE_CALL = 0x00;
    public static final int SEVER_SWITCH = 0x01;
    
    public static final String THIS_IS_TARGET = "this_is_target";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.conv_activity);

        Intent intent = this.getIntent();
        this.account = intent.getStringExtra(IntentConstants.KEY_INTENT_MSG_CHAT_ACCOUNT);
        this.target  = intent.getStringExtra(IntentConstants.KEY_INTENT_MSG_CHAT_TARGET);
        this.source  = intent.getStringExtra(IntentConstants.KEY_INTENT_MSG_CHAT_SOURCE);
        this.name    = intent.getStringExtra(IntentConstants.KEY_INTENT_MSG_CHAT_NAME);

		initView();

		// initInfrastructure();
		initListener();

		// / Init controller and datasource
		initInfrastructure();

        scrollReceiver = new ScrollToBottomReceiver();
        onItemLongClickedReceiver = new OnItemLongClickReceiver();
        this.registerReceiver(scrollReceiver, new IntentFilter(IntentConstants.INTENT_ACTION_SCROLL_TO_BOTTOM));
        this.registerReceiver(onItemLongClickedReceiver, new IntentFilter(IntentConstants.INTENT_ACTION_MSG_LONG_CLICKED));
        handler = new Handler(this);
	}


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(scrollReceiver != null) this.unregisterReceiver(scrollReceiver);
        if(onItemLongClickedReceiver != null) this.unregisterReceiver(onItemLongClickedReceiver);
    }


    public void initView() {
		main_content = (RelativeLayout) findViewById(R.id.conv_activity_main_layout);

		function_content = (FrameLayout) findViewById(R.id.conv_activity_function_layout);
		top_content = (FrameLayout) findViewById(R.id.conv_activity_top_layout);
		info_content = (FrameLayout) findViewById(R.id.conv_activity_info_layout);
		

		initFunctionLayer();
		initTopLayer();
		initInfoLayer();
		initMsgList();
        initOperationPanel();

		registerForContextMenu(main_content);
	}

	private void initTopLayer() {
		top_frame = (RelativeLayout) View.inflate(this,
				R.layout.conv_activity_top, null);
		top_content.addView(top_frame);

		conv_back = (ImageBtSingle) findViewById(R.id.conv_top_back_btn);
		conv_back.setImageResource(R.drawable.back);
        conv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConversationActivity.this.finish();
            }
        });

		sever_switch = (ImageBtSingle) findViewById(R.id.conv_top_sever_switch);
		sever_switch.setImageResource(R.drawable.sever_switch);
        sever_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Message m = new Message();
                m.what = SEVER_SWITCH;
                Bundle b = new Bundle();
                b.putString(THIS_IS_TARGET, target + "");
                m.setData(b);
                handler.sendMessage(m);
            }
        });

        serverName = (TextView) findViewById(R.id.conv_top_title_name);
        if(name != null){
            serverName.setText(name);
            serverNumber = (TextView) findViewById(R.id.conv_top_title_number);
            serverNumber.setText(target);
        }else {
            serverNumber = (TextView) findViewById(R.id.conv_top_title2_number);
            serverNumber.setText(target);
        }
        serverNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

		make_call = (ImageBtSingle) findViewById(R.id.conv_top_make_call);
		make_call.setImageResource(R.drawable.make_call);
        make_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	Message m = new Message();
                m.what = MAKE_CALL;
                Bundle b = new Bundle();
                b.putString(THIS_IS_TARGET, target + "");
                m.setData(b);
                handler.sendMessage(m);
            }
        });
	}

	private void initInfoLayer() {
		info_frame = (RelativeLayout) View.inflate(this,
				R.layout.conv_activity_info, null);
		info_content.addView(info_frame);

		infoTimer = new HandleTimer() {
			@Override
			protected void onTime() {
				// 在这里做更新ui的操作
				((LinearLayout) info_frame.findViewById(R.id.conv_info_msg_info))
				.setVisibility(View.GONE);
			}
		};
		
		setInfo();
		
		//设置timer操作
		
		
		// info_content.setVisibility(View.GONE);
	}

	private void initMsgList() {
		msgList = (PullToRefreshListView) findViewById(R.id.conv_activity_listview);
        chatMsgSource = MyService.getChatMsgSource(source, target);
        chatMsgViewAdapter = new ChatMsgViewAdapter(this, chatMsgSource,target);
        msgList.setAdapter(chatMsgViewAdapter);
		msgList.setCacheColorHint(Color.TRANSPARENT);
		msgList.requestFocus();
        msgList.setSelection(chatMsgViewAdapter.getCount() - 1);
        msgList.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatMsgSource.loadMore();
                msgList.onRefreshComplete();
            }
        });
	}

	private void initFunctionLayer() {
		function_frame = (RelativeLayout) View.inflate(this,
				R.layout.conv_activity_function, null);
		function_content.addView(function_frame);

		send_message=(ImageBtSingle)findViewById(R.id.conv_function_send_message);
		send_message.setImageResource(R.drawable.send);
        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = message_txt.getText().toString();
                if(msg != null && msg.length() > 0){
                    Intent intent = new Intent(IntentConstants.INTENT_ACTION_SEND_MSG);
                    intent.putExtra(IntentConstants.KEY_INTENT_CHAT_SOURCES, source);
                    intent.putExtra(IntentConstants.KEY_INTENT_CHAT_TARGETS, target);
                    intent.putExtra(IntentConstants.KEY_INTENT_CHAT_MSG, msg);
                    sendBroadcast(intent);
                    // 清空发送框
                    message_txt.setText("");
                }else{
                    showToast(ConversationActivity.this, R.string.error_empty_message, Toast.LENGTH_LONG);
                }
            }
        });

		
		message_txt = (EditText) findViewById(R.id.conv_function_message_txt);

		message_txt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
			}
		});

		message_txt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			// 在文字改变后调用
			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
				} else {
				}
			}
		});

	}

    private void initOperationPanel(){

        operation_container = (AbsoluteLayout) this.findViewById(R.id.conv_activity_operation_container);
        operation_panel = (LinearLayout) this.findViewById(R.id.conv_activity_opertation_panel);
        deleteMsg = (TextView) this.findViewById(R.id.conv_activity_delete_msg);
        copyContent = (TextView) this.findViewById(R.id.conv_activity_copy_content);
        //viewDetail = (TextView) this.findViewById(R.id.conv_activity_view_detail);
        operation_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                operation_container.setVisibility(View.INVISIBLE);
            }
        });
        deleteMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedEntity != null){
                    // 发送广播要求删除消息
                    Intent intent = new Intent(IntentConstants.INTENT_ACTION_DELETE_MSG);
                    intent.putExtra(IntentConstants.KEY_INTENT_CHAT_MSGID, selectedEntity.get_id());
                    sendBroadcast(intent);
                }
                operation_container.setVisibility(View.INVISIBLE);
            }
        });
        copyContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedEntity != null){
                    ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    cmb.setText(selectedEntity.getBody().getMsg());
                    showToast(ConversationActivity.this, R.string.content_copied, Toast.LENGTH_LONG);
                }
                operation_container.setVisibility(View.INVISIBLE);
            }
        });
//        viewDetail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }

	//设置info栏的显示文字
	private void setInfo() {
		((TextView) info_frame.findViewById(R.id.conv_info_static_text))
				.setText(source);

		if (newestMsg != null) {
			((LinearLayout) info_frame.findViewById(R.id.conv_info_msg_info))
					.setVisibility(View.VISIBLE);
			((TextView) info_frame.findViewById(R.id.conv_info_msg_text))
					.setText(newestMsg);
			refreshInfoTimer();
		} else {
			((LinearLayout) info_frame.findViewById(R.id.conv_info_msg_info))
					.setVisibility(View.GONE);
		}
	}

	private void refreshInfoTimer() {
		infoTimer.restart(3000, 0);
	}

	/**
	 * 
	 */
	private void initInfrastructure() {
//		controller = new Controller();
//		datasource = Dao.instance(this);
	}

	private void initListener() {

	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what){
    	case MAKE_CALL:{
    		final String mTarget = msg.getData().getString(THIS_IS_TARGET);
    		Dialog dialog = new AlertDialog.Builder(
				ConversationActivity.this)
				.setTitle("拨号")
				.setMessage("是否拨打:" + mTarget + "？")
				.setPositiveButton("现在拨打",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								try {
				                    Intent intent = new Intent(Intent.ACTION_CALL);
				                    intent.setData(Uri.parse("tel:" + mTarget));
				                    startActivity(intent);
				                } catch (Exception e) {
				                    Log.e("SampleApp", "Failed to invoke call", e);
				                }
							}
						})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
									}
								}).create();
				dialog.show();
				break;}
    	case SEVER_SWITCH:{
    		final String mTarget = msg.getData().getString(THIS_IS_TARGET);
    		Dialog dialog = new AlertDialog.Builder(
					ConversationActivity.this)
					.setTitle("转至系统短信")
					.setMessage("是否跳转至系统短信回复？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
									if (mTarget != null&& !mTarget.equals("")) {
										// 调用系统短信界面
										Uri smsToUri = Uri.parse("smsto:"+ mTarget);
										Intent intent = new Intent(Intent.ACTION_SENDTO,smsToUri);
										if (message_txt.getText().toString() != null) {
											intent.putExtra("sms_body",message_txt.getText().toString());
										}
										startActivity(intent);
									}

								}
							})
							.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
								}
							}).create();
			dialog.show();
			break;
    	}
    	
    	}

		return false;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * add()方法的四个参数，依次是： 1、组别，不分组就Menu.NONE, 2、Id，Android根据这个Id来确定不同的菜单
		 * 3、顺序，菜单现在在前面由这个参数的大小决定 4、文本，菜单的显示文本
		 */
		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "清空聊天记录").setIcon(
				android.R.drawable.ic_menu_delete);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			break;
		case Menu.FIRST + 2:
			break;
		case Menu.FIRST + 3:
			break;
		}
		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent();
            intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_SOURCE, source);
            intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_TARGET, target);
            this.setResult(MsgCenterActivity.RESULT_CODE_CHAT, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class ScrollToBottomReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_SCROLL_TO_BOTTOM)){
                msgList.setSelection(chatMsgViewAdapter.getCount() - 1);
            }
        }
    }
    
    private class OnItemLongClickReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(IntentConstants.INTENT_ACTION_MSG_LONG_CLICKED)){
                int position = intent.getIntExtra(IntentConstants.KEY_INTENT_CHAT_POSITION, -1);
                if(position >=0 ){
                    selectedEntity = (ChatMsgEntity) chatMsgViewAdapter.getItem(position);
                    if(selectedEntity.getType() == MessageBean.TYPE_MSG_CAL_MISS){
                        copyContent.setClickable(false);
                        copyContent.setTextColor(getResources().getColor(R.color.dark_gray));
                    }else {
                        copyContent.setClickable(true);
                        copyContent.setTextColor(getResources().getColor(R.color.black));
                    }
                    int x = intent.getIntExtra(IntentConstants.KEY_INTENT_CHAT_POS_X, 0);
                    int y = intent.getIntExtra(IntentConstants.KEY_INTENT_CHAT_POS_Y, 0);
                    int h = intent.getIntExtra(IntentConstants.KEY_INTENT_CHAT_HEIGHT, 0);
                    int w = intent.getIntExtra(IntentConstants.KEY_INTENT_CHAT_WIDTH, 0);
                    Log.d(TAG, "x:" + x + " y:" + y + " h:" + h + " w:" + w);
                    int width  = operation_panel.getWidth();
                    int height = operation_panel.getHeight();
                    int status = WidgetsUtil.getStatusBarHeight(ConversationActivity.this);
                    Log.d(TAG, "width:" + width + " height:" + height + " status:" + status);
                    // 获取屏幕高度
                    DisplayMetrics metric = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metric);
                    int screenHeight = metric.heightPixels;
                    y = y - status;
                    // 计算x, 22是小角的偏移量
                    x = (x + w) / 2 - width / 2 + 22;
                    if(x < 22) x = 22;

                    // 计算y
                    if(y + h + height <= screenHeight / 2){
                        // 放在下方
                        y = y + h;
                    }
                    else if(y >= screenHeight / 2){
                        // 放在上方
                        y = y - height;
                    }
                    else if(y >= (screenHeight - y - h) && y >= height){
                        // 放在上方
                        y = y - height;
                    }
                    else if(y < (screenHeight - y - h) && (screenHeight - y - h) >= height){
                        // 放在下方
                        y = y + height;
                    }
                    else {
                        // 放在中间
                        y = (y + h - height) / 2;
                    }
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
                            AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                            AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                            x,y);
                    operation_panel.setLayoutParams(params);
                    operation_container.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
