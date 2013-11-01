package com.dt.cloudmsg.views;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.adapter.SingleContactPickerListAdapter;
import com.dt.cloudmsg.component.ImageBtSingle;
import com.dt.cloudmsg.component.PickedContact;
import com.dt.cloudmsg.datasource.ContactsSource;
import com.dt.cloudmsg.model.Contact;
import com.dt.cloudmsg.model.ContactsEntity;
import com.dt.cloudmsg.model.Picked;
import com.dt.cloudmsg.service.MyService;
import com.dt.cloudmsg.util.IntentConstants;
import com.dt.cloudmsg.util.StringUtil;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ContactChooseActivity extends BaseActivity implements
		Handler.Callback {

	private ListView contactList;
	private TextView letterIndicator;
	private LinearLayout listContainer;
	private LinearLayout pinyinSelector;
	private int willDelete;

	private List<Contact> allContacts = new ArrayList<Contact>();
	private List<Contact> tempList = new ArrayList<Contact>();
	private List<TextView> letters = new ArrayList<TextView>();
	private static List<String> adhocNums = new ArrayList<String>();
	private static ArrayList<PickedContact> pickedContactViews = new ArrayList<PickedContact>();

	private SingleContactPickerListAdapter contactAdapter;
	private BaseExpandableListAdapter groupAdapter;

	private enum State {
		STATE_CONTACT, STATE_GROUP, STATE_ACPT
	}

	private int lastLetter = 0;

	private static final String[] alphabet = { "#", "A", "B", "C", "D", "E",
			"F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
			"S", "T", "U", "V", "W", "X", "Y", "Z" };

	private ImageBtSingle addContact;
	private ImageBtSingle cont_back;
	private ImageBtSingle send_message;

	private RelativeLayout main_layer;
	private FrameLayout top_content;
	private FrameLayout picked_content;
	private FrameLayout function_content;
	private FrameLayout list_content;
	private FrameLayout function2_content;

	private RelativeLayout top_frame;
	private HorizontalScrollView picked_frame;
	private RelativeLayout list_frame;
	private RelativeLayout function_frame;
	private RelativeLayout function2_frame;

	private LinearLayout pickedContactContainer;
	private RelativeLayout pinyinContainer;

	private EditText searchTxtBox;
	private EditText message_txt;

	private String currentSever;
	private String imei;
	private int status = STATUS_NORMAL;

	private static final int STATUS_NORMAL = 0x00;
	private static final int STATUS_SEARCH = 0x01;

	private int pickedNumber;

	private int screenWidth;
	private int screenHeight;
	private float screenDensity;
	private int screenDensityDpi;
	private int pinyinContainerHeight;

	private static final int CONTACTS_LOADED = 0x00;
	private static Handler handler;
	private static Pattern numberPattern = Pattern
			.compile("((\\+)*[0-9]{2,3}[-]*)*[0-9]{3,16}");

	private ContactsSource contactsSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_picker);
		currentSever = getIntent().getStringExtra(
				IntentConstants.KEY_INTENT_MSG_CONTACT_SERVER);
		imei = getIntent().getStringExtra(
				IntentConstants.KEY_INTENT_MSG_CONTACT_IMEI);
		status = getIntent().getIntExtra(
				IntentConstants.KEY_INTENT_MSG_CONTACT_STATUS, 0);

		// 获得屏幕高度
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		screenWidth = metric.widthPixels; // 屏幕宽度（像素）
		screenHeight = metric.heightPixels; // 屏幕高度（像素）
		screenDensity = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
		screenDensityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）

		handler = new Handler(this);
		preInit();
		pinyinContainerHeight=(int) (screenHeight-screenDensity*48*5);//拼音条高度

		// 察看联系人是否加载完毕
		if (!MyService.isContactsLoadded()) {
			// 等待加载联系人
			showLoadingLayer();
			new AsyncTask() {
				@Override
				protected Object doInBackground(Object[] objects) {
					while (!MyService.isContactsLoadded())
						;
					handler.sendEmptyMessage(CONTACTS_LOADED);
					return null;
				}
			}.execute(null, null, null);
		} else {
			postInit();
		}
	}

	private void preInit() {
		initFramework();
		initTopLayer();
		initPickedLayer();
		initFunctionLayer();
		initFunction2Layer();
		Log.d("function 2 layer done", "");
		initLoadingLayer(main_layer);
	}

	private void postInit() {
		initData();
		Log.d("data init done", "");
		initContactList();
		Log.d("contact list init done", "");
		// Setup listeners
		initListener();
		Log.d("lister init done", "");
		initContact();
	}

	private void initData() {
		pickedNumber = 0;
		allContacts = MyService.getSortedContacts();
	}

	private void initFramework() {

		main_layer = (RelativeLayout) findViewById(R.id.contact_picker_main_layer);

		top_content = (FrameLayout) findViewById(R.id.contact_picker_top_layout);
		picked_content = (FrameLayout) findViewById(R.id.contact_picker_picked_layout);
		function_content = (FrameLayout) findViewById(R.id.contact_picker_function_layout);
		list_content = (FrameLayout) findViewById(R.id.contact_picker_list_layout);
		function2_content = (FrameLayout) findViewById(R.id.contact_picker_function2_layout);
	}

	private void initTopLayer() {
		// TODO Auto-generated method stub
		top_frame = (RelativeLayout) View.inflate(this,
				R.layout.contact_picker_top, null);
		top_content.addView(top_frame);

		cont_back = (ImageBtSingle) findViewById(R.id.contact_top_back_btn);
		cont_back.setImageResource(R.drawable.back);

		cont_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		setTitle();
	}

	private void initPickedLayer() {
		// TODO Auto-generated method stub
		picked_frame = (HorizontalScrollView) View.inflate(this,
				R.layout.contact_picker_picked, null);
		picked_content.addView(picked_frame);
		pickedContactContainer = (LinearLayout) this
				.findViewById(R.id.contact_picked_contact_container);
		picked_content.setVisibility(View.GONE);
	}

	private void initFunctionLayer() {
		// TODO Auto-generated method stub
		function_frame = (RelativeLayout) View.inflate(this,
				R.layout.contact_picker_function, null);
		function_content.addView(function_frame);

		searchTxtBox = (EditText) this
				.findViewById(R.id.contact_function_search_box);
		searchTxtBox.setHint("筛选或添加收件人");
		searchTxtBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i,
					int i2, int i3) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2,
					int i3) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				String filter = searchTxtBox.getText().toString();
				if (filter == null || filter.length() == 0) {
					if (status != STATUS_NORMAL)
						setStatus(STATUS_NORMAL);
				} else {
					tempList.clear();
					if (numberPattern.matcher(filter).matches()) {
						addContact.setImageResource(R.drawable.add_contact);
						addContact.setEnabled(true);
					} else {
						addContact
								.setImageResource(R.drawable.add_contact_unclickable);
						addContact.setEnabled(false);
					}
					String pinyin = StringUtil.getPinyin(filter);
					String shouZiMu = StringUtil.getShouZiMu(filter);
					if (StringUtil.isNumber(filter)) {
						for (Contact contact : allContacts) {
							if (contact.number.contains(filter))
								tempList.add(contact);
						}
					} else {
						for (Contact contact : allContacts) {
							if (contact.name.contains(filter))
								tempList.add(contact);
						}
						if (tempList.size() <= 0) {
							for (Contact contact : allContacts) {
								if (contact.key_lower.contains(pinyin
										.toLowerCase())
										|| contact.shouZiMu.contains(shouZiMu))
									tempList.add(contact);
							}
						}
					}
					setStatus(STATUS_SEARCH);
				}
			}
		});

		addContact = (ImageBtSingle) this
				.findViewById(R.id.contact_function_add_btn);
		addContact.setImageResource(R.drawable.add_contact_unclickable);
		addContact.setEnabled(false);
		addContact.setBackgroundResource(R.drawable.button_top);
		addContact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d("cliked", "cliked");
				String number = searchTxtBox.getText().toString();
				if (numberPattern.matcher(number).matches()) {
					if (adhocNums.contains(number)) {
						showToast(ContactChooseActivity.this,
								R.string.number_already_added,
								Toast.LENGTH_LONG);
						return;
					}
					searchTxtBox.setText("");
					adhocNums.add(number);
					addContact(null, number);
				} else {
					showToast(ContactChooseActivity.this,
							R.string.error_invalid_number, Toast.LENGTH_LONG);
				}
			}
		});

	}

	private void initContactList() {
		// TODO Auto-generated method stub
		list_frame = (RelativeLayout) View.inflate(this,
				R.layout.contact_picker_list, null);
		list_content.addView(list_frame);
		// Setup contact picker list
		contactList = (ListView) this
				.findViewById(R.id.contact_list_item_container);
		contactAdapter = new SingleContactPickerListAdapter(this, allContacts);
		contactList.setAdapter(contactAdapter);

		// / Init pinyin container
		pinyinContainer = (RelativeLayout) this
				.findViewById(R.id.contact_list_pinyin_container);

		pinyinSelector = (LinearLayout) this
				.findViewById(R.id.contact_list_pinyin_selector);
		letterIndicator = (TextView) this
				.findViewById(R.id.contact_list_letter_indicator);
		letters = new ArrayList<TextView>();

		for (String letter : alphabet) {
			TextView tv = new TextView(this);
			tv.setText(letter);
			tv.setTextSize(12);
			letters.add(tv);
			pinyinSelector.addView(tv, new LayoutParams(
					LayoutParams.WRAP_CONTENT, (int)(pinyinContainerHeight*0.036)));
		}
		letters.get(lastLetter).setTextColor(
				this.getResources().getColor(R.color.msg_blue));
		pinyinSelector.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					letterIndicator.setVisibility(View.VISIBLE);
				case MotionEvent.ACTION_MOVE:
					int height = pinyinSelector.getHeight();
					int index = (int) (event.getY() / height * alphabet.length);
					if ((index < alphabet.length) && (index >= 0)
							&& (event.getY() < height)) {
						setSelectedLetter(index);
						letterIndicator.setText(alphabet[index]);
						setListSelection();
					}
					break;
				case MotionEvent.ACTION_UP:
					letterIndicator.setVisibility(View.GONE);
					height = pinyinSelector.getHeight();
					index = (int) (event.getY() / height * alphabet.length);
					if ((index < alphabet.length) && (index >= 0)
							&& (event.getY() < height)) {
						setSelectedLetter(index);
						letterIndicator.setText(alphabet[index]);
						setListSelection();
					}
					break;
				}
				return true;
			}

		});

	}

	private void initFunction2Layer() {
		// TODO Auto-generated method stub

		function2_frame = (RelativeLayout) View.inflate(this,
				R.layout.contact_picker_function2, null);

		function2_content.addView(function2_frame);

		send_message = (ImageBtSingle) findViewById(R.id.contact_function2_send_messsage);
		send_message.setImageResource(R.drawable.send);
		send_message.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				String msg = message_txt.getText().toString();
				if (msg != null && msg.length() > 0) {
					String targets = "";
					for (Contact contact : allContacts) {
						if (contact.chosen)
							targets += contact.number + ";";
					}
					for (String num : adhocNums)
						targets += num + ";";
					Log.d("current server:", currentSever);
					if(!targets.equals("")){
					Intent intent = new Intent(
							IntentConstants.INTENT_ACTION_SEND_MSG);
					intent.putExtra(IntentConstants.KEY_INTENT_CHAT_SOURCES,
							currentSever);
					intent.putExtra(IntentConstants.KEY_INTENT_CHAT_TARGETS,
							targets);
					intent.putExtra(IntentConstants.KEY_INTENT_CHAT_MSG, msg);
					ContactChooseActivity.this.sendBroadcast(intent);
					message_txt.setText("");
					Toast.makeText(ContactChooseActivity.this, "消息已发送",
							Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(ContactChooseActivity.this, "尚未选中收件人",
								Toast.LENGTH_LONG).show();
					}
				} else {
					showToast(ContactChooseActivity.this, "不能发送空消息",
							Toast.LENGTH_LONG);
				}
			}
		});

		message_txt = (EditText) findViewById(R.id.contact_function2_msg_txt);
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

	private void setTitle() {
		TextView tips_txt = (TextView) findViewById(R.id.contact_top_title_tips);
		TextView sever_txt = (TextView) findViewById(R.id.contact_top_title_sever);

		if (pickedNumber > 0) {
			tips_txt.setText("已选择" + pickedNumber + "位收件人");
			sever_txt.setText(currentSever);
		} else {
			tips_txt.setText("请添加收件人");
			sever_txt.setText(currentSever);
		}
	}

	private int binarySearch(List<Contact> contacts, Contact key, int l, int u) {
		if (l > u)
			return -1;

		int m = (l + u) / 2;
		int c = contacts.get(m).compareTo(key);

		if (c == 0)
			return m;
		else if (c > 0)
			return binarySearch(contacts, key, l, m - 1);
		else
			return binarySearch(contacts, key, m + 1, u);
	}

	/**
	 * 初始化事件监听
	 */
	private void initListener() {
		this.contactList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (status == STATUS_NORMAL) {
					Contact c = allContacts.get(arg2);
					if (!c.chosen) {
						addContact(c, null);
					} else {
						deleteContact(c, null, null);
					}
					c.chosen = !c.chosen;

					contactAdapter.notifyDataSetChanged();
				} else {
					Contact c = tempList.get(arg2);
					if (!c.chosen) {
						addContact(c, null);
					} else {
						deleteContact(c, null, null);
					}
					c.chosen = !c.chosen;

					contactAdapter.notifyDataSetChanged();
				}
			}

		});

		this.contactList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub0
				if (firstVisibleItem < allContacts.size()) {
					char[] key_c = allContacts.get(firstVisibleItem).key_arr;
					if (key_c[0] >= 'a' && key_c[0] <= 'z')
						setSelectedLetter(key_c[0] - 'a' + 1);
					else
						setSelectedLetter(0);
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

		});
	}

	/**
	 * 将选中的联系人添加到复选文本框中
	 */
	private void initContact() {
		pickedContactContainer.removeAllViews();
		pickedContactViews.clear();
		pickedNumber = 0;
		for (Contact c : allContacts) {
			if (c.chosen) {
				PickedContact pickedContact = getPickedContact(c, null);
				pickedContactViews.add(pickedContact);
				pickedContactContainer.addView(pickedContact);
				pickedNumber++;
			}
		}
		for (String num : adhocNums) {
			PickedContact pickedContact = getPickedContact(null, num);
			pickedContactViews.add(pickedContact);
			pickedContactContainer.addView(pickedContact);
			pickedNumber++;
		}

		if (pickedNumber > 0) {
			picked_content.setVisibility(View.VISIBLE);
			Handler mHandler = new Handler();

			mHandler.post(new Runnable() {
				public void run() {

					int offset = pickedContactContainer.getMeasuredWidth()
							- picked_frame.getHeight();
					if (offset < 0) {
						offset = 0;
					}

					picked_frame.scrollTo(offset, 0);
				}
			});
		} else {
			picked_content.setVisibility(View.GONE);
		}

		setTitle();
	}

	/**
	 * 添加选中的联系人
	 */
	private void addContact(Contact contact, String number) {
		PickedContact pickedContact = getPickedContact(contact, number);
		pickedContactViews.add(pickedContact);
		pickedContactContainer.addView(pickedContact);
		pickedNumber++;
		if (pickedNumber > 0) {
			picked_content.setVisibility(View.VISIBLE);
			Handler mHandler = new Handler();

			mHandler.post(new Runnable() {
				public void run() {

					int offset = pickedContactContainer.getMeasuredWidth()
							- picked_frame.getHeight();
					if (offset < 0) {
						offset = 0;
					}

					picked_frame.scrollTo(offset, 0);
				}
			});
		} else {
			picked_content.setVisibility(View.GONE);
		}

		setTitle();
	}

	/**
	 * 删除选中的联系人
	 */
	private void deleteContact(Contact contact, String number,
			PickedContact pickedContact) {
		if ((pickedContact == null)) {
			for (PickedContact p : pickedContactViews) {
				if (p.isContact() && (p.getContact().equals(contact))) {
					pickedContactViews.remove(p);
					pickedContactContainer.removeView(p);
					pickedNumber--;
					break;
				}
			}
		} else if (pickedContact.isContact()) {
			pickedContactViews.remove(pickedContact);
			pickedContactContainer.removeView(pickedContact);
			pickedNumber--;
		} else {
			pickedContactViews.remove(pickedContact);
			pickedContactContainer.removeView(pickedContact);
			pickedNumber--;
		}
		if (pickedNumber > 0) {
			picked_content.setVisibility(View.VISIBLE);
			
		} else {
			picked_content.setVisibility(View.GONE);
		}

		setTitle();
	}

	/**
	 * 清空选中的联系人
	 */
	private void clearContact() {

		pickedNumber = 0;
		pickedContactContainer.removeAllViews();
		pickedContactViews.clear();
		pickedNumber = 0;
		adhocNums.clear();
		for (Contact c : allContacts) {
			if (c.chosen) {
				c.chosen=false;
			}
		}

		picked_content.setVisibility(View.GONE);

		setTitle();
	}

	private PickedContact getPickedContact(Contact contact, String number) {
		final PickedContact pickedItem = new PickedContact(this, contact,
				number);
		pickedItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (pickedItem.isContact()) {
					pickedItem.getContact().chosen = false;
					contactAdapter.notifyDataSetChanged();
				} else {
					adhocNums.remove(pickedItem.getNumber());
				}
				deleteContact(null, null, pickedItem);
			}
		});
		return pickedItem;
	}

	/**
	 * 设置当前被选中的字母
	 * 
	 * @param position
	 */
	private void setSelectedLetter(int position) {
		this.lastLetter = position;
		this.letters.get(lastLetter).setTextColor(
				this.getResources().getColor(R.color.msg_blue));
		for (int i = 0; i < alphabet.length; i++)
			if (lastLetter != i) {
				this.letters.get(i).setTextColor(
						this.getResources().getColor(R.color.black));
			}

	}

	/**
	 * list快速跳转
	 * 
	 */
	private void setListSelection() {
		// TODO Auto-generated method stub

		boolean isFind = false;
		List<Contact> localList = (status == STATUS_NORMAL ? allContacts
				: tempList);

		if (localList.size() <= 0)
			return;
		if (lastLetter <= 0)
			contactList.setSelection(0);
		else {
			while (!isFind && lastLetter >= 0) {
				for (int i = 0; i < localList.size(); i++)
					if (localList.get(i).key.toUpperCase().startsWith(
							alphabet[lastLetter])) {
						contactList.setSelection(i);
						isFind = true;
						break;
					}
				lastLetter--;
			}
			if (!isFind)
				contactList.setSelection(0);
		}

	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case CONTACTS_LOADED: {
			Log.d("Contact choose activity:", "contacts loaded");
			dismissLoadingLayer();
			postInit();
			break;
		}
		}

		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			switch (status) {
			case STATUS_NORMAL: {
				finish();
			}
			case STATUS_SEARCH: {
				setStatus(STATUS_NORMAL);
			}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setStatus(int status) {
		this.status = status;
		switch (status) {
		case STATUS_SEARCH: {
			contactAdapter.setContacts(tempList);
			pinyinContainer.setVisibility(View.GONE);
			break;
		}
		case STATUS_NORMAL: {
			searchTxtBox.setText("");
			addContact.setImageResource(R.drawable.add_contact_unclickable);
			addContact.setEnabled(false);
			contactAdapter.setContacts(allContacts);
			pinyinContainer.setVisibility(View.VISIBLE);
			break;
		}
		}
	}

	private float getTextSize() {
		if (screenHeight > screenWidth) {
			float m = (float) (((screenHeight - screenWidth * 0.6)
					/ (Math.sqrt(screenDensityDpi)) / 3.8));
			return m;
		}
		return 11;
	}
}