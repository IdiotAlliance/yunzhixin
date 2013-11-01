package com.dt.cloudmsg.dao;

import android.content.Context;

import com.dt.cloudmsg.datasource.AbstractDataSource;
import com.dt.cloudmsg.model.NoteMsgEntity;

/**
 * Created by lvxiang on 13-10-8.
 */
public class NoteDAO extends AbstractDAO<NoteMsgEntity>{

    protected NoteDAO(Context context, String createSQL) {
        super(context, createSQL);
    }

    @Override
    public long add(NoteMsgEntity noteMsgEntity) {

        return 0;
    }

    @Override
    public void update(NoteMsgEntity noteMsgEntity) {

    }

    @Override
    public void delete(NoteMsgEntity noteMsgEntity) {

    }

    @Override
    public void deleteById(long id) {

    }

    @Override
    public NoteMsgEntity getById(long id) {
        return null;
    }

    @Override
    public void addOrUpdate(NoteMsgEntity noteMsgEntity) {

    }

    @Override
    public boolean exists(long id) {
        return false;
    }
}
