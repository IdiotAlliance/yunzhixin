package com.dt.cloudmsg.dao;

/**
 * Created by lvxiang on 13-10-11.
 */
public interface ObservableDAO<T> {

    public void register(DAOObserver<T> observer);

    public void unregister(DAOObserver<T> observer);

    public void unregisterAll();

    public void nofiyChange();

    public void notifyAdd(T t);

    public void notifyUpdate(T t);

    public void notifyDelete(T t);
}
