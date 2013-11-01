package com.dt.cloudmsg.dao;

import android.content.Context;

import com.dt.cloudmsg.datasource.DataSource;
import com.dt.cloudmsg.datasource.MsgListSource;
import com.dt.cloudmsg.model.MsgListEntity;

public class DAOFacade {

    private static ChatMsgDAO cmd = null;
    private static AccountDAO acd = null;
    private static MsgListDAO mld = null;

    public static ChatMsgDAO getChatMsgDAO(Context context){
        if(cmd == null)
            cmd = new ChatMsgDAO(context);
        return cmd;
    }

    public static AccountDAO getAccountDAO(Context context){
        if(acd == null)
            acd = new AccountDAO(context);
        return acd;
    }

}
