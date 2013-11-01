package com.dt.cloudmsg.datasource;

import android.content.Context;

import com.dt.cloudmsg.dao.AccountDAO;
import com.dt.cloudmsg.model.Account;

/**
 * Created by lvxiang on 13-9-27.
 */
public class DataSourceFactory {

    private static DataSource<Account> accountDataSource;

    public static AccountDAO getAccountDatasource(Context context){
        return new AccountDAO(context);
    }

}
