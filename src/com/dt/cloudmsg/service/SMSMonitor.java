package com.dt.cloudmsg.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by lvxiang on 13-10-22.
 */
public class SMSMonitor {

    private Context context;
    private ContentResolver contentResolver = null;
    private Handler smshandler = null;
    private ContentObserver smsObserver = null;
    public String smsNumber = "";
    public static boolean thCountStatus = false;
    public static int thIncreCount = 0;
    public boolean monitorStatus = false;
    String code;
    //   Feedmanager fm = null;
    static public String activationCode;
    int smsCount = 0;

    public SMSMonitor(final Context context, final Handler smshandler) {
        this.context = context;
        contentResolver = context.getContentResolver();
        this.smshandler = smshandler;
        smsObserver = new SMSObserver(smshandler);
    }

    public void startSMSMonitoring() {
        try {
            monitorStatus = false;
            if (!monitorStatus) {
                contentResolver.registerContentObserver(Uri.parse("content://sms"), true, smsObserver);
            }
        } catch (Exception e) {
            Log.d("test", "SMSMonitor :: startSMSMonitoring Exception == " + e.getMessage());
        }
    }

    public void stopSMSMonitoring() {
        try {
            monitorStatus = false;
            if (!monitorStatus) {
                contentResolver.unregisterContentObserver(smsObserver);
            }
        } catch (Exception e) {
            Log.e("test","SMSMonitor :: stopSMSMonitoring Exception == "+ e.getMessage());
        }
    }

    class SMSObserver extends ContentObserver {
        private Handler sms_handle = null;
        public SMSObserver(final Handler smshandle) {
            super(smshandle);
            sms_handle = smshandle;
        }

        public void onChange(final boolean bSelfChange) {
            super.onChange(bSelfChange);
            Thread thread = new Thread() {
                public void run() {
                    try {
                        monitorStatus = true;

                        // Send message to Activity
                        Uri uriSMSURI = Uri.parse("content://sms");
                        Cursor cur = context.getContentResolver().query(
                                uriSMSURI, null, null, null, "_id");

                        if (cur.getCount() != smsCount) {
                            smsCount = cur.getCount();

                            if (cur != null && cur.getCount() > 0) {
                                cur.moveToLast();
                                for (int i = 0; i < cur.getColumnCount(); i++)
                                {
                                    Log.d("sms db column","SMSMonitor :: incoming Column Name : " +
                                    cur.getColumnName(i) + ":" +
                                    cur.getString(i));
                                }

                                smsNumber = cur.getString(cur.getColumnIndex("address"));
                                if (smsNumber == null || smsNumber.length() <= 0)
                                {
                                    smsNumber = "Unknown";
                                }
                                int type = Integer.parseInt(cur.getString(cur.getColumnIndex("type")));
                                String message = cur.getString(cur.getColumnIndex("body"));
                                Log.d("test","SMSMonitor :: SMS type == " + type);
                                Log.d("test","SMSMonitor :: Message Txt == " + message);
                                Log.d("test","SMSMonitor :: Phone Number == " + smsNumber);

                                cur.close();

                                if (type == 1) {
                                    // do nothing
                                    // onSMSReceive(message, smsNumber);
                                } else {
                                    Message message1 = new Message();
                                    message1.what = MyService.MSG_SEND;
                                    Bundle bundle = new Bundle();
                                    bundle.putString(MyService.KEY_TARGET, smsNumber);
                                    bundle.putString(MyService.KEY_CONTENT, message);
                                    message1.setData(bundle);
                                    sms_handle.sendMessage(message1);
                                }
                            }
                        }
                    } catch (Exception e) {
//                  Log("KidSafe","SMSMonitor :: onChange Exception == "+ e.getMessage());
                    }
                }
            };
            thread.start();
        }
    }
}
