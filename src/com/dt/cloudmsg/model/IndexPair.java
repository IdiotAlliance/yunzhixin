package com.dt.cloudmsg.model;

public class IndexPair implements Comparable<IndexPair>{

	public int lower;
	public int upper;
	
	public IndexPair(int l,int u){
		this.lower = l;
		this.upper = u;
	}

	@Override
	public int compareTo(IndexPair another) {
		// TODO Auto-generated method stub
		return lower==another.lower?0:lower>another.lower?1:-1;
	}
	
	
}
