package com.dt.cloudmsg.dao;

/**
 * Created by lvxiang on 13-10-11.
 */
public interface DAOObserver<T> {

    public void onChange();

    public void onUpdate(T t);

    public void onAdd(T t);

    public void onDelete(T t);

}
