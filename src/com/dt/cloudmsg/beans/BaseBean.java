package com.dt.cloudmsg.beans;

import com.dt.cloudmsg.util.Encoder;
import com.dt.cloudmsg.util.JsonUtil;
import com.dt.cloudmsg.util.SystemConstants;

import java.io.IOException;


public class BaseBean implements Jsonable{

	public String toJson() {
		return JsonUtil.toJsonWithExposeAnnotation(this);
	}

	public String toDESJson() {
		try {
			return Encoder.encodeBASE64(Encoder.encryptDES(Encoder.gzip(toJson()), SystemConstants.DES_KEY4));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
