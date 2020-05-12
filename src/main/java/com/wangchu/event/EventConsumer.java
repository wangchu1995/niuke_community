package com.wangchu.event;

import com.alibaba.fastjson.JSONObject;
import com.wangchu.dal.entity.Event;
import com.wangchu.dal.entity.Message;
import com.wangchu.dao.mapper.MessageMapper;
import com.wangchu.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer {
    @Autowired
    MessageMapper messageMapper;

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @KafkaListener(topics = {CommunityConstant.TOPIC_COMMENT,CommunityConstant.TOPIC_LIKE,CommunityConstant.TOPIC_FOLLOW})
    public void handleMessage(ConsumerRecord record){
        if(record==null||record.value()==null) {
            logger.error("消息为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null) {
            logger.error("事件为空");
            return;
        }
        Message message = new Message();
        message.setFromId(CommunityConstant.SYSTEM_COMMENT_ID);
        message.setToId(event.getEntityUserId());
        message.setCreateTime(new Date());
        message.setConversationId(event.getTopic());

        Map<String,Object> content = new HashMap<>();
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        content.put("userId",event.getUserId());
        if(event.getData()!=null&&!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry:event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageMapper.insertMessage(message);
    }
}
