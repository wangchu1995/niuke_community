package com.wangchu.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String LIKE_PREFIX="like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    public static String getLikeKey(int entityType,int entityId){
        return LIKE_PREFIX+SPLIT+entityType+SPLIT+entityId;
    }

    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }
}
