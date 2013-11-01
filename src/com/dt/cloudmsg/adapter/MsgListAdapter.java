package com.dt.cloudmsg.adapter;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.R.color;
import com.dt.cloudmsg.datasource.MsgListSource;
import com.dt.cloudmsg.datasource.XDataChangeListener;
import com.dt.cloudmsg.model.MsgListEntity;
import com.dt.cloudmsg.util.DateFormatter;

import android.os.Handler;
import android.os.Message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MsgListAdapter extends BaseAdapter implements XDataChangeListener<MsgListEntity>, Removable{
	private static final String TAG = MsgListAdapter.class.getSimpleName();

    private MsgListSource msgListSource;
	private Context context;

	public MsgListAdapter(Context context,MsgListSource source) {
		this.context = context;
        this.msgListSource = source;
        source.registerDataChangeListener(this);
	}

	public int getCount() {
		return msgListSource.size();
	}

	public Object getItem(int position) {
		return msgListSource.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		MsgListEntity entity = (MsgListEntity)getItem(position);

		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.msglist_item, null);
			viewHolder = new ViewHolder();
			viewHolder.tvComFrom = (TextView) convertView
					.findViewById(R.id.msglist_item_tv_comfrom);
			viewHolder.tvMsgCount = (TextView) convertView
					.findViewById(R.id.msglist_item_tv_msgcount);
			viewHolder.tvTime = (TextView) convertView
					.findViewById(R.id.msglist_item_tv_time);
			viewHolder.tvLastMsg = (TextView) convertView
					.findViewById(R.id.msglist_item_tv_lastmsg);
			viewHolder.tvNewMsg = (TextView) convertView
					.findViewById(R.id.msglist_item_tv_newmsg);
			viewHolder.tvNewCall = (TextView) convertView
					.findViewById(R.id.msglist_item_tv_newcall);
			viewHolder.imgMsg = (ImageView) convertView
					.findViewById(R.id.msglist_item_msg_img);
			viewHolder.imgCall = (ImageView) convertView
					.findViewById(R.id.msglist_item_call_img);
			viewHolder.unRead = (ImageView) convertView
					.findViewById(R.id.have_unread);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if(entity.getComname()!=null && entity.getComname().length() > 0){
			viewHolder.tvComFrom.setText(entity.getComname());
		}else{
			viewHolder.tvComFrom.setText(entity.getComNumber());
		}
		
		viewHolder.tvMsgCount.setText("(" + entity.getCount() +")");

		viewHolder.tvTime.setText(DateFormatter.getLocalDate(entity.getRtime(), DateFormatter.DATE_FOR_MSGLIST));
		viewHolder.tvLastMsg.setText(entity.getLastMsg());
		
		if(entity.getMsgCount()!=0){
			viewHolder.imgMsg.setVisibility(View.VISIBLE);
			viewHolder.tvNewMsg.setVisibility(View.VISIBLE);
			viewHolder.tvNewMsg.setText(""+entity.getMsgCount());
		}else{
			viewHolder.imgMsg.setVisibility(View.GONE);
			viewHolder.tvNewMsg.setVisibility(View.GONE);
		}
		
		if(entity.getNewCall()!=0){
			viewHolder.imgCall.setVisibility(View.VISIBLE);
			viewHolder.tvNewCall.setVisibility(View.VISIBLE);
			viewHolder.tvNewCall.setText(""+entity.getNewCall());
		}else{
			viewHolder.imgCall.setVisibility(View.GONE);
			viewHolder.tvNewCall.setVisibility(View.GONE);
		}
		
		if((entity.getNewCall()!=0)||(entity.getMsgCount()!=0)){
			viewHolder.unRead.setBackgroundResource(R.drawable.have_unread);
		}else{
			viewHolder.unRead.setBackgroundResource(R.drawable.no_unread);
		}

		  
		return convertView;
	}

    private Object getResources() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void unregister() {
        if(msgListSource != null)
            msgListSource.removeListener(this);
    }

    static class ViewHolder {
        public TextView tvComFrom;
        public TextView tvMsgCount;
        public TextView tvTime;
        public TextView tvLastMsg;
        public TextView tvNewMsg;
        public TextView tvNewCall;

        public ImageView imgError;
        public ImageView imgMsg;
        public ImageView imgCall;
        public ImageView unRead;
        public ImageView noUnRead;
    }

    @Override
    public void onChange() {
        postNotifyDataChange();
    }

    @Override
    public void onAdd(MsgListEntity item) {
        postNotifyDataChange();
    }

    @Override
    public void onAddAll(List<MsgListEntity> items) {
        postNotifyDataChange();
    }

    @Override
    public void onUpdate(MsgListEntity msgListEntity) {
        postNotifyDataChange();
    }

    @Override
    public void onDelete(MsgListEntity item) {
        postNotifyDataChange();
    }

    @Override
    public void onDeleteAll(List<MsgListEntity> items) {
        postNotifyDataChange();
    }


    private void postNotifyDataChange() {
        changeHandler.sendEmptyMessage(0);
    }

    private Handler changeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                notifyDataSetChanged();
            }
        }
    };
}
