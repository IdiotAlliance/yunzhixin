package com.dt.cloudmsg.component;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.model.Contact;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PickedContact extends RelativeLayout {
	private TextView tv;
	private Contact contact;
	private String number;
	public static final int STATUS_NORMAL = 0x01;
	public static final int STATUS_DELETE = 0x02;

	public PickedContact(Context context, Contact contact, String number) {
		this(context, contact, number, null);
	}

	public PickedContact(Context context, Contact contact, String number,
			AttributeSet attrs) {
		super(context, attrs);

		// 导入布局
		LayoutInflater.from(context).inflate(R.layout.contactpicked_item, this,
				true);
		tv = (TextView) findViewById(R.id.contactpicked_item_tv_name);
		this.contact = contact;
		this.number = number;
		if (contact == null) {
			setText(number);
		} else {
			setText(contact.name);
		}
	}

	// public void setStatus(int status){
	// switch(status){
	// case:STATUS_NORMAL
	// }
	// }

	private void setText(String contact) {
		tv.setText(contact);
	}

	public boolean isContact() {
		if (contact == null) {
			return false;
		} else {
			return true;
		}
	}

	public Contact getContact() {
		return contact;
	}

	public String getNumber() {
		return number;
	}
}
