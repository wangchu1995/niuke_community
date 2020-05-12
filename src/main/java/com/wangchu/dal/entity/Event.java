package com.wangchu.dal.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
    //事件的主题
    private String topic;
    //触发事件的人,谁评论了帖子
    private int userId;
    //触发事件类型及事件本身id
    private int entityType;
    private int entityId;
    //触发事件的接收人
    private int entityUserId;
    //存储额外数据信息
    private Map<String,Object> data = new HashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key,Object value) {
        this.data.put(key,value);
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }
}
