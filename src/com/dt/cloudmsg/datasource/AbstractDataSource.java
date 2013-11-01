package com.dt.cloudmsg.datasource;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.dt.cloudmsg.dao.AbstractDAO;
import com.dt.cloudmsg.dao.DAOObserver;

public abstract class AbstractDataSource<T> implements DataSource<T>, DAOObserver<T>{

	protected Context context;
	protected SQLiteDatabase db;
	protected List<XDataChangeListener<T>> listeners = null;
	
	protected static final String DB_NAME = "cloudmsg";

	protected AbstractDataSource(Context context, String createSQL){
		this.context = context;
        this.listeners = new ArrayList<XDataChangeListener<T>>();
        if(createSQL != null){
		    this.db = context.openOrCreateDatabase(AbstractDAO.DB_NAME, Context.MODE_PRIVATE, null);
            this.db.execSQL(createSQL);
        }
	}

    @Override
    public void registerDataChangeListener(XDataChangeListener<T> listener){
        listeners.add(listener);
    }

    @Override
    public void removeListener(XDataChangeListener<T> listener){
        listeners.remove(listener);
    }

    protected void notifyChange(){
        for(XDataChangeListener<T> listener: listeners){
            listener.onChange();
        }
    }

    protected void notifyAdd(T t){
		for(XDataChangeListener<T> listener: listeners){
			listener.onAdd(t);
		}
	}
	
	protected void notifyAddList(List<T> list){
		for(XDataChangeListener<T> listener: listeners){
			listener.onAddAll(list);
		}
	}
	
	protected void notifyDelete(T t){
		for(XDataChangeListener<T> listener: listeners){
			listener.onDelete(t);
		}
	}
	
	protected void notifyDeleteList(List<T> list){
		for(XDataChangeListener<T> listener: listeners){
			listener.onDeleteAll(list);
		}
	}

    protected void notifyUpdate(T t){
        for(XDataChangeListener<T> listener: listeners){
            listener.onUpdate(t);
        }
    }

	public void close(){
		this.db.close();
	}

    protected abstract void load();
}
