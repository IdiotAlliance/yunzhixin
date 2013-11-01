package com.dt.cloudmsg.datasource;

import java.util.List;

public interface XDataChangeListener<T> {

	public void onChange();
	
	public void onAdd(T t);
	
	public void onAddAll(List<T> list);

    public void onUpdate(T t);

	public void onDelete(T t);
	
	public void onDeleteAll(List<T> list);
}
