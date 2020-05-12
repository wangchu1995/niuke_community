package com.wangchu.util;

public class CommunityConstant {
    public static final int ACTIVATION_SUCCESS=0;
    public static final int ACTIVATION_FALIURE=1;
    public static final int ACTIVATION_REPEAT = 2;

    public static final int DEFAULT_TICKETTIME = 10*60;
    public static final int LONG_TICKETTIME = 60*60*6;

    public static final int ENTITY_TYPE_POST = 1;
    public static final int ENTITY_TYPE_COMMENT= 2;
    public static final int ENTITY_TYPE_USER= 3;

    //kafka 事件类型
    public static final String TOPIC_COMMENT="comment";
    public static final String TOPIC_LIKE="like";
    public static final String TOPIC_FOLLOW="follow";

    //系统用户Id
    public static final int SYSTEM_COMMENT_ID=1;
}
