package com.dt.cloudmsg.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.model.Contact;
import com.dt.cloudmsg.util.IntentConstants;
import com.dt.cloudmsg.util.NumberFormatter;
import com.dt.cloudmsg.util.StringUtil;
import com.dt.cloudmsg.views.ConversationActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lvxiang on 13-11-6.
 */
public class SessionShortcutAdapter extends BaseAdapter{

    private Context context;
    private List<Contact> contacts;
    private List<Contact> filteredContacts = new ArrayList<Contact>();
    private String account;
    private String source;

    public SessionShortcutAdapter(Context context, List<Contact> contacts, String account){
        this.context  = context;
        this.contacts = contacts;
        this.account = account;
    }

    @Override
    public int getCount() {
        return filteredContacts.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredContacts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_session_shortcut, null);
            holder.newSession = (LinearLayout) convertView.findViewById(R.id.item_session_shotcut_new_session);
            holder.name   = (TextView) convertView.findViewById(R.id.item_session_shortcut_name);
            holder.newMsg = (ImageButton) convertView.findViewById(R.id.item_session_shorcut_create_session);
            holder.makeCall = (ImageButton) convertView.findViewById(R.id.item_session_shorcut_call);
            convertView.setTag(holder);
        }else
            holder = (ViewHolder) convertView.getTag();

        final Contact current = filteredContacts.get(i);
        holder.name.setText(current.getNameNumber());
        holder.newSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String target  = NumberFormatter.normalizeNumber(current.getNumber());

                Intent intent = new Intent(context, ConversationActivity.class);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_ACCOUNT, account);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_SOURCE, source);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_TARGET, target);
                intent.putExtra(IntentConstants.KEY_INTENT_MSG_CHAT_NAME, current.getName());
                context.startActivity(intent);
            }
        });
        holder.newMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri smsToUri = Uri.parse("smsto:"+ current.getNumber());
                Intent intent = new Intent(Intent.ACTION_SENDTO,smsToUri);
                context.startActivity(intent);
            }
        });
        holder.makeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + current.getNumber()));
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    private class ViewHolder{
        LinearLayout newSession;
        TextView name;
        ImageButton newMsg;
        ImageButton makeCall;
    }

    public void filter(String filter, String source){
        this.source = source;
        this.filteredContacts.clear();
        String pinyin = StringUtil.getPinyin(filter);
        String shouZiMu = StringUtil.getShouZiMu(filter);
        if (StringUtil.isNumber(filter)) {
            for (Contact contact : contacts) {
                if (contact.number.contains(filter))
                    filteredContacts.add(contact);
            }
        } else {
            for (Contact contact : contacts) {
                if (contact.name.contains(filter))
                    filteredContacts.add(contact);
            }
            if (filteredContacts.size() <= 0) {
                for (Contact contact : contacts) {
                    if (contact.key_lower.contains(pinyin
                            .toLowerCase())
                            || contact.shouZiMu.contains(shouZiMu))
                        filteredContacts.add(contact);
                }
            }
        }
        this.notifyDataSetChanged();
    }
}
