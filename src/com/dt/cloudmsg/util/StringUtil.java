package com.dt.cloudmsg.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.codec.binary.Base64;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	public static final int DATE_FOR_MSGLIST= 0x01;
	public static final int DATE_FOR_CHATMSG= 0x02;

    private static final Pattern numberPattern = Pattern.compile("[0-9]+");
	
	private static net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat t1 = new HanyuPinyinOutputFormat();
	static {
		t1.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		t1.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		t1.setVCharType(HanyuPinyinVCharType.WITH_V);
	}

	public static int strCompare(String str1, String str2, Collator cmp) {
		if (str1.equals(str2))
			return 0;

		String[] arr = new String[] { str1, str2 };
		Arrays.sort(arr, cmp);
		if (str1.equals(arr[0]))
			return -1;
		return 1;
	}

	/**
	 * 获取一个字符串的全拼，如果字符串中包含多音字，则进行重复
	 *
	 * @param str
	 * @return
	 */
	public static String getPinyin(String str) {
        if(str != null){
            List<String> results = new ArrayList<String>();
            results.add("");
            for (char c : str.toCharArray()) {
                List<String> temp = new ArrayList<String>();
                String string = String.valueOf(c);
                if (isChinese(string)) {
                    String[] pinyins = toPinYin(c);
                    for(String pinyin: pinyins){
                        for(String result: results){
                            temp.add(result + pinyin.trim());
                        }
                    }
                } else {
                    for(String result: results)
                        temp.add(result + string);
                }
                results = temp;
            }
            String py = "";
            for(String result: results) py += result;
            return py;
        }
        return null;
	}

    /***
     * 获得一个字符串的首字母, 结果转化为小写
     * @param str
     * @return
     */
    public static String getShouZiMu(String str){
        if(str != null){
            List<String> results = new ArrayList<String>();
            results.add("");
            String[] segs = str.toLowerCase().split("\\s*[._-]+\\s*");
            for(String seg: segs){
                if(seg != null && seg.length() > 0){
                    for(char c: seg.toCharArray()){
                        List<String> temp = new ArrayList<String>();
                        if(isChinese(c + "")){
                            String[] pinyins = toPinYin(c);
                            for(String pinyin: pinyins){
                                for(String result: results){
                                    if(pinyin.length() > 0)
                                        temp.add(result + pinyin.toCharArray()[0]);
                                }
                            }
                        }
                        else{
                            for(String result: results)
                                temp.add(result + c);
                        }
                        results = temp;
                    }
                }
            }
            String shouzimu = "";
            for(String result: results)
                shouzimu += result;
            return shouzimu.toLowerCase();
        }
        return null;
    }

    /***
     * 取出一个字符串中最长的数字序列
     * @param str
     * @return
     */
    public static String extractNumber(String str){
        if(str != null){
            String result = "";
            Matcher matcher = numberPattern.matcher(str);
            while (matcher.find()){
                String temp = matcher.group(0);
                result = result.length() > temp.length() ? result : temp;
            }
            return result;
        }
        return null;
    }

    public static boolean isLetter(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

	/***
	 * �ж�һ���ַ��Ƿ��������ִ���ͷ
	 *
	 * @param word
	 * @return
	 */
	public static boolean startWithChinese(String word) {
		return word.matches("^[\u4e00-\u9fa5]");
	}

	/**
	 * �ж�һ���ַ��Ƿ�Ϊ�����ַ�
	 *
	 * @param c
	 * @return
	 */
	public static boolean isChinese(String c) {
		return c.matches("[\u4e00-\u9fa5]+");
	}

    public static boolean isNumber(String str){
        return numberPattern.matcher(str).matches();
    }

	/**
	 * �ж�һ���ַ��Ƿ������
	 *
	 * @param c
	 * @return
	 */
	public static boolean contacinsChinese(String c) {
		return c.matches(".[\u4e00-\u9fa5]+.");
	}

	/**
	 * ʹ��pinyin4j������ת��Ϊ����ƴ��
	 *
	 * @param str
	 * @return
	 */
	protected static String toPinYin(String str) {
		String py = "";
		String[] t = new String[str.length()];

		char[] hanzi = new char[str.length()];
		for (int i = 0; i < str.length(); i++) {
			hanzi[i] = str.charAt(i);
		}

		try {
			for (int i = 0; i < str.length(); i++) {
				if ((str.charAt(i) >= 'a' && str.charAt(i) < 'z')
						|| (str.charAt(i) >= 'A' && str.charAt(i) <= 'Z')
						|| (str.charAt(i) >= '0' && str.charAt(i) <= '9')) {
					py += str.charAt(i);
				} else {
					t = PinyinHelper.toHanyuPinyinStringArray(hanzi[i], t1);
					py = py + t[0];
				}
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}

		return py.trim().toString().toLowerCase();
	}

	/**
	 * Parse a Chinese string into pinyin representation
	 *
	 * @param c
	 * @return
	 * @throws net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination
	 */
	protected static String[] toPinYin(char c) {

		try {
			return PinyinHelper.toHanyuPinyinStringArray(c, t1);
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String[]{"z"};
	}

	public static String BASE64Encode(String str)
			throws UnsupportedEncodingException {
		return Base64.encodeBase64String(str.getBytes("UTF-8"));
	}

	public static String BASE64Decode(String str)
			throws UnsupportedEncodingException {
		return new String(Base64.decodeBase64(str), "UTF-8");
	}

	public static boolean moreThanNumber(String str) {
		if (str == null || str.length() <= 0) {
			return false;
		}
		int len = 0;

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			//((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
			//|| (c >= 'A' && c <= 'Z')) 字母 数字
			if (c >= '0' && c <= '9') {
				// 数字
				len++;
			}
		}
		if(len==str.length()){
			return false ;
		}else{
			return true ;
		}

	}

	public static String getNumber(String str) {
		String myNumber="";
		if (str == null || str.length() <= 0) {
			return myNumber;
		}

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			//((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
			//|| (c >= 'A' && c <= 'Z')) 字母 数字
			if (c >= '0' && c <= '9') {
				// 数字
				myNumber=myNumber+c;
			}
		}
		return myNumber;

	}

	public static String getLocalDate(long msgDate,int DATE_TYPE){
		String returnDate="";
		SimpleDateFormat    sDateFormatY    =   new    SimpleDateFormat("yyyy");
		returnDate=returnDate+compareDate(sDateFormatY,msgDate);
		SimpleDateFormat    sDateFormatM    =   new    SimpleDateFormat("MM-dd");
		returnDate=returnDate+compareDate(sDateFormatM,msgDate);
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
	
	public static final boolean isEmpty(String str){
		return str == null || str.length() == 0;
	}

	private static String compareDate(SimpleDateFormat sDateFormat,long msgDate){
		String    date    =    sDateFormat.format(new    Date());
		String    date2   =    sDateFormat.format(msgDate);
		if(date.equals(date2)){
			return "";
		}else{
			return date2;
		}
	}
	
	// 获取AppKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
        	return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
            	apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {

        }
        return apiKey;
    }

}
