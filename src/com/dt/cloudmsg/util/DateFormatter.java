package com.dt.cloudmsg.util;

import java.text.SimpleDateFormat;

/**
 * Created by lvxiang on 13-10-27.
 */
public class DateFormatter {

    public static final int DATE_FOR_MSGLIST = 0x00;
    public static final int DATE_FOR_CHATMSG = 0x01;

    public static String getLocalDate(long msgDate,int DATE_TYPE){
        String returnDate="";
        SimpleDateFormat sDateFormatY    =   new    SimpleDateFormat("yyyy");
        returnDate=returnDate + compareDate(sDateFormatY,msgDate);
        SimpleDateFormat    sDateFormatM    =   new    SimpleDateFormat("MM-dd");
        returnDate=returnDate + compareDate(sDateFormatM,msgDate);
        SimpleDateFormat    sDateFormatT    =   new    SimpleDateFormat("HH:mm");
        if(returnDate.equals("")){
            returnDate=sDateFormatT.format(msgDate);
        }else{
            switch(DATE_TYPE){
                case DATE_FOR_MSGLIST:
                    returnDate=returnDate;
                    break;
                case DATE_FOR_CHATMSG:
                    returnDate=returnDate+" "+sDateFormatT.format(msgDate);
            }
        }
        return returnDate;
    }

    private static String compareDate(SimpleDateFormat sDateFormat,long msgDate){
        String    date    =    sDateFormat.format(new    java.util.Date());
        String    date2   =    sDateFormat.format(msgDate);
        if(date.equals(date2)){
            return "";
        }else{
            return date2;
        }
    }

}
