package com.dt.cloudmsg.adapter;

import java.util.List;

import com.dt.cloudmsg.R;
import com.dt.cloudmsg.model.Contact;
import com.dt.cloudmsg.model.Group;
import com.dt.cloudmsg.model.IndexPair;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SimpleExpandableListAdapter extends BaseExpandableListAdapter {

	private LayoutInflater inflater;
	private List<Contact> contacts;
	private List<Group> groups;

	public SimpleExpandableListAdapter(Context context, List<Group> groups,
			List<Contact> contacts) {
		this.inflater = LayoutInflater.from(context);
		this.contacts = contacts;
		this.groups = groups;
	}

	@Override
	public Object getChild(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return getContact(arg0, arg1);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return getContact(groupPosition, childPosition).hashCode();
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ChildHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_simple_multi_choice,
					null);
			holder = new ChildHolder();
			holder.checkbox = (ImageView) convertView
					.findViewById(R.id.simple_multi_choice_checkbox);
			holder.name = (TextView) convertView
					.findViewById(R.id.simple_multi_choice_name);
			holder.number = (TextView) convertView
					.findViewById(R.id.simple_multi_choice_number);
			convertView.setTag(holder);
		}
		holder = (ChildHolder) convertView.getTag();
		Contact c = getContact(groupPosition, childPosition);
		if (c.chosen)
			holder.checkbox
					.setImageResource(R.drawable.checkbox_selected);
		else
			holder.checkbox
					.setImageResource(R.drawable.checkbox_unselected);
		holder.name.setText(c.name);
		holder.number.setText(c.number);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return groups.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		GroupHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.item_simple_multi_choice_group, null);
			holder = new GroupHolder();
			holder.checkbox = (ImageView) convertView
					.findViewById(R.id.simple_multi_choice_group_checkbox);
			holder.title = (TextView) convertView
					.findViewById(R.id.simple_multi_choice_group_title);
			holder.info = (TextView) convertView
					.findViewById(R.id.simple_multi_choice_group_info);
			holder.indicator = (ImageView) convertView
					.findViewById(R.id.simple_multi_choice_group_indicator);
			convertView.setTag(holder);
		}
		holder = (GroupHolder) convertView.getTag();
		if (groups.get(groupPosition).chosen) {
			holder.checkbox
					.setImageResource(R.drawable.checkbox_selected);
		} else {
			holder.checkbox
					.setImageResource(R.drawable.checkbox_unselected);
		}
		holder.checkbox.setOnClickListener(new GroupCheckboxOnClickListener(
				groupPosition));
		holder.title.setText(groups.get(groupPosition).title);
		holder.info.setText("(" + groups.get(groupPosition).size() + ")");
		if (isExpanded) {
			holder.indicator
					.setImageResource(android.R.drawable.arrow_up_float);
		} else
			holder.indicator
					.setImageResource(android.R.drawable.arrow_down_float);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	private class ChildHolder {
		ImageView checkbox;
		TextView name;
		TextView number;
	}

	private class GroupHolder {
		ImageView checkbox;
		TextView title;
		TextView info;
		ImageView indicator;
	}

	private class GroupCheckboxOnClickListener implements OnClickListener {

		private int gp;

		public GroupCheckboxOnClickListener(int gp) {
			this.gp = gp;
		}

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Group g = groups.get(gp);
			if (g.size() > 0) {
				for (IndexPair pair : g.contacts)
					for (int i = pair.lower; i <= pair.upper; i++)
						contacts.get(i).chosen = !g.chosen;
				g.chosen = !g.chosen;
				SimpleExpandableListAdapter.this.notifyDataSetChanged();
			}
		}
	}

	public Contact getContact(int g, int c) {
		return contacts.get(groups.get(g).getIndex(c));
	}

}
