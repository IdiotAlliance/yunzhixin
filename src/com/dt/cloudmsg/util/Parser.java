package com.dt.cloudmsg.util;

/**
 * Created by lvxiang on 13-11-1.
 */
public class Parser {

    public static  <T> T fromEncodedJson(String json, Class<T> c) throws Exception {
        return JsonUtil.fromJson(new String(
                Encoder.decompressGzip( // decompress with gzip
                        Encoder.decryptDES( // decrypt with des
                                Encoder.decodeBASE64(json), // decrypt with base64
                                SystemConstants.DES_KEY4)), "utf-8"), c);
    }

}
