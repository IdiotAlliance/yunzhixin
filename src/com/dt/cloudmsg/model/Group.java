package com.dt.cloudmsg.model;

import java.text.Collator;
import java.util.List;

import com.dt.cloudmsg.util.StringUtil;


public class Group implements Comparable<Group>{

	public String title;
	public String id;
	public List<IndexPair> contacts;
	public boolean chosen = false;
	
	/**
	 * 
	 * @param id
	 * @param title
	 * @param pairs
	 */
	public Group(String id,String title, List<IndexPair> pairs){
		this.title = title;
		this.id = id;
		this.contacts = pairs;
	}
	
	public int size(){
		int size = 0;
		for(IndexPair pair: contacts){
			size += pair.upper - pair.lower + 1;
		}
		return size;
	}

	@Override
	public int compareTo(Group another) {
		// TODO Auto-generated method stub
		return StringUtil.strCompare(title, another.title, Collator.getInstance(java.util.Locale.CHINA));
	}
	
	/**
	 * 找到该组中第i个元素在联系人列表中的位置
	 * @param index
	 * @return
	 */
	public int getIndex(int index){
		int i = 0;
		for(IndexPair pair: contacts){
			for(int j=pair.lower;j<=pair.upper;j++){
				if(i==index)
					return j;
				i ++;
			}
		}
		return -1;
	}

}