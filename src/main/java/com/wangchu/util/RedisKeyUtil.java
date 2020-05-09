package com.wangchu.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String LIKE_PREFIX="like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_LOGINTICKE = "loginticket";
    private static final String PREFIX_USER = "user";

    public static String getLikeKey(int entityType,int entityId){
        return LIKE_PREFIX+SPLIT+entityType+SPLIT+entityId;
    }

    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    public static String getFolloweeKey(int userId,int entityType){
        //key:followee:userId:entityType -----> (entityId,now)  我关注了实体
        return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }

    public static String getFollowerKey(int entityType,int entityId){
        //key:follower:entityType:entityId---->(userId,now)  某实体有xx粉丝
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }

    public static String getKaptchaKey(String kaptcha){
        return PREFIX_KAPTCHA+SPLIT+kaptcha;
    }

    public static String getLoginTicket(String ticket){
        return PREFIX_LOGINTICKE+SPLIT+ticket;
    }

    public static String getUserKey(int userId){
        return PREFIX_USER+ SPLIT+userId;
    }
}
