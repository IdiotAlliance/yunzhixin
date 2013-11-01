package com.dt.cloudmsg.views;

import android.content.SharedPreferences;
import android.drm.DrmManagerClient;
import android.os.Bundle;
import com.dt.cloudmsg.R;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.dt.cloudmsg.util.RDBitmapGenerator;

import java.util.Stack;

public abstract class BaseActivity extends Activity implements Callback{

    protected static final Stack<Activity> activityStack = new Stack<Activity>();
    protected static RDBitmapGenerator generator = RDBitmapGenerator.getInstance();
	
	protected Handler handler;
	protected LinearLayout loadingLayer = null;
	protected TextView loadingText = null;
	
	protected static final String INTENT_FROM    = "from";
	protected static final String FROM_LOGIN     = "from_login";
	protected static final String FROM_SERVICE   = "from_service";
	protected static final String INTENT_ACCOUNT = "account";

    // shared preferences
	protected static final String SHARED_PREFERENCES = "SP";
	protected static final String SP_KEY_FIRST = "first_start";
    protected static final String SP_API_KEY   = "api_key";
    protected static final String SP_LAST_LOGIN = "last_login";
    protected static final String SP_LOGIN_COUNT = "login_count";
    protected static final String SP_LAST_REG = "last_reg";
    protected static final String SP_REG_COUNT = "reg_count";

    // constants
	protected static String gusername = "";
	protected static String gpassword = "";
    protected static String gapiKey   = "";
    protected static String gapiToken = "";
    protected static String gIMEI     = "";
    protected static String gDevicename = "";
    protected static String gDeviceNum  = "";
    protected static boolean gServerOn = false;
    protected static boolean gMsgOn    = false;
    protected static boolean gCallOn   = false;
    protected static boolean gPushOn   = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        activityStack.push(this);

        if(gIMEI == null || gIMEI.equals("")){
            TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
            gIMEI = tm.getDeviceId();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        activityStack.pop();
    }

    public static final int activeActivities(){
        return activityStack.size();
    }

    public static final void logout(){
        while (activityStack.size() > 0){
            activityStack.pop().finish();
        }
    }

	protected void showToast(Context context,String text,int duration){
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}
	
	protected void showToast(Context context, int resid, int duration){
		Toast.makeText(context, resid, Toast.LENGTH_LONG).show();
	}

	protected void initLoadingLayer(final FrameLayout contentLayer){
		loadingLayer = (LinearLayout)View.inflate(this, R.layout.loading_dialog, null);
		loadingText = (TextView) loadingLayer.findViewById(R.id.dialog_loading_text);
        contentLayer.addView(loadingLayer);
		loadingLayer.setVisibility(View.INVISIBLE);
	    loadingLayer.setClickable(true);
    }
	protected void initLoadingLayer(final RelativeLayout contentLayer){
		loadingLayer = (LinearLayout)View.inflate(this, R.layout.loading_dialog, null);
		loadingText = (TextView) loadingLayer.findViewById(R.id.dialog_loading_text);
        contentLayer.addView(loadingLayer, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		loadingLayer.setVisibility(View.INVISIBLE);
	    loadingLayer.setClickable(true);
    }
	
	protected void showLoadingLayer(){
		loadingLayer.setVisibility(View.VISIBLE);
	}
	
	protected void dismissLoadingLayer(){
		loadingLayer.setVisibility(View.INVISIBLE);
	}

    protected void setSharedPreferences(String key, String value){
        SharedPreferences sp =  this.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    protected void setSharedPreferences(String key, Boolean value){
        SharedPreferences sp =  this.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    protected void setSharedPreferences(String key, long value){
        SharedPreferences sp =  this.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    protected void setSharedPreferences(String key, int value){
        SharedPreferences sp =  this.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    protected Boolean getSPBoolean(String key, Boolean def){
        SharedPreferences sp =  this.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return sp.getBoolean(key, def);
    }

    protected String getSPString(String key, String def){
        SharedPreferences sp =  this.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return sp.getString(key, def);
    }

    protected long getSPLong(String key, long def){
        SharedPreferences sp =  this.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return sp.getLong(key, def);
    }

    protected int getSPInt(String key, int def){
        SharedPreferences sp =  this.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return sp.getInt(key, def);
    }

    protected boolean SPExists(String key){
        SharedPreferences sp = this.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return sp.contains(key);
    }

    protected void sendLogoutBroadcast(){

    }


}
