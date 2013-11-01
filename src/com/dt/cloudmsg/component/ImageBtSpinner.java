package com.dt.cloudmsg.component;

import com.dt.cloudmsg.R;

import android.R.string;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImageBtSpinner extends RelativeLayout {
	private ImageView iv;
	private ImageView more;
	private TextView tv;
	private static final int SERVER_ONLINE_ICON = R.drawable.server_online;
	private static final int SERVER_OFFLINE_ICON = R.drawable.server_offline;
	private static final int SERVER_UNUSUAL_ICON = R.drawable.server_unusua;
	private static final int SERVER_LOCAL_ICON = R.drawable.server_local;
	private static final int SERVER_LOCAL_OFFLINE_ICON = R.drawable.server_local_offline;

	public static final int SERVER_ONLINE = 0x01;
	public static final int SERVER_OFFLINE = 0x02;
	public static final int SERVER_UNUSUAL = 0x03;
	public static final int SERVER_LOCAL = 0x04;
	public static final int SERVER_LOCAL_OFFLINE = 0x05;

	public ImageBtSpinner(Context context) {
		this(context, null);
	}

	public ImageBtSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 导入布局
		LayoutInflater.from(context).inflate(R.layout.imagebt_spinner_style1,
				this, true);
		iv = (ImageView) findViewById(R.id.iv);
		tv = (TextView) findViewById(R.id.tv);
		more = (ImageView) findViewById(R.id.iv_more);

	}

	/**
	 * 设置显示的文字
	 */
	public void setTextViewText(String text) {
		tv.setText(text);
	}

	public void setTextViewText(int id) {
		tv.setText(id);
	}

	public void setItemNum(int num) {
		if (num <= 1) {
			more.setVisibility(View.GONE);
			this.setClickable(false);
		} else {
			more.setVisibility(View.VISIBLE);
			this.setClickable(true);
		}
	}

	public void setStatus(int i){
		switch (i){
			case SERVER_ONLINE:
				iv.setImageResource(SERVER_ONLINE_ICON);
				break;
			case SERVER_OFFLINE:
			iv.setImageResource(SERVER_OFFLINE_ICON);
			break;
			case SERVER_UNUSUAL:
				iv.setImageResource(SERVER_UNUSUAL_ICON);
				break;
			case SERVER_LOCAL:
				iv.setImageResource(SERVER_LOCAL_ICON);
				break;
			case SERVER_LOCAL_ICON:
				iv.setImageResource(SERVER_LOCAL_OFFLINE_ICON);
		}
	}


}
