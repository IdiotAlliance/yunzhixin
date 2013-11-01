package com.dt.cloudmsg.model;

import com.dt.cloudmsg.util.StringUtil;


public class Contact implements Comparable<Contact>{

    public int index;
	public String key; // the key by witch contacts are sorted
	public String key_lower;
	public String contact_id;
	public String name;
	public String number;
	public boolean chosen = false;
	public int stCN   = 0;
	public char[] key_arr;
    public String shouZiMu;

	/**
	 *
     * @param index
	 * @param contact_id
	 * @param name
	 * @param number
	 */
	public Contact(int index, String contact_id,String name,String number){
        this.index = index;
		this.contact_id = contact_id;
		this.name = name;
		this.number = number;

		if(!StringUtil.startWithChinese(name))
			this.stCN = 1;
		this.key  = StringUtil.getPinyin(name);
		this.key_lower = this.key.toLowerCase();
		this.key_arr   = this.key_lower.toCharArray();
        this.shouZiMu = StringUtil.getShouZiMu(name);
		
		//Log.d("联系人姓名拼音", name + ":" + key_lower);
	}

	@Override
	public int compareTo(Contact another) {
		// TODO Auto-generated method stub
		if(this.key_arr[0] == another.key_arr[0]){
			if(this.stCN > another.stCN)
				return -1;
			if(this.stCN < another.stCN)
				return 1;
			return this.key_lower.compareTo(another.key_lower);
		}
		else
			if(this.key_arr[0] > another.key_arr[0])
					return 1;
			else
				return -1;
	}
	
}
