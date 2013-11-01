package com.dt.cloudmsg.util;

/**
 * Created by lvxiang on 13-10-24.
 */
public class NumberFormatter {

    /***
     * 标准化电话号码
     * @param number
     * @return
     */
    public static String normalizeNumber(final String number){
        if(number != null){
            return number.replaceAll("^(\\+86)", "").replaceAll("^(\\+)", "");
        }
        return null;
    }
}
