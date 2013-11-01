package com.dt.cloudmsg.communications;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.dt.cloudmsg.util.LogUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.telephony.CellSignalStrength;
import android.util.Log;

import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class MsgSender {
	
	private static final HttpClient hc = AndroidHttpClient.newInstance("android");

    public static final void get(String url, String key, String value, final HttpConnectionListener listener, int nonce){
        get(url, new String[]{key}, new String[]{value}, listener);
    }

    public static final void get(String url, String[] keys, String[] values, final HttpConnectionListener listener){
        String realURL = url;
        if(values != null && values.length > 0){
            realURL += "?" + keys[0] + "=" + values[0];
            for(int i = 1; i < values.length; i++){
                realURL += "&" + keys[1] + "=" + values[1];
            }
        }
        final HttpGet get = new HttpGet(realURL);
        Log.d("getting url:", realURL);
        new AsyncTask(){

            @Override
            protected Object doInBackground(Object... objects) {
                HttpResponse response = null;
                try {
                    response = hc.execute(get);
                    String msg = EntityUtils.toString(response.getEntity());
                    if(listener != null)
                        listener.onMessage(response.getStatusLine().getStatusCode(), msg);
                } catch (ConnectTimeoutException e){
                    e.printStackTrace();
                    if(listener != null){
                        listener.onMessage(HttpStatus.SC_REQUEST_TIMEOUT, null);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    if(response != null && listener != null)
                        listener.onMessage(response.getStatusLine().getStatusCode(), null);
                    else if(listener != null)
                        listener.onMessage(-1, null);
                }
                return null;
            }
        }.execute(null, null, null);
    }
	
	public static void post(String url, String key, String value, HttpConnectionListener listener){
		post(url, new String[]{key}, new String[]{value}, listener);
	}
	
	@SuppressWarnings("unchecked")
	public static final void post(String url, String[] keys, String[] values, final HttpConnectionListener listener){
		final HttpPost post = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
		for(int i = 0; i < keys.length; i++){
			params.add(new BasicNameValuePair(keys[i], values[i]));
		}
        try {
            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new AsyncTask(){

			@Override
			protected Object doInBackground(Object... params) {
                HttpResponse response = null;
				try {
					response = hc.execute(post);
					String msg = EntityUtils.toString(response.getEntity());
                    if(listener != null)
					    listener.onMessage(response.getStatusLine().getStatusCode(), msg);
				}
                catch (ConnectTimeoutException e){
                    e.printStackTrace();
                    if(listener != null){
                        listener.onMessage(HttpStatus.SC_REQUEST_TIMEOUT, null);
                    }
                }
                catch (Exception e) {
                    LogUtils.d("MsgSender", "post exception");
					e.printStackTrace();
                    if(response != null && listener != null){
                        listener.onMessage(response.getStatusLine().getStatusCode(), null);
                    } else if(listener != null)
                        listener.onMessage(-1, null);
				}
				return null;
			}
		}.execute(null, null, null);
	}
	
	private static String getMsg(InputStream in) throws IOException {

        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(in));
        String msg = "";
        String line = null;
        while((line = reader.readLine()) != null){
            msg += line;
        }
        return msg;
    }
}
