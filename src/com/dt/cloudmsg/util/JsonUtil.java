package com.dt.cloudmsg.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtil {
	
	private static Gson gson = new Gson();
	private static GsonBuilder builder = new GsonBuilder();
	private static Gson exposeGson = builder.excludeFieldsWithoutExposeAnnotation().create();
	
	public static String toJson(Object o){
		return gson.toJson(o);
	}

	public static String toJsonWithExposeAnnotation(Object o){
		return exposeGson.toJson(o);
	}
	
	public static <T> T fromJson(String json, Class<T> c){
		return gson.fromJson(json, c);
	}

    public static String getValue(String key, String json){
        Pattern pattern = Pattern.compile("\"" + key + "\":(\\{[^}]*\\})");
        Matcher matcher = pattern.matcher(json);
        if(matcher.find())
            return matcher.group(1);
        return null;
    }
}
