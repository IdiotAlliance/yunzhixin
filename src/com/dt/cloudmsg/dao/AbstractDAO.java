package com.dt.cloudmsg.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lvxiang on 13-10-11.
 */
public abstract class AbstractDAO<T> implements DAO<T>,ObservableDAO<T>{

    public static final String DB_NAME = "cloudmsg";
    protected static SQLiteDatabase db = null;
    protected Set<DAOObserver<T>> observerSet = new HashSet<DAOObserver<T>>();

    protected AbstractDAO(Context context, String createSQL){
        if(db == null)
            db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        db.execSQL(createSQL);
    }

    @Override
    public void register(DAOObserver<T> observer) {
        observerSet.add(observer);
    }

    @Override
    public void unregister(DAOObserver<T> observer) {
        observerSet.remove(observer);
    }

    @Override
    public void unregisterAll(){
        observerSet.clear();
    }

    @Override
    public void nofiyChange() {
        for(DAOObserver observer: observerSet){
            observer.onChange();
        }
    }

    @Override
    public void notifyAdd(T t) {
        for(DAOObserver observer: observerSet){
            observer.onAdd(t);
        }
    }

    @Override
    public void notifyUpdate(T t) {
        for(DAOObserver observer: observerSet){
            observer.onUpdate(t);
        }
    }

    @Override
    public void notifyDelete(T t) {
        for(DAOObserver observer: observerSet){
            observer.onDelete(t);
        }
    }

    public void close(){
        unregisterAll();
        if(db != null){
            db.close();
            db = null;
        }
    }
}
