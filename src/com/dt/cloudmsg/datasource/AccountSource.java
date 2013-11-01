package com.dt.cloudmsg.datasource;

import android.content.Context;

import com.dt.cloudmsg.model.Account;

/**
 * Created by lvxiang on 13-10-15.
 */
public class AccountSource extends AbstractDataSource<Account>{

    protected AccountSource(Context context, String createSQL) {
        super(context, createSQL);
    }

    @Override
    protected void load() {

    }

    @Override
    public void onChange() {

    }

    @Override
    public void onUpdate(Account account) {

    }

    @Override
    public void onAdd(Account account) {

    }

    @Override
    public void onDelete(Account account) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Account get(int index) {
        return null;
    }
}
