package com.wangchu.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommonUtils {

    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    public static String md5(String key){
        if(StringUtils.isBlank(key)) return null;
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
