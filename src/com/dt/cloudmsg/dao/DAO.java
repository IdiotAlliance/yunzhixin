package com.dt.cloudmsg.dao;

/**
 * Created by lvxiang on 13-10-11.
 */
public interface DAO<T> {

    public long add(T t);

    public void update(T t);

    public void delete(T t);

    public void deleteById(long id);

    public T getById(long id);

    public void addOrUpdate(T t);

    public boolean exists(long id);

    public void close();

}
