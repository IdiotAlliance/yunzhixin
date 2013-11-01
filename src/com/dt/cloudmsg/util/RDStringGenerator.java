package com.dt.cloudmsg.util;

/**
 * Created by lvxiang on 13-9-24.
 */
public class RDStringGenerator {

    private static final String[] alphebat = {
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
            "l", "m", "o", "p", "q", "r", "s", "t", "u", "v", "w",
            "x", "y", "z", "_"
    };

    public static final String genNonce(int len){
        if(len <= 0)
            throw new IllegalArgumentException();
        String nonce = "";
        for(int i = 0; i < len; i++)
            nonce = nonce + alphebat[((int) (Math.random() * alphebat.length))];
        return nonce;
    }

}
