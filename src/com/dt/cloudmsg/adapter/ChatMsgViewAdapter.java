package com.dt.cloudmsg.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.beans.MessageBean;
import com.dt.cloudmsg.component.ImageBtSingle;
import com.dt.cloudmsg.datasource.ChatMsgSource;
import com.dt.cloudmsg.datasource.XDataChangeListener;
import com.dt.cloudmsg.model.ChatMsgEntity;
import com.dt.cloudmsg.util.DateFormatter;
import com.dt.cloudmsg.util.IntentConstants;
import com.dt.cloudmsg.views.ConversationActivity;

public class ChatMsgViewAdapter extends BaseAdapter implements
		XDataChangeListener<ChatMsgEntity>, Removable {

	private static final int IMVT_COM_MSG = 0;
	private static final int IMVT_TO_MSG = 1;

	private ChatMsgSource chatMsgSource;
	private Context context;
	private String target;
	private Handler handler;

	public ChatMsgViewAdapter(Context context, ChatMsgSource chatMsgSource,String target) {
		this.context = context;
		this.chatMsgSource = chatMsgSource;
		this.chatMsgSource.registerDataChangeListener(this);
		this.target=target;
		handler = new Handler((Callback) context);
	}

	public int getCount() {
		return chatMsgSource.size();
	}

	public Object getItem(int position) {
		return chatMsgSource.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		ChatMsgEntity entity = (ChatMsgEntity) getItem(position);
		return entity.isComMsg() ? IMVT_COM_MSG : IMVT_TO_MSG;
	}

	public int getViewTypeCount() {
		return 2;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		ChatMsgEntity entity = (ChatMsgEntity) getItem(position);
		boolean isComMsg = entity.isComMsg();
		int msgType = entity.getType();
		ViewHolder viewHolder = new ViewHolder();
		if (convertView == null || ((ViewHolder)convertView.getTag()).isCom != isComMsg) {
			if (isComMsg) {
				if (msgType == MessageBean.TYPE_MSG_CAL_MISS) {
                    convertView = LayoutInflater.from(context).inflate(
                            R.layout.chatting_item_call_text_left, null);
                    viewHolder.content = (LinearLayout) convertView
                            .findViewById(R.id.chat_msg_content);
					viewHolder.tvSendTime = (TextView) convertView
							.findViewById(R.id.chat_call_tv_sendtime);
					viewHolder.tvCallhead = (TextView) convertView
							.findViewById(R.id.chat_call_tv_callhead);
					viewHolder.tvCalldesc = (TextView) convertView
							.findViewById(R.id.chat_call_tv_calldesc);
					viewHolder.callBtn = (ImageBtSingle) convertView
							.findViewById(R.id.chat_call_call_btn);
				} else {
                    convertView = LayoutInflater.from(context).inflate(
                            R.layout.chatting_item_msg_text_left, null);
                    viewHolder.content = (LinearLayout) convertView
                            .findViewById(R.id.chat_msg_content);
					viewHolder.tvSendTime = (TextView) convertView
							.findViewById(R.id.chat_left_tv_sendtime);
					viewHolder.tvContent = (TextView) convertView
							.findViewById(R.id.chat_left_tv_chatcontent);
				}
			} else {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.chatting_item_msg_text_right, null);
                viewHolder.content = (LinearLayout) convertView
                        .findViewById(R.id.chat_msg_content);
				viewHolder.tvSendTime = (TextView) convertView
						.findViewById(R.id.chat_right_tv_sendtime);
				viewHolder.tvContent = (TextView) convertView
						.findViewById(R.id.chat_right_tv_chatcontent);
				viewHolder.sending = (TextView) convertView
						.findViewById(R.id.chat_right_tv_insend);
				viewHolder.sendFail = (TextView) convertView
						.findViewById(R.id.chat_right_tv_sendfailed);
			}
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

        viewHolder.content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(IntentConstants.INTENT_ACTION_MSG_LONG_CLICKED);
                int[] location = new int[2];
                view.getLocationInWindow(location);
                intent.putExtra(IntentConstants.KEY_INTENT_CHAT_POSITION, position);
                intent.putExtra(IntentConstants.KEY_INTENT_CHAT_POS_X, location[0]);
                intent.putExtra(IntentConstants.KEY_INTENT_CHAT_POS_Y, location[1]);
                intent.putExtra(IntentConstants.KEY_INTENT_CHAT_HEIGHT, view.getHeight());
                intent.putExtra(IntentConstants.KEY_INTENT_CHAT_WIDTH, view.getWidth());
                context.sendBroadcast(intent);
                return true;
            }
        });

		viewHolder.tvSendTime.setText(DateFormatter.getLocalDate(entity.getRawtime(), DateFormatter.DATE_FOR_CHATMSG));

		if (!isComMsg) {
			viewHolder.tvContent.setText(entity.getBody().getMsg());
			switch (entity.getStatus()) {
			case ChatMsgEntity.STATUS_OK: {
				viewHolder.sending.setVisibility(View.INVISIBLE);
				viewHolder.sendFail.setVisibility(View.INVISIBLE);
				break;
			}
			case ChatMsgEntity.STATUS_FAILED: {
				viewHolder.sendFail.setVisibility(View.VISIBLE);
				viewHolder.sending.setVisibility(View.INVISIBLE);
				break;
			}
			case ChatMsgEntity.STATUS_SENDING: {
				viewHolder.sending.setVisibility(View.VISIBLE);
				viewHolder.sendFail.setVisibility(View.INVISIBLE);
				break;
			}
			}
		} else if (msgType == MessageBean.TYPE_MSG_CAL_MISS) {
			String[] callInfo=entity.getBody().getMsg().split(";");
			viewHolder.tvCallhead.setText(callInfo[0]);
			viewHolder.tvCalldesc.setText(callInfo[1]);
			viewHolder.callBtn.setImageResource(R.drawable.make_call);
			viewHolder.callBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Message m = new Message();
                    m.what = ConversationActivity.MAKE_CALL;
                    Bundle b = new Bundle();
                    b.putString(ConversationActivity.THIS_IS_TARGET, target + "");
                    m.setData(b);
                    handler.sendMessage(m);
				}
			});
		}else if(msgType == MessageBean.TYPE_MSG_SMS){
			viewHolder.tvContent.setText(entity.getBody().getMsg());
		}
		return convertView;
	}

	@Override
	public void unregister() {
		chatMsgSource.removeListener(this);
	}

	static class ViewHolder {
        public LinearLayout content;
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public TextView tvCallhead;
		public TextView tvCalldesc;
		public TextView sending;
		public TextView sendFail;
		public ImageBtSingle callBtn;
        public boolean isCom;
	}

	@Override
	public void onChange() {
		postNotifyDataChange();
		context.sendBroadcast(new Intent(
				IntentConstants.INTENT_ACTION_SCROLL_TO_BOTTOM));
	}

	@Override
	public void onAdd(ChatMsgEntity item) {
		postNotifyDataChange();
		context.sendBroadcast(new Intent(
				IntentConstants.INTENT_ACTION_SCROLL_TO_BOTTOM));
	}

	@Override
	public void onAddAll(List<ChatMsgEntity> items) {
		postNotifyDataChange();
		context.sendBroadcast(new Intent(
				IntentConstants.INTENT_ACTION_SCROLL_TO_BOTTOM));
	}

	@Override
	public void onUpdate(ChatMsgEntity chatMsgEntity) {
		postNotifyDataChange();
		context.sendBroadcast(new Intent(
				IntentConstants.INTENT_ACTION_SCROLL_TO_BOTTOM));
	}

	@Override
	public void onDelete(ChatMsgEntity item) {
		postNotifyDataChange();
		context.sendBroadcast(new Intent(
				IntentConstants.INTENT_ACTION_SCROLL_TO_BOTTOM));
	}

	@Override
	public void onDeleteAll(List<ChatMsgEntity> items) {
		postNotifyDataChange();
		context.sendBroadcast(new Intent(
				IntentConstants.INTENT_ACTION_SCROLL_TO_BOTTOM));
	}

	private void postNotifyDataChange() {
		changeHandler.sendEmptyMessage(0);
	}

	private Handler changeHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				notifyDataSetChanged();
			}
		}
	};
}
