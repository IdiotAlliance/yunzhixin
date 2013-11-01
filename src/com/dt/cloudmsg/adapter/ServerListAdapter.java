package com.dt.cloudmsg.adapter;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.datasource.ServerSource;
import com.dt.cloudmsg.datasource.XDataChangeListener;
import com.dt.cloudmsg.model.Device;
import com.dt.cloudmsg.views.SettingActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ServerListAdapter extends BaseAdapter implements XDataChangeListener<Device>, Removable{
	private static final String TAG = ServerListAdapter.class.getSimpleName();
    private ServerSource serverSource;
	private Context context;
    private String imei;
    private String username;
    private List<Device> devices = new ArrayList<Device>();

	public ServerListAdapter(Context context, ServerSource serverSource, boolean onServer, String imei, String username) {
		this.context = context;
        this.serverSource = serverSource;
        this.username = username;
        this.imei = imei;
        serverSource.registerDataChangeListener(this);

        load();

    }

	public int getCount() {
		return devices.size();
	}

	public Object getItem(int position) {
		return devices.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = View.inflate(context, R.layout.item_sever_selector, null);
            ViewHolder holder = new ViewHolder();
            TextView tv = (TextView) convertView.findViewById(R.id.server_selector_option_number);
            ImageView iv = (ImageView) convertView.findViewById(R.id.server_selector_option_icon);
            holder.number = tv;
            holder.img = iv;
            convertView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.number.setText(devices.get(position).getName() + "(" + devices.get(position).getNumber() + ")");
        holder.img.setImageResource(R.drawable.server_online);
        // TODO 根据状态改变图标
        return convertView;
	}

    private class ViewHolder{
        TextView number;
        ImageView img;
    }

    @Override
    public void onChange() {
        load();
        this.notifyDataSetChanged();
    }

    @Override
    public void onAdd(Device device) {
        load();
        this.notifyDataSetChanged();
    }

    @Override
    public void onAddAll(List<Device> list) {
        load();
        this.notifyDataSetChanged();
    }

    @Override
    public void onUpdate(Device device) {
        load();
        this.notifyDataSetChanged();
    }

    @Override
    public void onDelete(Device device) {
        load();
        this.notifyDataSetChanged();
    }

    @Override
    public void onDeleteAll(List<Device> list) {
        load();
        this.notifyDataSetChanged();
    }

    @Override
    public void unregister() {
        serverSource.removeListener(this);
    }

    private void load(){
        devices.clear();
        int size = serverSource.size();
        for(int i = 0; i < size; i ++){
            Device device = serverSource.get(i);
            if(!imei.equals(device.getImei()) && device.isBound())
                devices.add(device);
        }
        this.notifyDataSetChanged();
        Log.d(TAG, "num of devices:" + devices.size());
    }
}
