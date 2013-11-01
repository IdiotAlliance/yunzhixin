package com.dt.cloudmsg.datasource;

public interface DataSource<T> {

	public int size();
	
	public T get(int index);
	
	public void registerDataChangeListener(XDataChangeListener<T> listener);
	
	public void removeListener(XDataChangeListener<T> listener);
	
	public void close();
	
}
